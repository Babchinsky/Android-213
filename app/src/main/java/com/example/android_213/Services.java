package com.example.android_213;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Services {
    private final static Map<String, CacheItem> cache = new HashMap<>();

    public static String fetchUrlText(String href) throws RuntimeException {
        if (cache.containsKey(href)) {
            CacheItem cacheItem = cache.get(href);
            if (cacheItem != null) {
                return cacheItem.text;
            }
        }
        try (InputStream urlStream = new URL(href).openStream()) {
            String text = readAllText(urlStream);
            cache.put(href, new CacheItem(href, text, null));
            return text;
        } catch (IOException ex) {
            Log.d("loadRates", "Ошибка загрузки: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    public static String readAllText(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[4096];
        ByteArrayOutputStream byteBuilder = new ByteArrayOutputStream();
        int receivedBytes;
        while ((receivedBytes = inputStream.read(buffer)) > 0) {
            byteBuilder.write(buffer, 0, receivedBytes);
        }
        return byteBuilder.toString();
    }

    static class CacheItem {
        private String href;
        private String text;
        private Date expires;

        public CacheItem(String href, String text, Date expires) {
            this.href = href;
            this.text = text;
            this.expires = expires;
        }
    }
}
