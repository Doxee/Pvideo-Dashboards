package com.doxee.pvideo.dashboard.commons.json;

import java.util.ArrayList;
import java.util.List;

public class FirstDayDTO {

    private List<Integer> opened;

    private List<Integer> views;

    private List<Integer> conversions;

    public FirstDayDTO() {
        opened = new ArrayList<>();
        views = new ArrayList<>();
        conversions = new ArrayList<>();
        for (int ii = 0; ii < 24; ii++) {
            opened.add(0);
            views.add(0);
            conversions.add(0);
        }
    }

    public List<Integer> getOpened() {
        return opened;
    }

    public void setOpened(List<Integer> opened) {
        this.opened = opened;
    }

    public List<Integer> getViews() {
        return views;
    }

    public void setViews(List<Integer> views) {
        this.views = views;
    }

    public List<Integer> getConversions() {
        return conversions;
    }

    public void setConversions(List<Integer> conversions) {
        this.conversions = conversions;
    }
}
