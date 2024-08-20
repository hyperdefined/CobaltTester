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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RequestUtil {

    public static final Logger logger = LogManager.getLogger(RequestUtil.class);
    private static final Pattern TITLE_PATTERN = Pattern.compile("<title>(.*?)</title>", Pattern.CASE_INSENSITIVE);

    /**
     * Send a POST request.
     *
     * @param body The body to send.
     * @param url  The url to send to.
     * @return A RequestResults object.
     */
    public static RequestResults sendPost(JSONObject body, String url) {
        String content;
        HttpURLConnection connection = null;
        int responseCode = -1;
        try {
            StringBuilder stringBuilder;
            BufferedReader reader;
            URL urlFixed = new URL(url);
            connection = (HttpURLConnection) urlFixed.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("User-Agent", CobaltTester.USER_AGENT);

            byte[] out = body.toString().getBytes(StandardCharsets.UTF_8);
            OutputStream stream = connection.getOutputStream();
            stream.write(out);
            stream.close();

            responseCode = connection.getResponseCode();
            InputStream inputStream;

            // get the content if the post works or fails regardless
            if (responseCode == 200) {
                inputStream = connection.getInputStream();
            } else {
                inputStream = connection.getErrorStream();
            }

            String line;
            reader = new BufferedReader(new InputStreamReader(inputStream));
            stringBuilder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            reader.close();
            content = stringBuilder.toString();

        } catch (Exception exception) {
            return new RequestResults(null, responseCode);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
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

    /**
     * Test a coablt's frontend. It will match the HTML title "cobalt".
     *
     * @param url The url to test.
     * @return true/false if it works and is valid..
     */
    public static boolean testFrontEnd(String url) {
        int response;
        try {
            URL connectUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) connectUrl.openConnection();
            connection.setRequestProperty("User-Agent", CobaltTester.USER_AGENT);
            connection.setRequestMethod("GET");
            connection.connect();
            response = connection.getResponseCode();

            if (response == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();

                Matcher matcher = TITLE_PATTERN.matcher(content.toString());
                if (matcher.find()) {
                    String title = matcher.group(1).trim();
                    if (title.equalsIgnoreCase("cobalt")) {
                        return true;
                    } else {
                        logger.warn("{} frontend is alive, but title does NOT match to cobalt. Please manually check this!", url);
                    }
                }
            }
        } catch (IOException exception) {
            logger.error("Unable to read URL {}", url, exception);
            return false;
        }
        return false;
    }
}
