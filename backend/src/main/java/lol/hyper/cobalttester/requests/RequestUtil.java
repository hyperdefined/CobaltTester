package lol.hyper.cobalttester.requests;

import lol.hyper.cobalttester.CobaltTester;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class RequestUtil {

    public static final Logger logger = LogManager.getLogger(RequestUtil.class);

    /**
     * Send a POST request.
     *
     * @param body The body to send.
     * @param url  The url to send to.
     * @return A RequestResults object.
     */
    public static RequestResults sendPost(JSONObject body, String url) {
        String content;
        int responseCode = -1;
        try {
            StringBuilder stringBuilder;
            BufferedReader reader;
            URL urlFixed = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) urlFixed.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("User-Agent", CobaltTester.USER_AGENT);
            byte[] out = body.toString().getBytes(StandardCharsets.UTF_8);
            OutputStream stream = connection.getOutputStream();
            stream.write(out);
            responseCode = connection.getResponseCode();
            String line;
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            stringBuilder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            reader.close();
            content = stringBuilder.toString();
            connection.disconnect();
        } catch (Exception exception) {
            logger.warn("Post failed for {} because of {}", url, exception.getMessage());
            return new RequestResults(null, responseCode);
        }
        return new RequestResults(content, responseCode);
    }

    /**
     * Request a JSON object from URL.
     *
     * @param url The URL to request.
     * @return The JSONObject it returns. NULL if something went wrong.
     */
    public static JSONObject requestJSON(String url) {
        String rawJSON;
        try {
            URLConnection connection = new URL(url).openConnection();
            connection.setRequestProperty("User-Agent", CobaltTester.USER_AGENT);
            connection.setConnectTimeout(20000);
            connection.setReadTimeout(20000);
            connection.connect();
            InputStream in = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            rawJSON = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            reader.close();
        } catch (Exception exception) {
            logger.error("Unable to read contents of {}", url, exception);
            return null;
        }
        if (rawJSON.isEmpty()) {
            logger.error("Read JSON from {} returned an empty string!", url);
            return null;
        }
        try {
            return new JSONObject(rawJSON);
        } catch (JSONException exception) {
            return null;
        }
    }
}
