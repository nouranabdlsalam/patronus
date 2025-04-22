package com.example.patronus;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class PacketParser {

    public static String parseDNSPacket(byte[] packet, int length) {
        try {
            // DNS usually starts at byte 28+ (Ethernet + IP + UDP headers)
            int dnsStart = 28;
            ByteBuffer buffer = ByteBuffer.wrap(packet, dnsStart, length - dnsStart);

            int qNameStart = dnsStart + 12;
            StringBuilder domain = new StringBuilder();
            int i = 12;
            while (i < length - dnsStart && packet[dnsStart + i] != 0) {
                int labelLen = packet[dnsStart + i];
                if (labelLen < 0) break;

                for (int j = 1; j <= labelLen; j++) {
                    domain.append((char) packet[dnsStart + i + j]);
                }
                domain.append('.');
                i += labelLen + 1;
            }

            if (domain.length() > 0) domain.setLength(domain.length() - 1); // remove last dot
            return domain.toString();

        } catch (Exception e) {
            Log.e("PacketParser", "Failed to parse DNS packet", e);
        }

        return null;
    }

    public static boolean containsHttpPlaintext(byte[] packet, int length) {
        String payload = new String(packet, 0, Math.min(length, 2048), StandardCharsets.US_ASCII);
        return payload.contains("HTTP/1.1") || payload.toLowerCase().contains("password=") || payload.toLowerCase().contains("login");
    }
}
