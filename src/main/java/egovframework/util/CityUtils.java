package egovframework.util;

import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 주소 문자열에서 시/도 약칭을 추출하는 유틸리티
 */
public class CityUtils {

    // 주소 접두사 → 시/도 약칭 매핑 (긴 접두사를 먼저 체크)
    private static final Map<String, String> CITY_PREFIX_MAP = new LinkedHashMap<>();
    static {
        CITY_PREFIX_MAP.put("전북특별자치도", "전북");
        CITY_PREFIX_MAP.put("전라북도",       "전북");
        CITY_PREFIX_MAP.put("전라남도",       "전남");
        CITY_PREFIX_MAP.put("충청북도",       "충북");
        CITY_PREFIX_MAP.put("충청남도",       "충남");
        CITY_PREFIX_MAP.put("경상북도",       "경북");
        CITY_PREFIX_MAP.put("경상남도",       "경남");
        CITY_PREFIX_MAP.put("강원특별자치도", "강원");
        CITY_PREFIX_MAP.put("제주특별자치도", "제주");
        CITY_PREFIX_MAP.put("서울",           "서울");
        CITY_PREFIX_MAP.put("부산",           "부산");
        CITY_PREFIX_MAP.put("대구",           "대구");
        CITY_PREFIX_MAP.put("인천",           "인천");
        CITY_PREFIX_MAP.put("광주",           "광주");
        CITY_PREFIX_MAP.put("대전",           "대전");
        CITY_PREFIX_MAP.put("울산",           "울산");
        CITY_PREFIX_MAP.put("세종",           "세종");
        CITY_PREFIX_MAP.put("경기",           "경기");
        CITY_PREFIX_MAP.put("강원",           "강원");
        CITY_PREFIX_MAP.put("충북",           "충북");
        CITY_PREFIX_MAP.put("충남",           "충남");
        CITY_PREFIX_MAP.put("전북",           "전북");
        CITY_PREFIX_MAP.put("전남",           "전남");
        CITY_PREFIX_MAP.put("경북",           "경북");
        CITY_PREFIX_MAP.put("경남",           "경남");
        CITY_PREFIX_MAP.put("제주",           "제주");
    }

    private CityUtils() {}

    /**
     * 주소 문자열에서 시/도 약칭을 추출한다.
     * 예) "경기도 수원시 ..." → "경기", "충청북도 청주시 ..." → "충북"
     *
     * @return 시/도 약칭, 매핑되지 않으면 null
     */
    public static String extractCity(String addr) {
        if (!StringUtils.hasText(addr)) return null;
        for (Map.Entry<String, String> entry : CITY_PREFIX_MAP.entrySet()) {
            if (addr.startsWith(entry.getKey())) return entry.getValue();
        }
        return null;
    }
}
