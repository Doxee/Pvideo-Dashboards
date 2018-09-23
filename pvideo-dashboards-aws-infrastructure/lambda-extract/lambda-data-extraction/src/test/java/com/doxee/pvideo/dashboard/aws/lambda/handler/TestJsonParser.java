package com.doxee.pvideo.dashboard.aws.lambda.handler;

import com.amazonaws.util.IOUtils;
import com.amazonaws.util.json.Jackson;
import com.doxee.pvideo.dashboard.aws.lambda.handler.utils.JsonUtils;
import com.doxee.pvideo.dashboard.commons.json.ConfigurationEvent;
import com.doxee.pvideo.dashboard.commons.json.ExtractionDTO;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class TestJsonParser {

    @Test
    public void parseJsonData() {
        try {
            String resource = "extraction.json";
            InputStream stream = TestJsonParser.class.getResourceAsStream(resource);
            String json = IOUtils.toString(stream);
            ExtractionDTO extractionDTO = Jackson.fromJsonString(json, ExtractionDTO.class);

            System.out.println(Jackson.toJsonPrettyString(extractionDTO));
            assertEquals(true, true);

        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    @Test
    public void createJsonInputEventData() {
        ConfigurationEvent event = new ConfigurationEvent();
        List<String> clients = new ArrayList<>();
        clients.add("axa");
        clients.add("client_name_1");
        clients.add("client_name_2");
        event.setCampaigns(clients);

        String json = Jackson.toJsonPrettyString(event);
        System.out.println(json);

        ConfigurationEvent startEvent = Jackson.fromJsonString(json, ConfigurationEvent.class);
        if (startEvent.getCampaigns() == null || startEvent.getCampaigns().isEmpty()) {
            System.out.println("null or empty");
        } else {
            System.out.println("not null");
        }
        assertEquals(true, true);
    }

}
