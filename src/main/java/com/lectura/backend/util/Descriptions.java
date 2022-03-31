package com.lectura.backend.util;

import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class Descriptions {
    // pdf/epub/mobi
    private static final Map<String, String> formats = Map.of(
            "E101", "epub",
            "E107", "pdf",
            "E127", "mobi",
            "E200", "epub",
            "E201", "epub");

    // none/watermark/acs4
    private static final Map<String, String> protections = Map.of(
            "00", "none",
            "02", "watermark",
            "03", "acs4");

    public static List<String> getFormats(String formatCodes) {
        return Arrays.stream(StringUtils.split(formatCodes, "|"))
                .map(f -> formats.get(f)).distinct()
                .collect(Collectors.toList());
    }

    public static String getProtection(String codeProtection) {
        return protections.getOrDefault(codeProtection, "none");
    }
}
