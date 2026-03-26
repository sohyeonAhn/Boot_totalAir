package egovframework.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import egovframework.config.VWorldProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egovframe.rte.psl.dataaccess.util.EgovMap;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * VWorld 주소 검색 API 클라이언트
 *
 * [API 문서] https://www.vworld.kr/dev/v4dv_search2_s001.do
 * [처리 흐름]
 *   searchAddress() → executeGet() → parseAddresses() → EgovMap 리스트 반환
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VWorldClient {

    private final RestTemplate    restTemplate;
    private final ObjectMapper    objectMapper;
    private final VWorldProperties props;


    /**
     * 주소 검색 (도로명 + 지번 통합)
     *
     * @param query 검색어
     * @param page  페이지 번호
     * @param size  페이지당 결과 수
     * @return 검색 결과 목록 (title, road, parcel, x, y)
     */
    public List<EgovMap> searchAddress(String query, int page, int size) {
        URI uri = UriComponentsBuilder
                .fromHttpUrl(props.getSearch().getBaseUrl())
                .queryParam("service",     "search")
                .queryParam("request",     "search")
                .queryParam("version",     "2.0")
                .queryParam("crs",         "EPSG:4326")
                .queryParam("size",        size)
                .queryParam("page",        page)
                .queryParam("query",       query)
                .queryParam("type",        "ADDRESS")
                .queryParam("category",    "ROAD")
                .queryParam("format",      "json")
                .queryParam("errorFormat", "json")
                .queryParam("key",         props.getKey())
                .build()
                .encode()
                .toUri();

        String json = executeGet(uri);
        return parseAddresses(json);
    }


    // HTTP GET 요청 실행 후 응답 문자열 반환
    private String executeGet(URI uri) {
        try {
            return restTemplate.getForObject(uri, String.class);
        } catch (Exception e) {
            throw new IllegalStateException("VWorld API 호출 실패: " + e.getMessage(), e);
        }
    }


    /**
     * 주소 검색 응답 JSON을 파싱하여 EgovMap 리스트로 변환
     *
     * [JSON 구조]
     *   response > result > items > [{ id, title, point:{x, y}, address:{road, parcel} }, ...]
     */
    private List<EgovMap> parseAddresses(String json) {
        try {
            List<EgovMap> result = new ArrayList<>();

            JsonNode root   = objectMapper.readTree(json);
            String   status = root.path("response").path("status").asText();

            if (!"OK".equalsIgnoreCase(status)) {
                if ("NOT_FOUND".equalsIgnoreCase(status)) {
                    log.debug("[VWorld] 검색 결과 없음 status=NOT_FOUND");
                } else {
                    String errCode = root.path("response").path("error").path("code").asText("-");
                    String errText = root.path("response").path("error").path("text").asText("-");
                    log.warn("[VWorld] API 오류 status={}, code={}, text={}", status, errCode, errText);
                }
                return result;
            }

            JsonNode items = root.path("response").path("result").path("items");
            if (!items.isArray()) return result;

            for (JsonNode item : items) {
                EgovMap map = new EgovMap();
                map.put("title",  text(item, "title"));
                map.put("road",   text(item.path("address"), "road"));   // 도로명주소
                map.put("parcel", text(item.path("address"), "parcel")); // 지번주소
                map.put("x",      text(item.path("point"), "x"));        // 경도 (lng)
                map.put("y",      text(item.path("point"), "y"));        // 위도 (lat)
                result.add(map);
            }
            return result;
        } catch (Exception e) {
            throw new IllegalStateException("VWorld 주소 응답 파싱 실패", e);
        }
    }


    // JsonNode에서 특정 필드의 문자열 값 추출 (없거나 빈 값이면 null 반환)
    private String text(JsonNode node, String field) {
        JsonNode val = node.path(field);
        if (val.isMissingNode() || val.isNull()) return null;
        String s = val.asText("").trim();
        return s.isEmpty() ? null : s;
    }
}
