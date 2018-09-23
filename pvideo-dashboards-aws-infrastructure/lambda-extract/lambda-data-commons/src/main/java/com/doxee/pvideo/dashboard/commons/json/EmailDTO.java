package com.doxee.pvideo.dashboard.commons.json;

public class EmailDTO {

    private long sent;

    private long opened;

    private double openRate;

    private long delivered;

    private long bounces;

    private double bounceRate;

    private long clicks;// purl clicked

    private double clickRate;

    private long purlProduced;// purl produced

    public EmailDTO() {
        this.sent = 0;
        this.opened = 0;
        this.openRate = 0;
        this.delivered = 0;
        this.bounces = 0;
        this.bounceRate = 0;
        this.clicks = 0;
        this.clickRate = 0;
        this.purlProduced = 0;
    }

    public long getSent() {
        return sent;
    }

    public void setSent(long sent) {
        this.sent = sent;
    }

    public long getOpened() {
        return opened;
    }

    public void setOpened(long opened) {
        this.opened = opened;
    }

    public double getOpenRate() {
        return openRate;
    }

    public void setOpenRate(double openRate) {
        this.openRate = openRate;
    }

    public long getDelivered() {
        return delivered;
    }

    public void setDelivered(long delivered) {
        this.delivered = delivered;
    }

    public long getBounces() {
        return bounces;
    }

    public void setBounces(long bounces) {
        this.bounces = bounces;
    }

    public double getBounceRate() {
        return bounceRate;
    }

    public void setBounceRate(double bounceRate) {
        this.bounceRate = bounceRate;
    }

    public long getClicks() {
        return clicks;
    }

    public void setClicks(long clicks) {
        this.clicks = clicks;
    }

    public double getClickRate() {
        return clickRate;
    }

    public void setClickRate(double clickRate) {
        this.clickRate = clickRate;
    }

    public long getPurlProduced() {
        return purlProduced;
    }

    public void setPurlProduced(long purlProduced) {
        this.purlProduced = purlProduced;
    }

    @Override
    public String toString() {
        return "EmailDTO{" + "sent=" + sent + ", opened=" + opened + ", openRate=" + openRate + ", delivered=" + delivered + ", bounces=" + bounces + ", bounceRate=" + bounceRate + ", clicks="
                + clicks + ", clickRate=" + clickRate + ", purlProduced=" + purlProduced + '}';
    }
}
