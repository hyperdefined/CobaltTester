package lol.hyper.cobalttester.utils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Services {

    private static final Map<String, String> servicesMap;

    static {
        servicesMap = new HashMap<>();
        servicesMap.put("music.youtube.com", "YouTube Music");
        servicesMap.put("youtube.com", "YouTube");
        servicesMap.put("tiktok.com", "TikTok");
        servicesMap.put("instagram.com", "Instagram");
        servicesMap.put("twitter.com", "Twitter");
        servicesMap.put("reddit.com", "Reddit");
        servicesMap.put("soundcloud.com", "SoundCloud");
        servicesMap.put("bilibili.com", "BiliBili");
        servicesMap.put("dailymotion.com", "Dailymotion");
        servicesMap.put("ok.ru", "OK.ru");
        servicesMap.put("streamable.com", "Streamable");
        servicesMap.put("tumblr.com", "Tumblr");
        servicesMap.put("twitch.tv", "Twitch Clips");
        servicesMap.put("vk.com", "VK.com");
        servicesMap.put("vimeo.com", "Vimeo");
        servicesMap.put("pinterest.com", "Pinterest");
        servicesMap.put("rutube.ru", "RUTUBE");
    }

    public static String makePretty(String url) {
        if (url.contains("www.")) {
            url = url.replace("www.", "");
        }
        String prettyName = null;
        for (String key : servicesMap.keySet()) {
            if (url.startsWith("https://" + key) || url.startsWith("http://" + key)) {
                prettyName = servicesMap.get(key);
                break;
            } else if (url.contains("/shorts/")) {
                // hardcode youtube shorts
                prettyName = "YouTube Shorts";
                break;
            }
        }
        return prettyName;
    }

    public static String makeUgly(String service) {
        String result = service;
        result = result.replace(" ", "_");
        return result.toLowerCase(Locale.ROOT);
    }
}
