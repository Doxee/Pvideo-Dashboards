package com.doxee.pvideo.dashboard.commons.json;

public class VideoDTO {

    private long viewsUnique;

    private double viewsUser;

    private long interactions;

    private long conversions;

    private double conversionRate;

    public double interactionRate;

    private double viewsRate;

    private long views;

    public VideoDTO() {
        this.viewsUnique = 0;
        this.viewsUser = 0;
        this.interactions = 0;
        this.conversions = 0;
        this.conversionRate = 0;
        this.interactionRate = 0;
        this.viewsRate = 0;
        this.views = 0;
    }

    public long getViewsUnique() {
        return viewsUnique;
    }

    public void setViewsUnique(long viewsUnique) {
        this.viewsUnique = viewsUnique;
    }

    public double getViewsUser() {
        return viewsUser;
    }

    public void setViewsUser(double viewsUser) {
        this.viewsUser = viewsUser;
    }

    public long getInteractions() {
        return interactions;
    }

    public void setInteractions(long interactions) {
        this.interactions = interactions;
    }

    public long getConversions() {
        return conversions;
    }

    public void setConversions(long conversions) {
        this.conversions = conversions;
    }

    public double getConversionRate() {
        return conversionRate;
    }

    public void setConversionRate(double conversionRate) {
        this.conversionRate = conversionRate;
    }

    public double getViewsRate() {
        return viewsRate;
    }

    public void setViewsRate(double viewsRate) {
        this.viewsRate = viewsRate;
    }

    public long getViews() {
        return views;
    }

    public void setViews(long views) {
        this.views = views;
    }

    public double getInteractionRate() {
        return interactionRate;
    }

    public void setInteractionRate(double interactionRate) {
        this.interactionRate = interactionRate;
    }
}
