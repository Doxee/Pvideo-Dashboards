package com.doxee.pvideo.dashboard.commons.json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OperatingSystemDTO {

    public static final String OS_ANDROID_LABEL = "Android";

    public static final String OS_IOS_LABEL = "iOS";

    public static final String OS_WINDOWS_LABEL = "Windows";

    public static final String OS_MACOS_LABEL = "MacOS";

    public static final String OS_LINUX_LABEL = "Linux";

    public static final String OS_WINDOWS_PHONE = "Windows Phone";

    public static final String OS_OTHER = "Other";

    public static final String[] osLabel = { OS_ANDROID_LABEL, OS_IOS_LABEL, OS_WINDOWS_LABEL, OS_MACOS_LABEL, OS_LINUX_LABEL, OS_WINDOWS_PHONE, OS_OTHER };

    private List<Integer> data;

    private List<String> labels;

    public OperatingSystemDTO() {
        List<Integer> dataTemp = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            dataTemp.add(0);
        }
        this.data = dataTemp;// "data": [60, 20, 5, 0],
        this.labels = Arrays.asList(osLabel);// "label": ["Android", "iOS", "Windows", "MacOS","Other"]
    }

    public List<Integer> getData() {
        return data;
    }

    public void setData(List<Integer> data) {
        this.data = data;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    @Override
    public String toString() {
        return "OperatingSystemDTO{" + "data=" + data + ", label=" + labels + '}';
    }
}
