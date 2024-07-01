package lol.hyper.cobalttester.services;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Services {

    private static final Map<String, String> servicesMap;

    static {
        servicesMap = new HashMap<>();
        servicesMap.put("music.youtube.com", "YouTube Music");
        servicesMap.put("youtube.com", "YouTube");
        servicesMap.put("youtube.com/shorts/", "YouTube Shorts");
        servicesMap.put("tiktok.com", "TikTok");
        servicesMap.put("instagram.com", "Instagram");
        servicesMap.put("twitter.com", "Twitter");
        servicesMap.put("reddit.com", "Reddit");
        servicesMap.put("soundcloud.com", "SoundCloud");
        servicesMap.put("bilibili.com", "BiliBili");
        servicesMap.put("dailymotion.com", "Dailymotion");
        servicesMap.put("ok.ru", "Odnoklassniki");
        servicesMap.put("streamable.com", "Streamable");
        servicesMap.put("tumblr.com", "Tumblr");
        servicesMap.put("twitch.tv", "Twitch Clips");
        servicesMap.put("vk.com", "VK");
        servicesMap.put("vimeo.com", "Vimeo");
        servicesMap.put("pinterest.com", "Pinterest");
        servicesMap.put("rutube.ru", "RUTUBE");
        servicesMap.put("vine.co", "Vine");
        servicesMap.put("loom.com", "Loom");
    }

    public static String makePretty(String url) {
        if (url.contains("www.")) {
            url = url.replace("www.", "");
        }
        String prettyName = null;
        for (String key : servicesMap.keySet()) {
            // hardcode YouTube Shorts
            if (url.contains("youtube.com/shorts")) {
                return "YouTube Shorts";
            }
            if (url.startsWith("https://" + key) || url.startsWith("http://" + key)) {
                prettyName = servicesMap.get(key);
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

    public static String makeSlug(String service) {
        String slug = service.toLowerCase(Locale.ROOT);
        return slug.replace(" ", "-");
    }

    public static Map<String, String> getServicesMap() {
        return servicesMap;
    }
}
