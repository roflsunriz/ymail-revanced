package app.revanced.extension.ymail;

import org.junit.jupiter.api.Test;

import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdBlockerTest {
    @Test
    void blocksGoogleYahooAndAdjustAdvertisingHostsWithoutOvermatching() {
        assertTrue(AdBlocker.isBlockedHost("pagead2.googlesyndication.com."));
        assertTrue(AdBlocker.isBlockedHost("sub.yads.yahoo.co.jp"));
        assertTrue(AdBlocker.isBlockedHost("app.adjust.com"));
        assertFalse(AdBlocker.isBlockedHost("mail.yahoo.co.jp"));
        assertFalse(AdBlocker.isBlockedHost("notdoubleclick.net"));
    }

    @Test
    void preservesNormalMailUrls() {
        assertEquals("https://mail.yahoo.co.jp/", AdBlocker.sanitizeNetworkUrl("https://mail.yahoo.co.jp/"));
        assertEquals("https://blocked.invalid/", AdBlocker.sanitizeNetworkUrl(
                "https://googleads.g.doubleclick.net/pagead/id"));
    }

    @Test
    void advertisingSdkDnsIsAlwaysRejected() {
        assertThrows(UnknownHostException.class, () -> AdBlocker.blockGetByName("unlisted.example"));
    }
}
