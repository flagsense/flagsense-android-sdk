package com.flagsense.android.request;

public class FlagVariation {
    private String flag;
    private String variation;
    private Long time;

    public FlagVariation(String flag, String variation) {
        this.time = System.currentTimeMillis();
        this.flag = flag;
        this.variation = variation;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public String getVariation() {
        return variation;
    }

    public void setVariation(String variation) {
        this.variation = variation;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }
}
