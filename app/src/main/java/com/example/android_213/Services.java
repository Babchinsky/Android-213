package com.example.android_213;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class Services {
    private final static Map<String, CacheItem> cache = new HashMap<>();

    public static String fetchUrlText(String href) throws RuntimeException {
//        if (cache.containsKey(href)) {
//            CacheItem cacheItem = cache.get(href);
//            if (cacheItem != null) {
//                return cacheItem.text;
//            }
//        }
        try (InputStream urlStream = new URL(href).openStream()) {
            String text = readAllText(urlStream);
            cache.put(href, new CacheItem(href, text, null));
            return text;
        } catch (IOException ex) {
            Log.d("loadRates", "Ошибка загрузки: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    public static boolean postForm( String url, Map<String, String> data ) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL( url ).openConnection();
            connection.setDoInput( true );   // очікуємо відповідь від сервера
            connection.setDoOutput( true );  // запис у connection - формування тіла
            connection.setChunkedStreamingMode( 0 );  // надсилати єдиним запитом
            connection.setRequestMethod( "POST" );
            connection.setRequestProperty( "Accept", "application/json" );
            connection.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded" );
            String charsetName = StandardCharsets.UTF_8.name();
            StringBuilder stringBuilder = new StringBuilder();
            boolean isFirst = true;
            for( Map.Entry<String, String> entry : data.entrySet() ) {
                if( isFirst ) {
                    isFirst = false;
                }
                else {
                    stringBuilder.append( '&' );
                }
                stringBuilder.append( String.format( Locale.ROOT,
                        "%s=%s",
                        entry.getKey(),
                        URLEncoder.encode( entry.getValue(), charsetName )
                ));
            }
            String body = stringBuilder.toString();
            OutputStream bodyStream = connection.getOutputStream();
            bodyStream.write( body.getBytes( charsetName ) );
            bodyStream.flush();
            bodyStream.close();

            int statusCode = connection.getResponseCode();
            if( statusCode < 300 ) {
                return true;
            }
            else {   // помилка від сервера, опис у тілі
                Log.d( "postForm",
                        Services.readAllText( connection.getErrorStream() ) //  - доступ до тіла при помилковій відповіді
                );
            }
            connection.disconnect();
        }
        catch (Exception ex) {
            Log.d( "postForm", ex.getCause() + " " + ex.getMessage() );
        }
        return false;
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
