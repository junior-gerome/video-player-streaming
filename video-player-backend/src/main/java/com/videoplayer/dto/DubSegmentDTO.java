package com.videoplayer.dto;

public class DubSegmentDTO {
    private int index;
    private double start;
    private double end;
    private String audioUrl;

    public DubSegmentDTO() {}

    public DubSegmentDTO(int index, double start, double end, String audioUrl) {
        this.index = index;
        this.start = start;
        this.end = end;
        this.audioUrl = audioUrl;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public double getStart() {
        return start;
    }

    public void setStart(double start) {
        this.start = start;
    }

    public double getEnd() {
        return end;
    }

    public void setEnd(double end) {
        this.end = end;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }
}
