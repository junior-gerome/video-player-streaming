package com.example.vt;

import java.nio.file.Path;
import java.util.List;

public class TranslationJob {
    public enum Status { PENDING, EXTRACTING, TRANSCRIBING, DUBBING, MERGING, DONE, FAILED }

    private final String id;
    private final List<String> languages;
    private volatile Status status = Status.PENDING;
    private volatile String error;
    private volatile Path result;

    public TranslationJob(String id, List<String> languages) {
        this.id = id;
        this.languages = languages;
    }
    public String getId() { return id; }
    public List<String> getLanguages() { return languages; }
    public Status getStatus() { return status; }
    public void setStatus(Status s) { this.status = s; }
    public String getError() { return error; }
    public void setError(String e) { this.error = e; }
    public Path getResult() { return result; }
    public void setResult(Path p) { this.result = p; }
}
