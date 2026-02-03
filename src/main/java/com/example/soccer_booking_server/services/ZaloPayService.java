package com.example.soccer_booking_server.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ZaloPayService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper;

    // TODO: bạn có thể inject từ properties bằng @Value hoặc @ConfigurationProperties
    private final String appId = "2553";
    private final String key1  = "PcY4iZIKFCIdgZvA6ueMcMHHUbRLYjPL";
    private final String endpoint = "https://sb-openapi.zalopay.vn/v2/create";

    public ZaloPayService(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public Map<String, Object> createOrder(long amount, String appUser) {
        int randomId = new Random().nextInt(1_000_000);

        // ✅ y hệt sample: yyMMdd_randomId
        String appTransId = getCurrentTimeString("yyMMdd") + "_" + randomId;

        long appTime = System.currentTimeMillis();

        // ✅ y hệt sample: embed_data = {}
        Map<String, Object> embedData = new HashMap<>();

        // ✅ y hệt sample: item = [{}]
        List<Map<String, Object>> items = List.of(new HashMap<>());

        String embedDataJson;
        String itemJson;
        try {
            // IMPORTANT: đây là JSON string sẽ đưa vào field + đưa vào chuỗi ký
            embedDataJson = mapper.writeValueAsString(embedData); // "{}"
            itemJson = mapper.writeValueAsString(items);          // "[{}]"
        } catch (Exception e) {
            throw new RuntimeException("JSON serialize failed", e);
        }

        // ✅ y hệt sample: tạo chuỗi ký
        String data = appId + "|" + appTransId + "|" + appUser + "|" + amount + "|" + appTime + "|" + embedDataJson + "|" + itemJson;
        String mac = hmacSha256Hex(key1, data);

        // ✅ y hệt sample: form-urlencoded fields
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("app_id", appId);
        form.add("app_trans_id", appTransId);
        form.add("app_time", String.valueOf(appTime));
        form.add("app_user", appUser);
        form.add("amount", String.valueOf(amount));
        form.add("description", "Lazada - Payment for the order #" + randomId);
        form.add("bank_code", "zalopayapp");
        form.add("item", itemJson);
        form.add("embed_data", embedDataJson);
        form.add("mac", mac);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(form, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                endpoint,
                HttpMethod.POST,
                entity,
                Map.class
        );

        // ✅ Trả raw map cho controller/FE dễ debug
        Map<String, Object> result = response.getBody();
        if (result == null) result = new HashMap<>();

        // Log để bạn so với sample
        System.out.println("ZLP DATA STRING: " + data);
        System.out.println("ZLP MAC: " + mac);
        System.out.println("ZLP RESPONSE: " + result);

        // Bạn có thể attach app_trans_id để FE lưu
        result.put("app_trans_id", appTransId);
        return result;
    }

    private static String getCurrentTimeString(String format) {
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT+7"));
        SimpleDateFormat fmt = new SimpleDateFormat(format);
        fmt.setCalendar(cal);
        return fmt.format(cal.getTimeInMillis());
    }

    private static String hmacSha256Hex(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder(raw.length * 2);
            for (byte b : raw) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("HMAC error", e);
        }
    }
}
