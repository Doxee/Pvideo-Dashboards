package com.doxee.pvideo.dashboard.aws.lambda.handler.utils;

import com.amazonaws.util.StringUtils;
import com.amazonaws.util.json.Jackson;
import com.doxee.pvideo.dashboard.aws.lambda.handler.exception.PvideoDashboardException;
import com.doxee.pvideo.dashboard.commons.json.*;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonUtils {

    private static Logger log = Logger.getLogger(JsonUtils.class);

    private JsonUtils() {
    }

    /**
     * merge twp metrics, put result in second method parameter
     * 
     * @param partialJsonDTO
     * @param completeJsonDTO
     * @return
     * @throws Exception
     */
    public static ExtractionDTO mergeContentData(ExtractionDTO partialJsonDTO, ExtractionDTO completeJsonDTO, String templateName) throws PvideoDashboardException {

        log.debug("|pvideo-dashboard| merge partial " + Jackson.toJsonPrettyString(partialJsonDTO));
        log.debug("|pvideo-dashboard| with complete " + Jackson.toJsonPrettyString(completeJsonDTO));

        if (completeJsonDTO != null && partialJsonDTO != null && !StringUtils.isNullOrEmpty(completeJsonDTO.getProcedureName()) && !StringUtils.isNullOrEmpty(partialJsonDTO.getProcedureName())
                && !completeJsonDTO.getProcedureName().equalsIgnoreCase(partialJsonDTO.getProcedureName())) {
            throw new PvideoDashboardException("Error merging data: Campaign title are not equals ! " + completeJsonDTO.getProcedureName() + " - " + partialJsonDTO.getProcedureName());
        }

        if (partialJsonDTO == null) {
            partialJsonDTO = new ExtractionDTO();
        } else {
            completeJsonDTO.setCampaignTitle(partialJsonDTO.getCampaignTitle());
            completeJsonDTO.setClientName(partialJsonDTO.getClientName());
            completeJsonDTO.setProcedureName(partialJsonDTO.getProcedureName());
            completeJsonDTO.setScope(partialJsonDTO.getScope());
        }

        if (completeJsonDTO == null) {
            completeJsonDTO = new ExtractionDTO();
        }

        // merge versions
        List<List<String>> resultVersion = mergeVersions(partialJsonDTO, completeJsonDTO);
        completeJsonDTO.setVersions(resultVersion);

        // email spedite
        long sent = partialJsonDTO.getEmail().getSent() + completeJsonDTO.getEmail().getSent();// ok
        // mail aperte
        long opened = partialJsonDTO.getEmail().getOpened() + completeJsonDTO.getEmail().getOpened();// ok
        // mail ko
        long bounces = partialJsonDTO.getEmail().getBounces() + completeJsonDTO.getEmail().getBounces();
        // click purl nella mail univoci
        long clicks = partialJsonDTO.getEmail().getClicks() + completeJsonDTO.getEmail().getClicks();
        // spedite - bounce
        long delivered = sent - bounces;
        // spedite/aperte
        double openRate = sent != 0 ? Utils.rateTwoDecimalsPercentual(opened, sent) : 0;
        // click purl / email aperta
        double clickRate = opened != 0 ? Utils.rateTwoDecimalsPercentual(clicks, opened) : 0;
        // mail ko / mail spedite
        double bounceRate = sent != 0 ? Utils.rateTwoDecimalsPercentual(bounces, sent) : 0;
        // purl produced
        long purlProduced = partialJsonDTO.getEmail().getPurlProduced() + completeJsonDTO.getEmail().getPurlProduced();

        EmailDTO mergedEmailDTO = completeJsonDTO.getEmail();
        mergedEmailDTO.setSent(sent);
        mergedEmailDTO.setOpened(opened);
        mergedEmailDTO.setBounces(bounces);
        mergedEmailDTO.setClicks(clicks);
        mergedEmailDTO.setDelivered(delivered);
        mergedEmailDTO.setOpenRate(openRate);
        mergedEmailDTO.setClickRate(clickRate);
        mergedEmailDTO.setBounceRate(bounceRate);
        mergedEmailDTO.setPurlProduced(purlProduced);
        completeJsonDTO.setEmail(mergedEmailDTO);

        // numero diclick su play unici
        long viewsUnique = partialJsonDTO.getVideo().getViewsUnique() + completeJsonDTO.getVideo().getViewsUnique();
        // click sul play totali
        long views = partialJsonDTO.getVideo().getViews() + completeJsonDTO.getVideo().getViews();
        // numero di click sulle int prefix
        long interactions = partialJsonDTO.getVideo().getInteractions() + completeJsonDTO.getVideo().getInteractions();
        // numero di click univoci sulle conversioni con prefix
        long conversions = partialJsonDTO.getVideo().getConversions() + completeJsonDTO.getVideo().getConversions();
        // numero visualizzazione medio per utente view totali/ view uniche
        double viewsUser = viewsUnique != 0 ? Utils.rateTwoDecimals(views, viewsUnique) : 0;
        // conversioni / video unique views
        double conversionRate = viewsUnique != 0 ? Utils.rateTwoDecimalsPercentual(conversions, viewsUnique) : 0;
        // iteractions / video unique views
        double interactionsRate = viewsUnique != 0 ? Utils.rateTwoDecimalsPercentual(interactions, viewsUnique) : 0;
        // click su play univoci / mail consegnate
        double viewsRate = 0;
        if (templateName.equalsIgnoreCase(PVideoTemplate.TemplateNoEmails.name()))
            viewsRate = purlProduced != 0 ? Utils.rateTwoDecimalsPercentual(viewsUnique, purlProduced) : 0;
        else
            viewsRate = delivered != 0 ? Utils.rateTwoDecimalsPercentual(viewsUnique, delivered) : 0;

        VideoDTO mergedVideoDTO = completeJsonDTO.getVideo();
        mergedVideoDTO.setViewsUnique(viewsUnique);
        mergedVideoDTO.setViews(views);
        mergedVideoDTO.setInteractions(interactions);
        mergedVideoDTO.setConversions(conversions);
        mergedVideoDTO.setViewsUser(Utils.roundTwoDecimals(viewsUser));
        mergedVideoDTO.setConversionRate(Utils.roundTwoDecimals(conversionRate));
        mergedVideoDTO.setInteractionRate(Utils.roundTwoDecimals(interactionsRate));
        mergedVideoDTO.setViewsRate(Utils.roundTwoDecimals(viewsRate));
        completeJsonDTO.setVideo(mergedVideoDTO);

        FirstDayDTO firstDayDTO = completeJsonDTO.getFirstDay();
        firstDayDTO.setOpened(getFirstDayOpenedMerge(partialJsonDTO, completeJsonDTO));
        firstDayDTO.setViews(getFirstDayViewsMerge(partialJsonDTO, completeJsonDTO));
        firstDayDTO.setConversions(getFirstDayConversionMerge(partialJsonDTO, completeJsonDTO));
        completeJsonDTO.setFirstDay(firstDayDTO);

        // "firstDay": { // primo array percentuali Secondo array numeriche Terzo totali
        // "opened": [[50, 200, 220, 100, 52, 50, 10, 2, 1, 15, 5, 12, 8, 2, 10, 1, 0, 1, 0, 2, 45, 1, 0, 0], [50, 200, 220, 100, 52, 50, 10, 2, 1, 15, 5, 12, 8, 2, 10, 1, 0, 1, 0, 2, 45, 1, 0,
        // 0],[50, 200, 220, 100, 52, 50, 10, 2, 1, 15, 5, 12, 8, 2, 10, 1, 0, 1, 0, 2, 45, 1, 0, 0]],
        // "views": [[45, 150, 200, 50, 10, 5, 0, 0, 0, 0, 0, 1, 0, 2, 2, 1, 0, 1, 0, 2, 0, 1, 0, 0],[45, 150, 200, 50, 10, 5, 0, 0, 0, 0, 0, 1, 0, 2, 2, 1, 0, 1, 0, 2, 0, 1, 0, 0],[45, 150, 200, 50,
        // 10, 5, 0, 0, 0, 0, 0, 1, 0, 2, 2, 1, 0, 1, 0, 2, 0, 1, 0, 0]],
        // "conversions": [[0, 2, 5, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],[0, 2, 5, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],[45, 150, 200, 50, 10, 5,
        // 0, 0, 0, 0, 0, 1, 0, 2, 2, 1, 0, 1, 0, 2, 0, 1, 0, 0]]
        // },

        // "conversions": [
        // ["Conversion event name 1", "58", "5000", "13"], // titolo conversione, numero di eventi unici %rispetto eventi totali= eventi unici / eventi totali
        // ["Conversion event name 2", "58 ", "5000", "13"]
        // ],
        List<List<String>> resultConversion = mergeConversionOrInteraction(partialJsonDTO.getConversions(), completeJsonDTO.getConversions(), partialJsonDTO.getVideo().getViewsUnique(),
                completeJsonDTO.getVideo().getViewsUnique());
        completeJsonDTO.setConversions(resultConversion);

        // "interactions": [ // titolo conversione, numero di eventi unici, numero di eventi totali, %rispetto eventi totali= eventi unici / eventi totali
        // ["Interaction event name 1", "58", "5000", "13"],
        // ["Interaction event name 2", "58", "5000", "13"]
        // ],
        List<List<String>> resultInteractions = mergeConversionOrInteraction(partialJsonDTO.getInteractions(), completeJsonDTO.getInteractions(), partialJsonDTO.getVideo().getViewsUnique(),
                completeJsonDTO.getVideo().getViewsUnique());
        completeJsonDTO.setInteractions(resultInteractions);

        // "deviceType":{
        // "mobile": 60,
        // "desktop": 34,
        // "tablet": 10
        // },
        int mobile = partialJsonDTO.getDeviceType().getMobile() + completeJsonDTO.getDeviceType().getMobile();
        int desktop = partialJsonDTO.getDeviceType().getDesktop() + completeJsonDTO.getDeviceType().getDesktop();
        int tablet = partialJsonDTO.getDeviceType().getTablet() + completeJsonDTO.getDeviceType().getTablet();
        int other = partialJsonDTO.getDeviceType().getOther() + completeJsonDTO.getDeviceType().getOther();

        DeviceTypeDTO mergedDeviceDTO = completeJsonDTO.getDeviceType();
        mergedDeviceDTO.setMobile(mobile);
        mergedDeviceDTO.setDesktop(desktop);
        mergedDeviceDTO.setTablet(tablet);
        mergedDeviceDTO.setOther(other);
        completeJsonDTO.setDeviceType(mergedDeviceDTO);

        // "operatingSystem":{
        // "data": [60, 20, 10, 10],
        // "label": ["Androi", "iOS", "Windows", "MacOS", "Other"]
        // },
        OperatingSystemDTO mergedOperatingSystemDTO = completeJsonDTO.getOperatingSystem();
        List<Integer> osMergedData = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            if (completeJsonDTO.getOperatingSystem().getData().size() == 6)
                completeJsonDTO.getOperatingSystem().getData().add(6, new Integer(0));

            osMergedData.add(i, partialJsonDTO.getOperatingSystem().getData().get(i) + completeJsonDTO.getOperatingSystem().getData().get(i));
        }

        mergedOperatingSystemDTO.setData(osMergedData);
        mergedOperatingSystemDTO.setLabels(Arrays.asList(OperatingSystemDTO.osLabel));// "label": ["Android", "iOS", "Windows", "MacOS", "Linux","Other"]
        completeJsonDTO.setOperatingSystem(mergedOperatingSystemDTO);

        // "engagement": {
        // "data": [750, 740, 730, 720, 710, 700, 690, 780, 770, 760] // numero di utenti per % di avanzamento
        // aggiunto rate come rapporto tra data/tot visualizzazioni
        // "rate": [750, 740, 730, 720, 710, 700, 690, 780, 770, 760]
        // }
        EngagementDTO mergedEngagementDTO = completeJsonDTO.getEngagement();
        List<Integer> mergedEngagementDataList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            mergedEngagementDataList.add(partialJsonDTO.getEngagement().getData().get(i) + completeJsonDTO.getEngagement().getData().get(i));
        }
        mergedEngagementDTO.setData(mergedEngagementDataList);
        completeJsonDTO.setEngagement(mergedEngagementDTO);
        // Rate
        List<Double> mergedEngagementRateList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            mergedEngagementRateList.add(Utils.rateTwoDecimals(completeJsonDTO.getEngagement().getData().get(i), completeJsonDTO.getVideo().getViewsUnique()));
        }
        mergedEngagementDTO.setRate(mergedEngagementRateList);
        return completeJsonDTO;
    }

    private static List<Integer> getFirstDayConversionMerge(ExtractionDTO partialJsonDTO, ExtractionDTO completeJsonDTO) {

        List<Integer> merged = initMerged();
        for (int i = 0; i < 24; i++) {
            int partial = partialJsonDTO.getFirstDay().getConversions().get(i);
            int complete = completeJsonDTO.getFirstDay().getConversions().get(i);
            int sum = partial + complete;
            merged.set(i, sum);
        }

        return merged;
    }

    private static List<Integer> getFirstDayViewsMerge(ExtractionDTO partialJsonDTO, ExtractionDTO completeJsonDTO) {
        List<Integer> merged = initMerged();
        for (int i = 0; i < 24; i++) {
            int partial = partialJsonDTO.getFirstDay().getViews().get(i);
            int complete = completeJsonDTO.getFirstDay().getViews().get(i);
            int sum = partial + complete;
            merged.set(i, sum);
        }

        return merged;
    }

    private static List<Integer> getFirstDayOpenedMerge(ExtractionDTO partialJsonDTO, ExtractionDTO completeJsonDTO) {
        List<Integer> merged = initMerged();
        for (int i = 0; i < 24; i++) {
            int partial = partialJsonDTO.getFirstDay().getOpened().get(i);
            int complete = completeJsonDTO.getFirstDay().getOpened().get(i);
            int sum = partial + complete;
            merged.set(i, sum);
        }

        return merged;
    }

    private static List<Integer> initMerged() {
        List<Integer> merged = new ArrayList<>();

        for (int i = 0; i < 24; i++) {
            merged.add(0);
        }

        return merged;
    }

    private static List<List<String>> mergeVersions(ExtractionDTO partialJsonDTO, ExtractionDTO completeJsonDTO) {
        if (completeJsonDTO.getVersions() == null)
            return partialJsonDTO.getVersions();

        Map<String, String> partialVersionMap = new HashMap<>();
        for (List<String> partialVersion : partialJsonDTO.getVersions()) {
            partialVersionMap.put(partialVersion.get(0), partialVersion.get(1));
        }

        for (List<String> completeVersion : completeJsonDTO.getVersions()) {
            String key = completeVersion.get(0);
            if (partialVersionMap.containsKey(key)) {
                // NONE
            } else
                partialVersionMap.put(key, completeVersion.get(1));
        }

        List<List<String>> merged = new ArrayList<>();
        for (Map.Entry<String, String> entry : partialVersionMap.entrySet()) {
            List<String> l = new ArrayList<>();
            l.add(entry.getKey());
            l.add("" + entry.getValue());
            merged.add(l);
        }
        return merged;
    }

    private static List<List<String>> mergeConversionOrInteraction(List<List<String>> partialCoversions, List<List<String>> completeCoversions, long partialViews, long completeViews) {

        Map<String, ConversionEvent> conversionEventMap = new HashMap<>();
        if (partialCoversions != null)
            for (List<String> partialCoversion : partialCoversions) {
                conversionEventMap.put(partialCoversion.get(0), new ConversionEvent(partialCoversion.get(0), Integer.parseInt(partialCoversion.get(1)), partialViews));
            }
        if (completeCoversions != null)
            for (List<String> completeCoversion : completeCoversions) {
                String key = completeCoversion.get(0);
                if (conversionEventMap.containsKey(key)) {
                    // SUM
                    ConversionEvent event = conversionEventMap.get(key);
                    event.setEventTot(event.getEventTot() + Integer.parseInt(completeCoversion.get(1)));
                    event.setEventRate(Utils.rateTwoDecimalsPercentual(event.getEventTot(), completeViews));
                } else
                    conversionEventMap.put(completeCoversion.get(0), new ConversionEvent(completeCoversion.get(0), Integer.parseInt(completeCoversion.get(1)), completeViews));
            }

        List<List<String>> merged = new ArrayList<>();
        for (Map.Entry<String, ConversionEvent> entry : conversionEventMap.entrySet()) {
            List<String> l = new ArrayList<>();
            l.add(entry.getKey());
            l.add("" + entry.getValue().eventTot);
            l.add("" + entry.getValue().eventRate);
            merged.add(l);
        }
        return merged;
    }

    static class ConversionEvent {

        String name;

        long eventTot;

        int eventRate;

        public ConversionEvent(String name, int eventTot, long totalViews) {
            this.name = name;
            this.eventTot = eventTot;
            this.eventRate = eventTot == 0 ? 0 : Utils.rateTwoDecimalsPercentual(eventTot, totalViews);
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getEventTot() {
            return eventTot;
        }

        public void setEventTot(long eventTot) {
            this.eventTot = eventTot;
        }

        public int getEventRate() {
            return eventRate;
        }

        public void setEventRate(int eventRate) {
            this.eventRate = eventRate;
        }
    }

}
