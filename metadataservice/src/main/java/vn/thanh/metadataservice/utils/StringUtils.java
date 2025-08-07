package vn.thanh.metadataservice.utils;

import java.util.UUID;

public class StringUtils {
    public static UUID convertToUUID(String raw) {
        // Chuyển 32 ký tự hex → UUID chuẩn có gạch ngang
        if (raw.matches("^[0-9A-Fa-f]{32}$")) {
            String formatted = raw.replaceFirst(
                    "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                    "$1-$2-$3-$4-$5"
            );
            return UUID.fromString(formatted);
        }
        // Nếu không khớp, assume đã đúng format
        return UUID.fromString(raw);
    }
}
