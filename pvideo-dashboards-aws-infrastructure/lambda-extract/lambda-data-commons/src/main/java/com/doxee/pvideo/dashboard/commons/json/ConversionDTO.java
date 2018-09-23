package com.doxee.pvideo.dashboard.commons.json;

public class ConversionDTO {

    // titolo conversione, numero di eventi unici, %rispetto eventi totali= eventi unici / eventi totali
    private String conversionEventName;

    private int uniqueEvent;

    private int totalEvent;// not show

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

    public int getTotalEvent() {
        return totalEvent;
    }

    public void setTotalEvent(int totalEvent) {
        this.totalEvent = totalEvent;
    }

    @Override
    public String toString() {
        return "ConversionDTO{" + "conversionEventName='" + conversionEventName + '\'' + ", uniqueEvent=" + uniqueEvent + ", totalEvent=" + totalEvent + ", uniqueEventRate=" + uniqueEventRate + '}';
    }
}
