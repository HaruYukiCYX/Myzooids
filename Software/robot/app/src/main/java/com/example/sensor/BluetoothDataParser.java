package com.example.sensor;

import java.util.HashMap;
import java.util.Map;

public class BluetoothDataParser {

    public static Map<String, Double> parseData(String data) {
        Map<String, Double> result = new HashMap<>();

        String[] dataList = data.trim().split("Z");
        for (String dataItem : dataList) {
            if (!dataItem.isEmpty()) {
                String type = dataItem.substring(0, 1);
                String value = dataItem.substring(1, dataItem.length());
                switch (type) {
                    case "A":
                        result.put("temperature", Double.parseDouble(value));
                        break;
                    case "B":
                        result.put("humidity", Double.parseDouble(value));
                        break;
                    case "E":
                        result.put("soilMoisture", Double.parseDouble(value));
                        break;
                    case "C":
                        result.put("lightIntensity", Double.parseDouble(value));
                        break;
                    case "F":
                        result.put("nitrogen", Double.parseDouble(value));
                        break;
                    case "G":
                        result.put("phosphorus", Double.parseDouble(value));
                        break;
                    case "H":
                        result.put("potassium", Double.parseDouble(value));
                        break;
                    case "D":
                        result.put("co2", Double.parseDouble(value));
                        break;
                }
            }
        }
        return result;
    }

    public static Map<String, Double> parseJsonData(String data) {
        Map<String, Double> result = new HashMap<>();

        try {
            data = data.trim().replace("{", "").replace("}", "");

            String[] pairs = data.split(",");

            for (String pair : pairs) {
                String[] keyValue = pair.split(":");
                if (keyValue.length == 2) {
                    String key = keyValue[0].trim().replace("\"", "");
                    double value = Double.parseDouble(keyValue[1].trim());
                    result.put(key, value);
                }
            }
        } catch (Exception e) {
            result.clear();
        }

        return result;
    }
}