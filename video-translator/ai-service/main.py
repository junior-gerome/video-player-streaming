"""
Service IA 100 % open source :
  - faster-whisper (MIT)      -> transcription avec horodatage
  - Argos Translate (MIT/CC0) -> traduction hors ligne
  - Piper (GPL-3.0, appelé comme processus séparé) -> synthèse vocale
  - FFmpeg                    -> ajustement de la vitesse (atempo)
"""
import os
import subprocess
import sys
import tempfile
import threading
from pathlib import Path

import argostranslate.package as argos_pkg
import argostranslate.translate as argos_tr
from fastapi import FastAPI, HTTPException, UploadFile
from fastapi.responses import FileResponse
from faster_whisper import WhisperModel
from pydantic import BaseModel
from pydub import AudioSegment
from starlette.background import BackgroundTask

VOICES_DIR = Path(os.getenv("VOICES_DIR", "/voices"))
# langue cible -> voix Piper (doit exister dans VOICES_DIR)
VOICES = {
    "en": "en_US-lessac-medium",
    "fr": "fr_FR-siwis-medium",
    "es": "es_ES-davefx-medium",
    "de": "de_DE-thorsten-medium",
    "ar": "ar_JO-kareem-medium",
    "pt": "pt_BR-faber-medium",
}
MAX_SPEEDUP = 1.6  # au-delà, la voix devient peu naturelle : on laisse déborder

app = FastAPI(title="video-translator ai-service")
whisper = WhisperModel(
    os.getenv("WHISPER_MODEL", "small"),
    device=os.getenv("WHISPER_DEVICE", "cpu"),
    compute_type=os.getenv("WHISPER_COMPUTE", "int8"),
)
_install_lock = threading.Lock()


class Segment(BaseModel):
    start: float
    end: float
    text: str


class DubRequest(BaseModel):
    segments: list[Segment]
    source_lang: str
    target_lang: str
    total_ms: int


# ---------- traduction ----------
def _has_pair(src: str, tgt: str) -> bool:
    langs = {l.code: l for l in argos_tr.get_installed_languages()}
    return src in langs and tgt in langs and langs[src].get_translation(langs[tgt]) is not None


def _install_pair(src: str, tgt: str) -> bool:
    argos_pkg.update_package_index()
    for p in argos_pkg.get_available_packages():
        if (p.from_code, p.to_code) == (src, tgt):
            argos_pkg.install_from_path(p.download())
            return True
    return False


def ensure_pair(src: str, tgt: str) -> None:
    """Installe les paquets Argos nécessaires (pivot par l'anglais si besoin)."""
    if src == tgt or _has_pair(src, tgt):
        return
    with _install_lock:
        hops = [(src, tgt)] if "en" in (src, tgt) else [(src, "en"), ("en", tgt)]
        for a, b in hops:
            if not _has_pair(a, b) and not _install_pair(a, b):
                raise HTTPException(422, f"Aucun paquet de traduction {a} -> {b}")


# ---------- voix + calage temporel ----------
def synthesize(text: str, lang: str, out: Path) -> None:
    model = VOICES_DIR / f"{VOICES[lang]}.onnx"
    # Piper (GPL) est lancé comme processus séparé, jamais importé
    subprocess.run(
        [sys.executable, "-m", "piper", "-m", str(model), "-f", str(out)],
        input=text.encode("utf-8"), check=True, capture_output=True,
    )


def fit(src: Path, dst: Path, slot: float, limit: float) -> AudioSegment:
    """Accélère la voix pour tenir dans `slot` secondes (max x1.6), coupe à `limit`."""
    clip = AudioSegment.from_wav(src)
    dur, target = len(clip) / 1000, max(slot, 0.3)
    if dur > target:
        speed = min(dur / target, MAX_SPEEDUP)
        subprocess.run(
            ["ffmpeg", "-y", "-i", str(src), "-filter:a", f"atempo={speed:.3f}", str(dst)],
            check=True, capture_output=True,
        )
        clip = AudioSegment.from_wav(dst)
    return clip[: int(limit * 1000)].fade_out(50)


# ---------- API ----------
@app.get("/health")
def health():
    return {"status": "ok", "targets": sorted(VOICES)}


@app.post("/transcribe")
def transcribe(file: UploadFile):
    with tempfile.NamedTemporaryFile(suffix=".mp3") as tmp:
        tmp.write(file.file.read())
        tmp.flush()
        segments, info = whisper.transcribe(tmp.name, vad_filter=True)
        return {
            "language": info.language,
            "segments": [{"start": s.start, "end": s.end, "text": s.text.strip()} for s in segments],
        }


@app.post("/dub")
def dub(req: DubRequest):
    if req.target_lang not in VOICES:
        raise HTTPException(400, f"Langue cible non supportée : {req.target_lang}")
    ensure_pair(req.source_lang, req.target_lang)

    timeline = AudioSegment.silent(duration=req.total_ms, frame_rate=22050)
    with tempfile.TemporaryDirectory() as tmp:
        tmp = Path(tmp)
        for i, seg in enumerate(req.segments):
            text = seg.text if req.source_lang == req.target_lang else \
                argos_tr.translate(seg.text, req.source_lang, req.target_lang)
            if not text.strip():
                continue
            raw = tmp / f"{i}.wav"
            synthesize(text, req.target_lang, raw)
            next_start = req.segments[i + 1].start if i + 1 < len(req.segments) else req.total_ms / 1000
            clip = fit(raw, tmp / f"{i}_fit.wav", seg.end - seg.start, next_start - seg.start)
            timeline = timeline.overlay(clip, position=int(seg.start * 1000))

    out = tempfile.NamedTemporaryFile(suffix=".wav", delete=False)
    out.close()
    timeline.export(out.name, format="wav")
    return FileResponse(out.name, media_type="audio/wav", background=BackgroundTask(os.unlink, out.name))
