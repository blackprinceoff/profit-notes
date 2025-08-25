package com.example.profitnotes.model;

import com.example.profitnotes.util.HttpClientUtil;
import com.fasterxml.jackson.databind.*;

public class ExchangeRateService {
    private static final String API_URL = "https://api.coingecko.com/api/v3/simple/price?ids=tether&vs_currencies=uah";

    public static double getUsdtToUahRate(){
        try {
            String response = HttpClientUtil.get(API_URL);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            return root.get("tether").get("uah").asDouble();
        } catch (Exception e){
            return 41.80; // fallback rate
        }
    }
}
