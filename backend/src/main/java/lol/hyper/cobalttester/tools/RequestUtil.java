package lol.hyper.cobalttester.tools;

import lol.hyper.cobalttester.CobaltTester;
import lol.hyper.cobalttester.RequestResults;
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

    public static Logger logger = LogManager.getLogger(RequestUtil.class);

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
            return new RequestResults(null, responseCode);
        }
        return new RequestResults(content, responseCode);
    }

    public static JSONObject requestJSON(String url) {
        String rawJSON;
        try {
            URLConnection connection = new URL(url).openConnection();
            connection.setRequestProperty("User-Agent", CobaltTester.USER_AGENT);
            connection.connect();
            InputStream in = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            rawJSON = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            reader.close();
        } catch (IOException exception) {
            logger.error("Unable to read URL " + url, exception);
            return null;
        }
        if (rawJSON.isEmpty()) {
            logger.error("Read JSON from " + url + " returned an empty string!");
            return null;
        }
        try {
            return new JSONObject(rawJSON);
        } catch (JSONException exception) {
            return null;
        }
    }

    public static boolean testUrl(String url) {
        int response;
        try {
            URL connectUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) connectUrl.openConnection();
            connection.setRequestProperty("User-Agent", CobaltTester.USER_AGENT);
            connection.setRequestMethod("GET");
            connection.connect();
            response = connection.getResponseCode();
        } catch (IOException exception) {
            logger.error("Unable to read URL " + url, exception);
            return false;
        }
        return response == 200;
    }
}
