package com.videoplayer.dto;

public class ArgosModelDTO {
    private String lang;
    private String label;
    private String fromLang;
    private String fileName;
    private String downloadUrl;
    private boolean installed;
    private double sizeMb;
    private boolean downloading;
    private int progress;

    public ArgosModelDTO() {}

    public ArgosModelDTO(String lang, String label, String fromLang, String fileName, String downloadUrl, boolean installed, double sizeMb, boolean downloading, int progress) {
        this.lang = lang;
        this.label = label;
        this.fromLang = fromLang;
        this.fileName = fileName;
        this.downloadUrl = downloadUrl;
        this.installed = installed;
        this.sizeMb = sizeMb;
        this.downloading = downloading;
        this.progress = progress;
    }

    public String getLang() { return lang; }
    public void setLang(String lang) { this.lang = lang; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getFromLang() { return fromLang; }
    public void setFromLang(String fromLang) { this.fromLang = fromLang; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getDownloadUrl() { return downloadUrl; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }

    public boolean isInstalled() { return installed; }
    public void setInstalled(boolean installed) { this.installed = installed; }

    public double getSizeMb() { return sizeMb; }
    public void setSizeMb(double sizeMb) { this.sizeMb = sizeMb; }

    public boolean isDownloading() { return downloading; }
    public void setDownloading(boolean downloading) { this.downloading = downloading; }

    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }
}
