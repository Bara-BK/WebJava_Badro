package tn.badro.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Service for translating text between languages using Google Translate API.
 */
public class TranslationService {
    
    private static final String API_URL = "https://translate.googleapis.com/translate_a/single";
    private static final String USER_AGENT = "Mozilla/5.0";
    
    // Cache for translations to avoid redundant API calls
    private final Map<String, String> translationCache = new HashMap<>();
    
    /**
     * Available languages for translation
     */
    public enum Language {
        AUTO_DETECT("auto", "Auto Detect"),
        ENGLISH("en", "English"),
        FRENCH("fr", "French"),
        ARABIC("ar", "Arabic"),
        GERMAN("de", "German"),
        SPANISH("es", "Spanish"),
        ITALIAN("it", "Italian"),
        PORTUGUESE("pt", "Portuguese"),
        RUSSIAN("ru", "Russian"),
        JAPANESE("ja", "Japanese"),
        CHINESE("zh-CN", "Chinese (Simplified)");
        
        private final String code;
        private final String displayName;
        
        Language(String code, String displayName) {
            this.code = code;
            this.displayName = displayName;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
        
        public static Language fromCode(String code) {
            for (Language language : values()) {
                if (language.getCode().equals(code)) {
                    return language;
                }
            }
            return AUTO_DETECT;
        }
    }
    
    /**
     * Translates text synchronously from source language to target language
     * 
     * @param text          Text to translate
     * @param sourceLanguage Source language code
     * @param targetLanguage Target language code
     * @return Translated text
     * @throws Exception If translation fails
     */
    public String translate(String text, Language sourceLanguage, Language targetLanguage) throws Exception {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        // Skip translation if source and target languages are the same
        if (sourceLanguage != Language.AUTO_DETECT && sourceLanguage == targetLanguage) {
            return text;
        }
        
        // Check cache first
        String cacheKey = sourceLanguage.getCode() + ":" + targetLanguage.getCode() + ":" + text;
        if (translationCache.containsKey(cacheKey)) {
            return translationCache.get(cacheKey);
        }
        
        // Construct the API URL
        String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
        String urlStr = API_URL +
                        "?client=gtx" +
                        "&sl=" + sourceLanguage.getCode() +
                        "&tl=" + targetLanguage.getCode() +
                        "&dt=t" +  // return translated text
                        "&q=" + encodedText;
        
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        // Set request properties
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", USER_AGENT);
        
        // Get the response
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
        
        // Parse JSON response
        String translatedText = parseTranslationResponse(response.toString());
        
        // Cache the result
        translationCache.put(cacheKey, translatedText);
        
        return translatedText;
    }
    
    /**
     * Translates text asynchronously
     * 
     * @param text          Text to translate
     * @param sourceLanguage Source language
     * @param targetLanguage Target language
     * @return CompletableFuture containing the translated text
     */
    public CompletableFuture<String> translateAsync(String text, Language sourceLanguage, Language targetLanguage) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return translate(text, sourceLanguage, targetLanguage);
            } catch (Exception e) {
                System.err.println("Translation error: " + e.getMessage());
                e.printStackTrace();
                return text; // Return original text on error
            }
        });
    }
    
    /**
     * Parse the Google Translate API JSON response
     * 
     * @param jsonResponse API response in JSON format
     * @return Translated text
     */
    private String parseTranslationResponse(String jsonResponse) {
        StringBuilder translatedText = new StringBuilder();
        
        try {
            com.google.gson.Gson gson = new com.google.gson.Gson();
            com.google.gson.JsonArray jsonArray = gson.fromJson(jsonResponse, com.google.gson.JsonArray.class);
            com.google.gson.JsonArray translationArray = jsonArray.get(0).getAsJsonArray();
            
            for (int i = 0; i < translationArray.size(); i++) {
                com.google.gson.JsonArray translationLineArray = translationArray.get(i).getAsJsonArray();
                String translationLine = translationLineArray.get(0).getAsString();
                translatedText.append(translationLine);
            }
        } catch (Exception e) {
            System.err.println("Error parsing translation response: " + e.getMessage());
            e.printStackTrace();
            return jsonResponse; // Return raw response in case of error
        }
        
        return translatedText.toString();
    }
    
    /**
     * Clear the translation cache
     */
    public void clearCache() {
        translationCache.clear();
    }
} 