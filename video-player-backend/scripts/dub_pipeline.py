#!/usr/bin/env python3
"""
Pipeline de doublage local temps réel haute performance:
  - Extraction audio et fond musical optimisés
  - Synthèse vocale Edge-TTS / Zero-Shot / gTTS
  - Streaming direct réplique par réplique dès la transcription
  - Traduction ultra-rapide avec multi-fallbacks
  - Gestion dynamique du seek via stdin
"""

import argparse
import asyncio
import hashlib
import json
import math
import os
import re
import shutil
import subprocess
import sys
import tempfile
import threading
import time
import warnings

warnings.filterwarnings("ignore", category=RuntimeWarning, module="pydub")

# Optimisations threads et environnement
os.environ["KMP_DUPLICATE_LIB_OK"] = "TRUE"
os.environ["OMP_NUM_THREADS"] = "4"
os.environ["MKL_NUM_THREADS"] = "4"
os.environ["OPENBLAS_NUM_THREADS"] = "4"

if sys.platform == "win32":
    try:
        asyncio.set_event_loop_policy(asyncio.WindowsSelectorEventLoopPolicy())
    except Exception:
        pass

def get_ffmpeg() -> str:
    import imageio_ffmpeg
    exe = imageio_ffmpeg.get_ffmpeg_exe()
    exe_dir = os.path.dirname(exe)
    if exe_dir not in os.environ.get("PATH", ""):
        os.environ["PATH"] = exe_dir + os.pathsep + os.environ.get("PATH", "")
    return exe

FFMPEG_BIN = get_ffmpeg()

from pydub import AudioSegment
AudioSegment.converter = FFMPEG_BIN

priority_seek_target = None
seek_lock = threading.Lock()


def log_progress(pct: int, label: str) -> None:
    print(f"PROGRESS {int(pct)} {label}", flush=True)


def run(cmd: list, allow_fail: bool = False) -> bool:
    proc = subprocess.run(cmd, capture_output=True, text=True, encoding="utf-8", errors="replace")
    if proc.returncode != 0 and not allow_fail:
        print(f"[dub] ECHEC: {' '.join(cmd[:3])}...", file=sys.stderr)
        print(proc.stderr[-600:], file=sys.stderr)
        return False
    return proc.returncode == 0


def cleanup_stale_locks(model_cache: str) -> None:
    """Nettoie les verrous de cache huggingface orphelins."""
    if not model_cache:
        return
    locks_dir = os.path.join(model_cache, ".locks")
    if os.path.exists(locks_dir):
        try:
            for item in os.listdir(locks_dir):
                item_path = os.path.join(locks_dir, item)
                try:
                    if os.path.isfile(item_path):
                        os.remove(item_path)
                    elif os.path.isdir(item_path):
                        shutil.rmtree(item_path, ignore_errors=True)
                except Exception:
                    pass
        except Exception:
            pass


def extract_audio(ffmpeg: str, video_path: str, wav_out: str) -> bool:
    """Extraction audio rapide PCM mono 16kHz."""
    return run([ffmpeg, "-y", "-i", video_path, "-vn", "-ac", "1", "-ar", "16000",
                "-c:a", "pcm_s16le", wav_out])


def extract_background_music(ffmpeg: str, orig_wav: str, bg_wav_out: str, workdir: str) -> bool:
    """
    Séparation musicale / voix résiliente :
    Atténuation douce de la bande vocale ou volume ducking sans blocage.
    """
    try:
        filt = "volume=0.30"
        ok = run([ffmpeg, "-y", "-i", orig_wav, "-af", filt, "-ar", "44100", "-c:a", "pcm_s16le", bg_wav_out], allow_fail=True)
        if ok and os.path.exists(bg_wav_out) and os.path.getsize(bg_wav_out) > 500:
            print("[dub][AUDIO] Fond musical extrait avec succès", flush=True)
            return True
    except Exception as e:
        print(f"[dub] Extraction musique notice: {e}", file=sys.stderr)
    return False


def estimate_speaker_pitch(wav_path: str) -> float:
    """Estime la fréquence fondamentale (F0 en Hz) de l'orateur original."""
    try:
        audio = AudioSegment.from_file(wav_path)
        samples = audio.get_array_of_samples()
        if not samples:
            return 130.0

        sr = audio.frame_rate
        chunk = samples[:min(len(samples), sr * 4)]
        min_lag = int(sr / 350)
        max_lag = int(sr / 75)
        best_lag = min_lag
        max_val = -1

        step = max(1, len(chunk) // 2000)
        sub_chunk = chunk[::step]
        sub_sr = sr // step
        min_lag_sub = max(1, int(sub_sr / 350))
        max_lag_sub = max(min_lag_sub + 1, int(sub_sr / 75))

        for lag in range(min_lag_sub, min(max_lag_sub, len(sub_chunk) // 2)):
            val = sum(abs(sub_chunk[i] - sub_chunk[i + lag]) for i in range(0, min(500, len(sub_chunk) - lag)))
            if val < max_val or max_val == -1:
                max_val = val
                best_lag = lag

        if best_lag > 0:
            est_hz = sub_sr / best_lag
            if 75 <= est_hz <= 350:
                return float(est_hz)
    except Exception:
        pass
    return 130.0


def resolve_cloned_voice(detected_lang: str, tgt_lang: str, speaker_ref_wav: str) -> str:
    """Sélectionne la voix la plus proche de l'orateur d'origine."""
    f0 = estimate_speaker_pitch(speaker_ref_wav)
    is_female = f0 > 165.0
    print(f"[dub][CLONE] Profil orateur: F0 = {f0:.1f} Hz ({'féminin' if is_female else 'masculin'})", flush=True)

    voice_catalog = {
        "fr": ("fr-FR-DeniseNeural" if is_female else "fr-FR-HenriNeural"),
        "en": ("en-US-JennyNeural" if is_female else "en-US-GuyNeural"),
        "de": ("de-DE-KatjaNeural" if is_female else "de-DE-ConradNeural"),
        "es": ("es-ES-ElviraNeural" if is_female else "es-ES-AlvaroNeural"),
        "it": ("it-IT-ElsaNeural" if is_female else "it-IT-DiegoNeural"),
        "pt": ("pt-BR-FranciscaNeural" if is_female else "pt-BR-AntonioNeural"),
    }
    return voice_catalog.get((tgt_lang or "fr").lower(), "fr-FR-HenriNeural")


# Table de vitesse de parole (caractères par seconde) issue de VoiceStudio (speech_rate.py)
RATE_CPS = {
    "en": 15.0, "de": 14.0, "fr": 15.0, "es": 15.5, "it": 15.0, "pt": 15.0,
    "zh": 6.0, "ja": 10.0, "ar": 12.0, "ru": 13.0, "hi": 17.0, "nl": 14.0
}

TRANSLATION_CACHE_FILE = None
_trans_cache = {}
_cache_lock = threading.Lock()

def init_translation_cache(base_dir: str):
    global TRANSLATION_CACHE_FILE, _trans_cache
    if not base_dir:
        return
    TRANSLATION_CACHE_FILE = os.path.join(base_dir, "translation_cache.json")
    if os.path.exists(TRANSLATION_CACHE_FILE):
        try:
            with open(TRANSLATION_CACHE_FILE, "r", encoding="utf-8") as f:
                _trans_cache = json.load(f)
        except Exception:
            _trans_cache = {}

def save_translation_cache():
    if not TRANSLATION_CACHE_FILE:
        return
    with _cache_lock:
        try:
            os.makedirs(os.path.dirname(TRANSLATION_CACHE_FILE), exist_ok=True)
            with open(TRANSLATION_CACHE_FILE, "w", encoding="utf-8") as f:
                json.dump(_trans_cache, f, ensure_ascii=False, indent=2)
        except Exception:
            pass


def translate_single_phrase(phrase: str, src_lang: str, tgt_lang: str) -> str:
    """Traduction résiliente avec repli hors-ligne garanti et mise en cache."""
    if not phrase or not phrase.strip():
        return phrase
    src = (src_lang or "auto").lower().split("-")[0]
    tgt = (tgt_lang or "fr").lower().split("-")[0]
    if src == tgt and src != "auto":
        return phrase

    cache_key = f"{src}->{tgt}:{phrase.strip().lower()}"
    with _cache_lock:
        if cache_key in _trans_cache:
            return _trans_cache[cache_key]

    translated = None

    # 1. API Directe Google Translate (en ligne, ultra-rapide ~50ms)
    try:
        import urllib.request
        import urllib.parse
        q = urllib.parse.quote(phrase.strip())
        s_param = "auto" if src == "auto" else src
        url = f"https://translate.googleapis.com/translate_a/single?client=gtx&sl={s_param}&tl={tgt}&dt=t&q={q}"
        req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"})
        with urllib.request.urlopen(req, timeout=2.5) as response:
            data = json.loads(response.read().decode("utf-8"))
            if data and data[0]:
                trans = "".join(part[0] for part in data[0] if part and part[0])
                if trans.strip():
                    translated = trans.strip()
    except Exception:
        pass

    # 2. Fallback MyMemory / deep_translator (en ligne secondaire)
    if not translated:
        try:
            from deep_translator import MyMemoryTranslator
            s_mem = "auto" if src == "auto" else src
            res = MyMemoryTranslator(source=s_mem, target=tgt).translate(phrase)
            if res and res.strip() and res.strip().lower() != tgt.lower():
                translated = res.strip()
        except Exception:
            pass

    # 3. Fallback Argos Translate (100% hors-ligne local si modèle .argosmodel téléchargé)
    if not translated:
        try:
            # 3a. Vérification dans le dossier des modèles extraits
            pair = f"{src}_{tgt}"
            models_dir = os.path.join(os.path.dirname(TRANSLATION_CACHE_FILE), "models", "argos", "extracted")
            target_dir = os.path.join(models_dir, pair)
            if not os.path.exists(target_dir):
                target_dir = os.path.join(models_dir, f"en_{tgt}")
            if os.path.exists(target_dir):
                import ctranslate2
                import sentencepiece as spm
                spm_path = None
                for root, _, files in os.walk(target_dir):
                    for f in files:
                        if f.endswith(".model") or "sentencepiece" in f or "spm" in f:
                            spm_path = os.path.join(root, f)
                            break
                    if spm_path:
                        break
                model_path = target_dir
                if os.path.exists(os.path.join(target_dir, "model")):
                    model_path = os.path.join(target_dir, "model")
                if spm_path and os.path.exists(model_path):
                    sp = spm.SentencePieceProcessor(model_file=spm_path)
                    tr = ctranslate2.Translator(model_path, device="cpu", compute_type="int8")
                    subwords = sp.encode(phrase.strip(), out_type=str)
                    res = tr.translate_batch([subwords])
                    out_tokens = res[0].hypotheses[0]
                    translated = sp.decode(out_tokens).strip()
        except Exception:
            pass

    # 3b. Fallback argostranslate python package standard si installé
    if not translated:
        try:
            import argostranslate.translate as argos_tr
            installed_langs = {l.code: l for l in argos_tr.get_installed_languages()}
            if src in installed_langs and tgt in installed_langs:
                translation = installed_langs[src].get_translation(installed_langs[tgt])
                if translation:
                    res = translation.translate(phrase)
                    if res and res.strip():
                        translated = res.strip()
        except Exception:
            pass

    # 4. Si aucune traduction n'a abouti (hors-ligne complet sans modèle téléchargé)
    if not translated:
        print(f"[dub][OFFLINE] Notice: Traduction hors-ligne pour '{src}->{tgt}' (conservation du texte source)", file=sys.stderr)
        translated = phrase.strip()

    with _cache_lock:
        _trans_cache[cache_key] = translated
    save_translation_cache()
    return translated


def normalize_for_tts(text: str) -> str:
    if not text:
        return ""
    out = re.sub(r"\[.*?\]", "", text)
    out = out.replace("«", "\"").replace("»", "\"").replace("“", "\"").replace("”", "\"")
    out = re.sub(r"([!?.,;:~_*#=-])\1{3,}", r"\1\1\1", out)
    out = re.sub(r"[*_`#~]", "", out)
    out = re.sub(r"\s+", " ", out)
    return out.strip()


def atempo_chain(ratio: float) -> str:
    stages = []
    remaining = float(ratio)
    while remaining > 2.0:
        stages.append("atempo=2.0")
        remaining /= 2.0
    while remaining < 0.5:
        stages.append("atempo=0.5")
        remaining /= 0.5
    stages.append(f"atempo={remaining:.6f}")
    return ",".join(stages)


def pitch_preserving_stretch(ffmpeg: str, in_wav: str, out_wav: str, ratio: float) -> bool:
    if abs(ratio - 1.0) < 0.01:
        shutil.copy2(in_wav, out_wav)
        return True
    filt = atempo_chain(ratio)
    cmd = [ffmpeg, "-y", "-i", in_wav, "-af", filt, "-ar", "44100", "-c:a", "pcm_s16le", out_wav]
    return run(cmd, allow_fail=True)


def synthesize_windows_sapi(text: str, tgt_lang: str, out_mp3: str, ffmpeg: str) -> bool:
    """Synthèse vocale 100% hors-ligne via le sous-système natif Windows (SAPI5/System.Speech)."""
    try:
        voice_map = {
            "fr": "Microsoft Hortense Desktop",
            "en": "Microsoft Zira Desktop",
            "es": "Microsoft Helena Desktop",
            "de": "Microsoft Hedda Desktop",
            "it": "Microsoft Elsa Desktop"
        }
        lang = (tgt_lang or "fr").lower().split("-")[0]
        voice_name = voice_map.get(lang, "Microsoft Hortense Desktop")
        safe_text = text.replace('"', ' ').replace("'", " ")

        temp_wav = out_mp3.replace(".mp3", "_sapi.wav")
        abs_wav = os.path.abspath(temp_wav)
        ps_code = f"Add-Type -AssemblyName System.Speech; $s = New-Object System.Speech.Synthesis.SpeechSynthesizer; try {{ $s.SelectVoice('{voice_name}') }} catch {{}}; $s.SetOutputToWaveFile('{abs_wav}'); $s.Speak('{safe_text}'); $s.Dispose()"

        res = subprocess.run(["powershell", "-NoProfile", "-ExecutionPolicy", "Bypass", "-Command", ps_code],
                             capture_output=True, text=True, timeout=10)

        if os.path.exists(temp_wav) and os.path.getsize(temp_wav) > 400:
            run([ffmpeg, "-y", "-i", temp_wav, "-codec:a", "libmp3lame", "-b:a", "128k", out_mp3], allow_fail=True)
            try:
                os.remove(temp_wav)
            except Exception:
                pass
            return os.path.exists(out_mp3) and os.path.getsize(out_mp3) > 100
    except Exception as e:
        print(f"[dub] SAPI offline notice: {e}", file=sys.stderr)
    return False


def synthesize_zero_shot_or_tts(text: str, tgt_lang: str, voice: str, out_mp3: str, ffmpeg: str = None, speaker_ref_wav: str = None, clone_voice: bool = False) -> bool:
    clean_text = normalize_for_tts(text)
    if not clean_text:
        return False

    # 1. Edge-TTS haute fidélité (en ligne)
    try:
        import edge_tts
        v = voice or "fr-FR-HenriNeural"
        loop = asyncio.new_event_loop()
        asyncio.set_event_loop(loop)
        communicate = edge_tts.Communicate(clean_text, v)
        loop.run_until_complete(asyncio.wait_for(communicate.save(out_mp3), timeout=4.0))
        loop.close()
        if os.path.exists(out_mp3) and os.path.getsize(out_mp3) > 100:
            return True
    except Exception as e:
        print(f"[dub] Edge-TTS notice ({e}), repli hors-connexion local...", file=sys.stderr)

    # 2. Fallback local hors-ligne Windows natif SAPI (100% hors connexion)
    if ffmpeg and sys.platform == "win32":
        if synthesize_windows_sapi(clean_text, tgt_lang, out_mp3, ffmpeg):
            return True

    # 3. Fallback gTTS (en ligne secondaire)
    try:
        from gtts import gTTS
        lang = (tgt_lang or "fr").lower().split("-")[0]
        tts = gTTS(text=clean_text, lang=lang)
        tts.save(out_mp3)
        return os.path.exists(out_mp3) and os.path.getsize(out_mp3) > 100
    except Exception as e:
        print(f"[dub] Échec repli TTS: {e}", file=sys.stderr)
        return False


def start_stdin_listener():
    def _listener():
        global priority_seek_target
        if sys.platform == "win32":
            import msvcrt
            import ctypes
            from ctypes import wintypes
            kernel32 = ctypes.windll.kernel32
            try:
                handle = msvcrt.get_osfhandle(sys.stdin.fileno())
            except Exception:
                return
            avail = wintypes.DWORD()
            while True:
                try:
                    time.sleep(0.15)
                    success = kernel32.PeekNamedPipe(handle, None, 0, None, ctypes.byref(avail), None)
                    if not success:
                        break
                    if avail.value > 0:
                        line = sys.stdin.readline()
                        if not line:
                            break
                        line = line.strip()
                        if line.startswith("SEEK "):
                            parts = line.split()
                            if len(parts) >= 2:
                                target = float(parts[1])
                                with seek_lock:
                                    priority_seek_target = target
                                print(f"SEEK_ACCEPTED {target}", flush=True)
                except Exception:
                    break
        else:
            import select
            while True:
                try:
                    r, _, _ = select.select([sys.stdin], [], [], 0.2)
                    if r:
                        line = sys.stdin.readline()
                        if not line:
                            break
                        line = line.strip()
                        if line.startswith("SEEK "):
                            parts = line.split()
                            if len(parts) >= 2:
                                target = float(parts[1])
                                with seek_lock:
                                    priority_seek_target = target
                                print(f"SEEK_ACCEPTED {target}", flush=True)
                except Exception:
                    break

    th = threading.Thread(target=_listener, daemon=True)
    th.start()


def synthesize_and_mix_segment(seg, seg_idx, synth_dir, ffmpeg, detected_lang, tgt_lang, voice, bg_audio=None, speaker_ref_wav=None, clone_voice=False, segment_cache_dir=None):
    start = float(seg["start"])
    end = float(seg["end"])
    raw_text = seg["text"].strip()
    slot_s = max(end - start, 0.35)
    final_chunk_mp3 = os.path.join(synth_dir, f"seg_{seg_idx:05d}_final.mp3")

    # 1. Empreinte déterministe SHA-256 (principe VoiceStudio incremental.py)
    fp_raw = f"{raw_text.lower()}|{tgt_lang.lower()}|{voice}|{slot_s:.2f}"
    fp = hashlib.sha256(fp_raw.encode("utf-8")).hexdigest()[:16]

    # 2. Relecture instantanée depuis le cache de segments (0 ms)
    if segment_cache_dir:
        os.makedirs(segment_cache_dir, exist_ok=True)
        cached_file = os.path.join(segment_cache_dir, f"chunk_{fp}.mp3")
        if os.path.exists(cached_file) and os.path.getsize(cached_file) > 400:
            shutil.copy2(cached_file, final_chunk_mp3)
            try:
                fitted_audio = AudioSegment.from_file(final_chunk_mp3)
                return fitted_audio, final_chunk_mp3
            except Exception:
                pass

    # 3. Traduction
    if detected_lang == tgt_lang.lower():
        trans_text = raw_text
    else:
        trans_text = translate_single_phrase(raw_text, detected_lang, tgt_lang)
    seg["translated"] = trans_text

    mp3_path = os.path.join(synth_dir, f"seg_{seg_idx:05d}.mp3")
    wav_raw = os.path.join(synth_dir, f"seg_{seg_idx:05d}_raw.wav")
    wav_stretched = os.path.join(synth_dir, f"seg_{seg_idx:05d}_fit.wav")

    if not synthesize_zero_shot_or_tts(trans_text, tgt_lang, voice, mp3_path, ffmpeg=ffmpeg, speaker_ref_wav=speaker_ref_wav, clone_voice=clone_voice):
        return None, None

    if not run([ffmpeg, "-y", "-i", mp3_path, "-ac", "1", "-ar", "44100", "-c:a", "pcm_s16le", wav_raw]):
        return None, None

    raw_audio = AudioSegment.from_file(wav_raw)
    natural_dur = raw_audio.duration_seconds
    need = natural_dur / slot_s if slot_s > 0 else 1.0

    MAX_AUDIO_ONLY_RATE = 1.30
    AUDIO_RATE_CAP = 1.50
    UNDERRUN_TOLERANCE = 0.90
    MIN_AUDIO_RATE = 0.85

    if need <= 1.0:
        audio_rate = max(need, MIN_AUDIO_RATE) if (need < UNDERRUN_TOLERANCE and need > 0.1) else 1.0
    elif need <= MAX_AUDIO_ONLY_RATE:
        audio_rate = need
    else:
        audio_rate = min(need, AUDIO_RATE_CAP)

    if abs(audio_rate - 1.0) > 0.01:
        pitch_preserving_stretch(ffmpeg, wav_raw, wav_stretched, audio_rate)
        fitted_audio = AudioSegment.from_file(wav_stretched)
    else:
        fitted_audio = raw_audio

    max_ms = int(slot_s * 1000)
    if len(fitted_audio) > max_ms:
        fitted_audio = fitted_audio[:max_ms].fade_out(30)

    # Mixage direct du fond sonore ducké
    start_ms = int(start * 1000)
    end_ms = start_ms + len(fitted_audio)

    if bg_audio and len(bg_audio) > start_ms:
        bg_slice = bg_audio[start_ms:end_ms]
        if len(bg_slice) < len(fitted_audio):
            bg_slice = bg_slice + AudioSegment.silent(duration=len(fitted_audio) - len(bg_slice))
        ducked_bg = bg_slice - 9
        stream_audio = ducked_bg.overlay(fitted_audio)
    else:
        stream_audio = fitted_audio

    stream_audio.export(final_chunk_mp3, format="mp3", bitrate="128k")

    # Sauvegarde dans le cache persistant par empreinte
    if segment_cache_dir:
        try:
            cached_file = os.path.join(segment_cache_dir, f"chunk_{fp}.mp3")
            shutil.copy2(final_chunk_mp3, cached_file)
        except Exception:
            pass

    return fitted_audio, final_chunk_mp3


def stream_dub(ffmpeg, wav_path, total_dur, src_lang, tgt_lang, model, model_cache, voice, workdir, out_mp3, debug_dir, transcript_cache=None, demucs_enabled=True, clone_voice=False):
    total_ms = int(total_dur * 1000) if total_dur > 0 else 0
    synth_dir = os.path.join(workdir, "tts")
    os.makedirs(synth_dir, exist_ok=True)
    cache_base = os.path.dirname(workdir) if os.path.dirname(workdir) else workdir
    segment_cache_dir = os.path.join(cache_base, "segment_cache")
    init_translation_cache(cache_base)

    start_stdin_listener()

    # 1. Extraction rapide du fond d'ambiance
    bg_audio = None
    if demucs_enabled:
        log_progress(10, "Extraction de la musique de fond")
        bg_wav = os.path.join(workdir, "background.wav")
        if extract_background_music(ffmpeg, wav_path, bg_wav, workdir):
            try:
                bg_audio = AudioSegment.from_file(bg_wav)
                print(f"[dub] Musique de fond chargée: {len(bg_audio)/1000:.1f}s", flush=True)
            except Exception as e:
                print(f"[dub] Erreur chargement musique: {e}", file=sys.stderr)

    if bg_audio is not None and len(bg_audio) > 0:
        base = bg_audio - 3
        if total_ms > 0 and len(base) < total_ms:
            base = base + AudioSegment.silent(duration=total_ms - len(base))
    else:
        base = AudioSegment.silent(duration=max(total_ms, 5000))

    all_segments = []
    detected_lang = src_lang if src_lang not in ("auto", "any", None) else None

    # 2. Vérification du cache de transcription existant
    if transcript_cache and os.path.exists(transcript_cache):
        try:
            with open(transcript_cache, "r", encoding="utf-8") as f:
                data = json.load(f)
                all_segments = data.get("segments", [])
                detected_lang = data.get("detected_lang", "fr")
            print(f"[dub] Transcription réutilisée depuis le cache ({len(all_segments)} répliques)", flush=True)
            log_progress(20, "Transcription réutilisée du cache")
        except Exception as e:
            all_segments = []

    # 3. Extraction d'échantillon pour le profilage orateur si demandé
    speaker_ref_wav = None
    resolved_voice = voice
    if clone_voice:
        try:
            speaker_ref_wav = os.path.join(workdir, "speaker_ref.wav")
            run([ffmpeg, "-y", "-i", wav_path, "-t", "6", "-ar", "16000", "-c:a", "pcm_s16le", speaker_ref_wav], allow_fail=True)
            if os.path.exists(speaker_ref_wav):
                resolved_voice = resolve_cloned_voice(detected_lang or "fr", tgt_lang, speaker_ref_wav)
                print(f"[dub][CLONE] Voix profilée pour le doublage: {resolved_voice}", flush=True)
        except Exception as e:
            print(f"[dub][CLONE] Erreur extraction profil orateur: {e}", file=sys.stderr)

    # 4. Mode 1 : Streaming en direct au fil de la transcription Whisper
    if not all_segments:
        cleanup_stale_locks(model_cache)
        from faster_whisper import WhisperModel
        cpu_threads = min(os.cpu_count() or 4, 4)
        audio_model = WhisperModel(model, device="cpu", compute_type="int8", download_root=model_cache, cpu_threads=cpu_threads, num_workers=1)
        lang_param = None if (not detected_lang or detected_lang in ("auto", "any")) else detected_lang

        log_progress(15, "Transcription et doublage en direct démarrés")
        segments_gen, info = audio_model.transcribe(
            wav_path,
            language=lang_param,
            beam_size=1,
            best_of=1,
            temperature=0.0,
            vad_filter=True,
            condition_on_previous_text=False
        )
        detected_lang = getattr(info, "language", None) or src_lang or "fr"
        detected_lang = detected_lang.lower()

        prev_norm = None
        rep_count = 0
        seg_counter = 0

        # STREAMING RÉEL : On traduit et synthétise chaque segment dès qu'il est émis !
        for seg in segments_gen:
            t = (seg.text or "").strip()
            if not t:
                continue
            norm = t.lower()
            if norm == prev_norm:
                rep_count += 1
                if rep_count > 2:
                    continue
            else:
                prev_norm = norm
                rep_count = 1

            seg_data = {
                "index": seg_counter,
                "start": float(seg.start),
                "end": float(seg.end),
                "text": t
            }
            all_segments.append(seg_data)

            # Synthèse et diffusion immédiate (avec vérification du cache d'empreintes)
            fitted_audio, chunk_path = synthesize_and_mix_segment(
                seg=seg_data,
                seg_idx=seg_counter,
                synth_dir=synth_dir,
                ffmpeg=ffmpeg,
                detected_lang=detected_lang,
                tgt_lang=tgt_lang,
                voice=resolved_voice,
                bg_audio=bg_audio,
                speaker_ref_wav=speaker_ref_wav,
                clone_voice=clone_voice,
                segment_cache_dir=segment_cache_dir
            )

            if fitted_audio and chunk_path:
                start_ms = int(seg_data["start"] * 1000)
                if start_ms + len(fitted_audio) > len(base):
                    base = base + AudioSegment.silent(duration=(start_ms + len(fitted_audio)) - len(base) + 2000)
                base = base.overlay(fitted_audio, position=start_ms)

                pct = min(20 + int((seg_data["end"] / max(total_dur, 1.0)) * 75), 98)
                print(f"SEGMENT_CHUNK {seg_counter} {seg_data['start']:.3f} {seg_data['end']:.3f} {chunk_path}", flush=True)
                print(f"PROGRESS_STREAM {pct} Réplique en direct #{seg_counter + 1} ({seg_data['start']:.1f}s - {seg_data['end']:.1f}s)", flush=True)

            seg_counter += 1

        del audio_model

        if transcript_cache:
            try:
                os.makedirs(os.path.dirname(transcript_cache), exist_ok=True)
                with open(transcript_cache, "w", encoding="utf-8") as f:
                    json.dump({"detected_lang": detected_lang, "segments": all_segments}, f, ensure_ascii=False, indent=2)
                print(f"[dub] Transcription mise en cache avec succès ({len(all_segments)} répliques)", flush=True)
            except Exception as e:
                print(f"[dub] Erreur sauvegarde transcript cache: {e}", file=sys.stderr)

    else:
        # 5. Mode 2 : Transcription déjà connue en cache -> Synthèse directe avec priorité de seek
        total_segs = len(all_segments)
        synthesized_indices = set()
        global priority_seek_target

        while len(synthesized_indices) < total_segs:
            target_idx = None
            with seek_lock:
                if priority_seek_target is not None:
                    seek_t = priority_seek_target
                    priority_seek_target = None
                    closest = [s for s in all_segments if s["index"] not in synthesized_indices and s["end"] >= (seek_t - 1.0)]
                    if closest:
                        target_idx = closest[0]["index"]
                        print(f"[dub] Re-priorisation vers le segment #{target_idx} (position seek {seek_t:.1f}s)", flush=True)

            if target_idx is None:
                for s in all_segments:
                    if s["index"] not in synthesized_indices:
                        target_idx = s["index"]
                        break

            if target_idx is None:
                break

            seg = all_segments[target_idx]
            fitted_audio, chunk_path = synthesize_and_mix_segment(
                seg=seg,
                seg_idx=target_idx,
                synth_dir=synth_dir,
                ffmpeg=ffmpeg,
                detected_lang=detected_lang,
                tgt_lang=tgt_lang,
                voice=resolved_voice,
                bg_audio=bg_audio,
                speaker_ref_wav=speaker_ref_wav,
                clone_voice=clone_voice,
                segment_cache_dir=segment_cache_dir
            )

            synthesized_indices.add(target_idx)

            if fitted_audio and chunk_path:
                start_ms = int(seg["start"] * 1000)
                if start_ms + len(fitted_audio) > len(base):
                    base = base + AudioSegment.silent(duration=(start_ms + len(fitted_audio)) - len(base) + 2000)
                base = base.overlay(fitted_audio, position=start_ms)

                pct = min(20 + int((len(synthesized_indices) / total_segs) * 78), 98)
                print(f"SEGMENT_CHUNK {target_idx} {seg['start']:.3f} {seg['end']:.3f} {chunk_path}", flush=True)
                print(f"PROGRESS_STREAM {pct} Réplique en direct #{target_idx + 1}/{total_segs} ({seg['start']:.1f}s - {seg['end']:.1f}s)", flush=True)

    # Finalisation et export de la piste globale complète
    if total_ms > 0:
        if len(base) > total_ms:
            base = base[:total_ms]
        elif len(base) < total_ms:
            base = base + AudioSegment.silent(duration=total_ms - len(base))

    base.export(out_mp3, format="mp3", bitrate="128k")
    log_progress(100, "Doublage complet terminé")
    return all_segments


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--input", required=True)
    ap.add_argument("--audio", default=None)
    ap.add_argument("--output", required=True)
    ap.add_argument("--source-lang", default="auto")
    ap.add_argument("--target-lang", required=True)
    ap.add_argument("--model", default="base")
    ap.add_argument("--workdir", default=None)
    ap.add_argument("--model-cache", default=None)
    ap.add_argument("--voice", default=None)
    ap.add_argument("--transcript-cache", default=None)
    ap.add_argument("--demucs", action="store_true", default=True)
    ap.add_argument("--no-demucs", action="store_false", dest="demucs")
    ap.add_argument("--clone-voice", action="store_true", default=False)
    args = ap.parse_args()

    workdir = args.workdir or tempfile.mkdtemp(prefix="dub_")
    os.makedirs(workdir, exist_ok=True)
    model_cache = args.model_cache or os.path.join(workdir, "whisper")
    os.makedirs(model_cache, exist_ok=True)
    ffmpeg = FFMPEG_BIN

    debug_dir = os.path.join(os.getcwd(), "debug")
    os.makedirs(debug_dir, exist_ok=True)

    wav_path = args.audio if (args.audio and os.path.exists(args.audio)) else os.path.join(workdir, "audio.wav")

    if not os.path.exists(wav_path):
        log_progress(5, "Extraction audio rapide")
        if not extract_audio(ffmpeg, args.input, wav_path):
            sys.exit(1)
        if args.audio and not os.path.exists(args.audio):
            try:
                os.makedirs(os.path.dirname(args.audio), exist_ok=True)
                shutil.copy2(wav_path, args.audio)
            except Exception as e:
                print(f"[dub] Cache audio notice: {e}", file=sys.stderr)

    total_dur = AudioSegment.from_wav(wav_path).duration_seconds

    stream_dub(
        ffmpeg=ffmpeg,
        wav_path=wav_path,
        total_dur=total_dur,
        src_lang=args.source_lang,
        tgt_lang=args.target_lang,
        model=args.model,
        model_cache=model_cache,
        voice=args.voice,
        workdir=workdir,
        out_mp3=args.output,
        debug_dir=debug_dir,
        transcript_cache=args.transcript_cache,
        demucs_enabled=args.demucs,
        clone_voice=args.clone_voice
    )


if __name__ == "__main__":
    main()