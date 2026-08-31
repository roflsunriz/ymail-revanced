package app.revanced.extension.ymail;

import android.webkit.WebView;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class AdBlocker {
    private static final String BLOCKED_URL = "https://blocked.invalid/";
    private static final Set<String> BLOCKED_HOSTS = new HashSet<>(Arrays.asList(
            "2mdn.net",
            "ad.yahoo.co.jp",
            "admob.com",
            "ads.yahoo.co.jp",
            "adserver.yahoo.co.jp",
            "adservice.google.com",
            "adservice.google.co.jp",
            "app-measurement.com",
            "app.adjust.com",
            "app.adjust.io",
            "app.adjust.net.in",
            "crashlyticsreports-pa.googleapis.com",
            "doubleclick.net",
            "firebase-settings.crashlytics.com",
            "gdpr.adjust.com",
            "gdpr.adjust.io",
            "googleadservices.com",
            "googleadsserving.cn",
            "googlesyndication.com",
            "googletagmanager.com",
            "googletagservices.com",
            "pagead2.googlesyndication.com",
            "rd.ane.yahoo.co.jp",
            "ssrv.adjust.com",
            "ssrv.adjust.io",
            "subscription.adjust.com",
            "subscription.adjust.io",
            "yads.c.yimg.jp",
            "yads.yahoo.co.jp",
            "ybx.yahoo.co.jp",
            "yjtag.yahoo.co.jp"
    ));

    private AdBlocker() {
    }

    public static InetAddress[] getAllByName(String host) throws UnknownHostException {
        rejectHost(host);
        return InetAddress.getAllByName(host);
    }

    public static InetAddress getByName(String host) throws UnknownHostException {
        rejectHost(host);
        return InetAddress.getByName(host);
    }

    public static InetAddress[] blockGetAllByName(String host) throws UnknownHostException {
        throw blocked();
    }

    public static InetAddress blockGetByName(String host) throws UnknownHostException {
        throw blocked();
    }

    public static URLConnection openConnection(URL url) throws IOException {
        rejectUrl(url);
        return url.openConnection();
    }

    public static URLConnection openConnection(URL url, Proxy proxy) throws IOException {
        rejectUrl(url);
        return url.openConnection(proxy);
    }

    public static URLConnection blockOpenConnection(URL url) throws IOException {
        throw blocked();
    }

    public static URLConnection blockOpenConnection(URL url, Proxy proxy) throws IOException {
        throw blocked();
    }

    public static void blockConnect(URLConnection connection) throws IOException {
        throw blocked();
    }

    public static String sanitizeNetworkUrl(String value) {
        return isBlockedUrl(value) ? BLOCKED_URL : value;
    }

    public static String blockNetworkUrl(String value) {
        return BLOCKED_URL;
    }

    public static void loadUrl(WebView webView, String value) {
        webView.loadUrl(isBlockedUrl(value) ? "about:blank" : value);
    }

    public static void loadUrl(WebView webView, String value, Map<String, String> headers) {
        webView.loadUrl(isBlockedUrl(value) ? "about:blank" : value, headers);
    }

    public static void blockLoadUrl(WebView webView, String value) {
        webView.loadUrl("about:blank");
    }

    public static void blockLoadUrl(WebView webView, String value, Map<String, String> headers) {
        webView.loadUrl("about:blank");
    }

    public static boolean isBlockedUrl(String value) {
        if (value == null || value.isEmpty()) return false;
        try {
            return isBlockedHost(URI.create(value).getHost());
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    public static boolean isBlockedHost(String host) {
        if (host == null || host.isEmpty()) return false;
        String normalized = host.toLowerCase(Locale.ROOT);
        while (normalized.endsWith(".")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        for (String blocked : BLOCKED_HOSTS) {
            if (normalized.equals(blocked) || normalized.endsWith("." + blocked)) return true;
        }
        return false;
    }

    private static void rejectHost(String host) throws UnknownHostException {
        if (isBlockedHost(host)) throw blocked();
    }

    private static UnknownHostException blocked() {
        return new UnknownHostException("Blocked advertising network request");
    }

    private static void rejectUrl(URL url) throws UnknownHostException {
        if (url != null) rejectHost(url.getHost());
    }
}
