package com.doxee.pvideo.dashboard.aws.lambda.handler;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TxtReader {

    public Map<String, List<String>> read(String filePath) throws Exception {

        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = "%";

        Map<String, List<String>> userAgents = new HashMap<>();
        try {

            br = new BufferedReader(new FileReader(filePath));
            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] field = line.split(cvsSplitBy);
                String scope = field[0].trim();
                String unique = field[2].trim();
                System.out.println("  AGENT='" + field[1].trim() + "'");
                if (!userAgents.containsKey(scope)) {
                    userAgents.put(scope, new ArrayList<String>());
                }

                userAgents.get(scope).add(field[1].trim() + "%" + field[2].trim());

            }

        } catch (FileNotFoundException e) {
            throw new Exception("Error", e);
        } catch (IOException e) {
            throw new Exception("Error", e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    throw new Exception("Error", e);
                }
            }
        }

        return userAgents;

    }

}
