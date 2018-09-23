package com.doxee.pvideo.dashboard.commons.json;

public class InteractionDTO {

    // titolo conversione, numero di eventi unici, %rispetto eventi totali= eventi unici / eventi totali
    private String conversionEventName;

    private int uniqueEvent;

    private double uniqueEventRate;

    public String getConversionEventName() {
        return conversionEventName;
    }

    public void setConversionEventName(String conversionEventName) {
        this.conversionEventName = conversionEventName;
    }

    public int getUniqueEvent() {
        return uniqueEvent;
    }

    public void setUniqueEvent(int uniqueEvent) {
        this.uniqueEvent = uniqueEvent;
    }

    public double getUniqueEventRate() {
        return uniqueEventRate;
    }

    public void setUniqueEventRate(double uniqueEventRate) {
        this.uniqueEventRate = uniqueEventRate;
    }

    @Override
    public String toString() {
        return "InteractionDTO{" + "conversionEventName='" + conversionEventName + '\'' + ", uniqueEvent=" + uniqueEvent + ", uniqueEventRate=" + uniqueEventRate + '}';
    }
}
