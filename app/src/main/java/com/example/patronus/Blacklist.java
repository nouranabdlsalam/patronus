package com.example.patronus;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Blacklist {
    private static final Set<String> badDomains = new HashSet<>(Arrays.asList(
            "badsite.com", "phishingsite.org", "ads.eviltrack.net", "untrusted.net"
    ));

    public static boolean isBlacklisted(String domain) {
        if (domain == null) return false;
        for (String bad : badDomains) {
            if (domain.contains(bad)) return true;
        }
        return false;
    }
}
