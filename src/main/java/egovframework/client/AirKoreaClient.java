package egovframework.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import egovframework.config.AirKoreaProperties;
import egovframework.util.DateTimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egovframe.rte.psl.dataaccess.util.EgovMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * AirKorea 오픈API 호출 클라이언트
 *
 * [처리 흐름]
 *   getStations() → executeGet() → parseStations() → EgovMap 리스트 반환
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AirKoreaClient {

    private final RestTemplate       restTemplate; // HTTP 통신 담당 (AppConfig에서 Bean 등록)
    private final ObjectMapper       objectMapper;  // JSON 파싱 담당 (AppConfig에서 Bean 등록)
    private final AirKoreaProperties props;         // airkorea.* 설정값 (serviceKey, baseUrl)


    // 전체 측정소 목록을 페이지 단위로 조회
    public List<EgovMap> getStations(int pageNo, int numOfRows) {
        URI uri = UriComponentsBuilder
                .fromHttpUrl(props.getMsrstn().getBaseUrl() + "/getMsrstnList")
                .queryParam("serviceKey", props.getServiceKey())
                .queryParam("returnType", "json")
                .queryParam("numOfRows", numOfRows)
                .queryParam("pageNo", pageNo)
                .build(true) // 이미 인코딩된 serviceKey의 중복 인코딩 방지
                .toUri();

        String json = executeGet(uri);
        List<EgovMap> rows = parseStations(json); // 측정소 정보가 담긴 EgovMap 리스트

        // 데이터 수신 여부 확인용 로그 (페이지별 수신 건수 체크)
        log.info("[AirKorea] pageNo={}, count={}", pageNo, rows.size());
        return rows;
    }


    // 측정소별 실시간 측정정보 조회 (dataTerm: DAILY / MONTH / 3MONTH)
    public List<EgovMap> getMeasurements(String stationName, String dataTerm, int pageNo, int numOfRows) {
        URI uri = UriComponentsBuilder
                .fromHttpUrl(props.getArpltn().getBaseUrl() + "/getMsrstnAcctoRltmMesureDnsty")
                .queryParam("serviceKey", props.getServiceKey())
                .queryParam("returnType", "json")
                .queryParam("numOfRows", numOfRows)
                .queryParam("pageNo", pageNo)
                .queryParam("stationName", stationName)
                .queryParam("dataTerm", dataTerm)
                .queryParam("ver", "1.3")
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUri();

        String json = executeGet(uri);
        List<EgovMap> rows = parseMeasurements(json);
        log.info("[AirKorea] getMeasurements station={}, pageNo={}, count={}", stationName, pageNo, rows.size());
        return rows;
    }


    // HTTP GET 요청 실행 후 응답( 예외 처리 )
    private String executeGet(URI uri) {
        try {
            return restTemplate.getForObject(uri, String.class);
        } catch (HttpStatusCodeException e) {
            if (HttpStatus.TOO_MANY_REQUESTS.equals(e.getStatusCode())) {
                throw new IllegalStateException("API 호출 한도 초과. serviceKey를 교체하세요.", e);
            }
            throw new IllegalStateException("API 호출 실패. status=" + e.getStatusCode().value(), e);
        } catch (Exception e) {
            throw new IllegalStateException("API 호출 실패: " + e.getMessage(), e);
        }
    }


    // 측정소 목록 API 응답 JSON을 파싱하여 EgovMap 리스트로 변환
    private List<EgovMap> parseStations(String json) {

        /** [JSON 구조]
         *   response > body > items > [ { stationName, addr, year, mangName, item, dmX, dmY, tm }, ... ]  */

        try {
            List<EgovMap> result = new ArrayList<>();

            // JSON 트리에서 items 배열 추출: response > body > items
            JsonNode items = objectMapper.readTree(json)
                    .path("response").path("body").path("items");

            // items가 배열이 아니면 빈 리스트 반환
            if (!items.isArray()) return result;

            for (JsonNode item : items) {
                String stationName = text(item, "stationName");
                if (!StringUtils.hasText(stationName)) continue; // 측정소명 없는 항목 제외

                EgovMap map = new EgovMap();
                map.put("stationName", stationName);
                map.put("addr",     text(item, "addr"));
                map.put("year",     parseInteger(text(item, "year")));              // Integer 변환
                map.put("mangName", text(item, "mangName"));
                map.put("item",     text(item, "item"));
                map.put("dmX",      DateTimeUtils.parseDoubleOrNull(text(item, "dmX"))); // Double 변환
                map.put("dmY",      DateTimeUtils.parseDoubleOrNull(text(item, "dmY"))); // Double 변환
                map.put("tm",       DateTimeUtils.parseDoubleOrNull(text(item, "tm")));  // Double 변환
                result.add(map);
            }
            return result;
        } catch (Exception e) {
            throw new IllegalStateException("측정소 응답 파싱 실패", e);
        }
    }


    // 측정정보 API 응답 JSON을 파싱하여 EgovMap 리스트로 변환
    private List<EgovMap> parseMeasurements(String json) {

        /** [JSON 구조]
         *   response > body > items > [ { stationName, dataTime, so2Value, coValue, o3Value,
         *                                  no2Value, pm10Value, pm25Value, khaiValue, khaiGrade,
         *                                  so2Grade, coGrade, o3Grade, no2Grade, pm10Grade, pm25Grade }, ... ] */

        try {
            List<EgovMap> result = new ArrayList<>();

            JsonNode items = objectMapper.readTree(json)
                    .path("response").path("body").path("items");

            if (!items.isArray()) return result;

            for (JsonNode item : items) {
                String dataTimeStr = text(item, "dataTime");
                if (!StringUtils.hasText(dataTimeStr)) continue;

                LocalDateTime dataTime = DateTimeUtils.parseDateTime(dataTimeStr);
                if (dataTime == null) continue;

                EgovMap map = new EgovMap();
                map.put("stationName", text(item, "stationName"));
                map.put("dataTime",    dataTime);
                map.put("so2Value",    DateTimeUtils.parseDoubleOrNull(text(item, "so2Value")));
                map.put("coValue",     DateTimeUtils.parseDoubleOrNull(text(item, "coValue")));
                map.put("o3Value",     DateTimeUtils.parseDoubleOrNull(text(item, "o3Value")));
                map.put("no2Value",    DateTimeUtils.parseDoubleOrNull(text(item, "no2Value")));
                map.put("pm10Value",   DateTimeUtils.parseDoubleOrNull(text(item, "pm10Value")));
                map.put("pm25Value",   DateTimeUtils.parseDoubleOrNull(text(item, "pm25Value")));
                map.put("khaiValue",   DateTimeUtils.parseDoubleOrNull(text(item, "khaiValue")));
                result.add(map);
            }
            return result;
        } catch (Exception e) {
            throw new IllegalStateException("측정정보 응답 파싱 실패", e);
        }
    }


    // JsonNode에서 특정 필드의 문자열 값 추출
    private String text(JsonNode node, String field) {
        JsonNode val = node.path(field);
        // 값이 빈 문자열이거나 공백만 있으면/필드가 없거나 null이면 → null 반환
        if (val.isMissingNode() || val.isNull()) return null;
        String s = val.asText("").trim();
        return s.isEmpty() ? null : s;
    }


    // 문자열을 Integer로 변환
    private Integer parseInteger(String value) {
        if (!StringUtils.hasText(value)) return null;
        try {
            return Integer.parseInt(value.trim().replace(",", "")); // 천 단위 쉼표(,) 제거 후 파싱
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
