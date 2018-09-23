package com.doxee.pvideo.dashboard.commons.json;

import java.util.ArrayList;
import java.util.List;

public class EngagementDTO {

    private List<Integer> data;

    private List<Double> rate;

    public EngagementDTO() {
        List<Integer> data = new ArrayList<>();
        List<Double> rate = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            data.add(0);
            rate.add(0d);
        }
        this.data = data;
        this.rate = rate;
    }

    public List<Integer> getData() {
        return data;
    }

    public void setData(List<Integer> data) {
        this.data = data;
    }

    public List<Double> getRate() {
        return rate;
    }

    public void setRate(List<Double> rate) {
        this.rate = rate;
    }

    @Override
    public String toString() {
        return "EngagementDTO{" + "data=" + data + ", rate=" + rate + '}';
    }
}
