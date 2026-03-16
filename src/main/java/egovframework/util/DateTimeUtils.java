package egovframework.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 날짜/시간 및 숫자 파싱 유틸리티
 * - AirKorea API 응답 문자열을 Java 타입으로 변환하는 정적 메서드 제공
 */
public class DateTimeUtils {

    // 지원하는 날짜+시간 포맷 목록 (순서대로 시도)
    private static final DateTimeFormatter[] DATE_TIME_FORMATTERS = {
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
        DateTimeFormatter.ofPattern("yyyyMMddHHmm"),
        DateTimeFormatter.ofPattern("yyyyMMddHHmmss"),
    };

    // 날짜만 있는 경우 사용하는 포맷 (10자리: yyyy-MM-dd)
    private static final DateTimeFormatter DATE_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private DateTimeUtils() {}

    /**
     * 문자열을 Double로 변환
     * - null, 빈 문자열, "-"(측정 불가), "null" 문자열, 천 단위 쉼표(,) 처리
     */
    public static Double parseDoubleOrNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        if (trimmed.isEmpty() || "-".equals(trimmed) || "null".equalsIgnoreCase(trimmed)) return null;
        try {
            return Double.parseDouble(trimmed.replace(",", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // 문자열을 LocalDateTime으로 변환
    public static LocalDateTime parseDateTime(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        if (trimmed.isEmpty()) return null;

        // 날짜만 있는 경우 (10자리: yyyy-MM-dd) → 자정(00:00:00)으로 변환
        if (trimmed.length() == 10) {
            try {
                return LocalDate.parse(trimmed, DATE_FORMATTER).atStartOfDay();
            } catch (DateTimeParseException e) {
                return null;
            }
        }

        // 날짜+시간 포맷을 순서대로 시도
        for (DateTimeFormatter formatter : DATE_TIME_FORMATTERS) {
            try {
                return LocalDateTime.parse(trimmed, formatter);
            } catch (DateTimeParseException e) {
                // 다음 포맷 시도
            }
        }
        return null; // 변환된 LocalDateTime, 변환 불가 시 null
    }
}
