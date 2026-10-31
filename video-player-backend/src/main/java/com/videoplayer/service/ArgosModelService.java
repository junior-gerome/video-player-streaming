package com.videoplayer.service;

import com.videoplayer.dto.ArgosModelDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class ArgosModelService {

    private static final Logger log = LoggerFactory.getLogger(ArgosModelService.class);

    @Value("${dub.temp-dir:./dub-tmp}")
    private String tempDir;

    @Value("${dub.python:./venv/Scripts/python.exe}")
    private String pythonPath;

    private final Map<String, Integer> downloadProgress = new ConcurrentHashMap<>();
    private final Set<String> activeDownloads = ConcurrentHashMap.newKeySet();

    private static final List<ModelDef> SUPPORTED_MODELS = List.of(
            new ModelDef("fr", "Français", "en", "translate-en_fr-1_9.argosmodel", "https://argos-net.com/v1/translate-en_fr-1_9.argosmodel", 89.2),
            new ModelDef("es", "Espagnol", "en", "translate-en_es-1_0.argosmodel", "https://argos-net.com/v1/translate-en_es-1_0.argosmodel", 84.5),
            new ModelDef("de", "Allemand", "en", "translate-en_de-1_3.argosmodel", "https://argos-net.com/v1/translate-en_de-1_3.argosmodel", 86.8),
            new ModelDef("it", "Italien", "en", "translate-en_it-1_0.argosmodel", "https://argos-net.com/v1/translate-en_it-1_0.argosmodel", 84.7)
    );

    public record ModelDef(String lang, String label, String fromLang, String fileName, String url, double estSizeMb) {}

    public Path getModelsDir() {
        Path p = Paths.get(tempDir, "models", "argos");
        try {
            Files.createDirectories(p);
        } catch (IOException e) {
            log.error("Impossible de créer le dossier des modèles Argos: {}", e.getMessage());
        }
        return p;
    }

    public List<ArgosModelDTO> getAvailableModels() {
        Path dir = getModelsDir();
        List<ArgosModelDTO> list = new ArrayList<>();

        for (ModelDef def : SUPPORTED_MODELS) {
            Path file = dir.resolve(def.fileName);
            boolean exists = Files.exists(file);
            double sizeMb = def.estSizeMb;
            if (exists) {
                try {
                    sizeMb = Math.round((Files.size(file) / (1024.0 * 1024.0)) * 10.0) / 10.0;
                } catch (IOException ignored) {}
            }
            boolean downloading = activeDownloads.contains(def.lang);
            int progress = downloadProgress.getOrDefault(def.lang, exists ? 100 : 0);

            list.add(new ArgosModelDTO(
                    def.lang,
                    def.label,
                    def.fromLang,
                    def.fileName,
                    def.url,
                    exists && sizeMb > 1.0,
                    sizeMb,
                    downloading,
                    progress
            ));
        }

        return list;
    }

    @Async
    public void downloadModelAsync(String targetLang) {
        String lang = (targetLang == null) ? "fr" : targetLang.trim().toLowerCase();
        Optional<ModelDef> opt = SUPPORTED_MODELS.stream().filter(m -> m.lang.equalsIgnoreCase(lang)).findFirst();
        if (opt.isEmpty()) {
            log.warn("Langue Argos non supportée pour le téléchargement: {}", targetLang);
            return;
        }

        ModelDef def = opt.get();
        if (activeDownloads.contains(lang)) {
            log.info("Téléchargement déjà en cours pour {}", lang);
            return;
        }

        activeDownloads.add(lang);
        downloadProgress.put(lang, 0);

        Path dir = getModelsDir();
        Path targetFile = dir.resolve(def.fileName);
        Path tempDownload = dir.resolve(def.fileName + ".tmp");

        try {
            log.info("Démarrage du téléchargement du paquet Argos {} depuis {}", def.fileName, def.url);
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(20))
                    .followRedirects(HttpClient.Redirect.ALWAYS)
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(def.url))
                    .header("User-Agent", "Mozilla/5.0 VideoPlayer/1.0")
                    .GET()
                    .build();

            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                long totalBytes = response.headers().firstValueAsLong("Content-Length").orElse((long) (def.estSizeMb * 1024 * 1024));
                long downloadedBytes = 0;

                try (InputStream in = response.body();
                     OutputStream out = new BufferedOutputStream(Files.newOutputStream(tempDownload))) {
                    byte[] buffer = new byte[65536];
                    int read;
                    long lastLog = System.currentTimeMillis();

                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                        downloadedBytes += read;

                        if (totalBytes > 0) {
                            int pct = (int) Math.min(99, (downloadedBytes * 100) / totalBytes);
                            downloadProgress.put(lang, pct);
                        }

                        if (System.currentTimeMillis() - lastLog > 2000) {
                            lastLog = System.currentTimeMillis();
                            log.info("Téléchargement {} : {} / {} Mo ({}%)", def.fileName,
                                    downloadedBytes / (1024 * 1024), totalBytes / (1024 * 1024), downloadProgress.get(lang));
                        }
                    }
                }

                Files.move(tempDownload, targetFile, StandardCopyOption.REPLACE_EXISTING);
                downloadProgress.put(lang, 100);
                log.info("Paquet Argos {} téléchargé avec succès ({} Mo)", def.fileName, Files.size(targetFile) / (1024 * 1024));

                // Décompression du paquet pour accès direct CTranslate2/SentencePiece
                extractModelPackage(targetFile, dir.resolve("extracted").resolve(def.fromLang + "_" + def.lang));

                // Si python argostranslate est disponible, installer également le package
                installWithArgosCli(targetFile);

            } else {
                log.error("Échec du téléchargement Argos {} HTTP {}", def.fileName, response.statusCode());
                Files.deleteIfExists(tempDownload);
            }
        } catch (Exception e) {
            log.error("Erreur lors du téléchargement du paquet Argos {}: {}", def.fileName, e.getMessage(), e);
            try { Files.deleteIfExists(tempDownload); } catch (Exception ignored) {}
        } finally {
            activeDownloads.remove(lang);
        }
    }

    public void downloadAllAsync() {
        for (ModelDef def : SUPPORTED_MODELS) {
            Path file = getModelsDir().resolve(def.fileName);
            if (!Files.exists(file) || !activeDownloads.contains(def.lang)) {
                downloadModelAsync(def.lang);
            }
        }
    }

    private void extractModelPackage(Path zipPath, Path destDir) {
        try {
            Files.createDirectories(destDir);
            try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipPath))) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    Path resolved = destDir.resolve(entry.getName()).normalize();
                    if (!resolved.startsWith(destDir)) {
                        continue;
                    }
                    if (entry.isDirectory()) {
                        Files.createDirectories(resolved);
                    } else {
                        Files.createDirectories(resolved.getParent());
                        Files.copy(zis, resolved, StandardCopyOption.REPLACE_EXISTING);
                    }
                    zis.closeEntry();
                }
            }
            log.info("Paquet Argos extrait avec succès dans {}", destDir);
        } catch (Exception e) {
            log.warn("Extraction du paquet Argos vers {} : {}", destDir, e.getMessage());
        }
    }

    private void installWithArgosCli(Path modelPath) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    pythonPath, "-c",
                    "import argostranslate.package as pkg; pkg.install_from_path(r'" + modelPath.toAbsolutePath() + "')"
            );
            pb.redirectErrorStream(true);
            Process p = pb.start();
            p.waitFor();
            log.info("Package Argos enregistré dans l'environnement Python.");
        } catch (Exception e) {
            log.debug("Enregistrement CLI Argos non critique: {}", e.getMessage());
        }
    }
}
