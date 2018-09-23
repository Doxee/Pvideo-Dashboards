package com.doxee.pvideo.dashboard.commons.json;

public class DeviceTypeDTO {

    private int mobile;

    private int desktop;

    private int tablet;

    private int other;

    public DeviceTypeDTO() {
        this.mobile = 0;
        this.desktop = 0;
        this.tablet = 0;
        this.other = 0;
    }

    public int getMobile() {
        return mobile;
    }

    public void setMobile(int mobile) {
        this.mobile = mobile;
    }

    public int getDesktop() {
        return desktop;
    }

    public void setDesktop(int desktop) {
        this.desktop = desktop;
    }

    public int getTablet() {
        return tablet;
    }

    public void setTablet(int tablet) {
        this.tablet = tablet;
    }

    public int getOther() {
        return other;
    }

    public void setOther(int other) {
        this.other = other;
    }
}
