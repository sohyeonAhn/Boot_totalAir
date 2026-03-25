package egovframework.air.controller;

import egovframework.client.VWorldClient;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.psl.dataaccess.util.EgovMap;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * VWorld 주소 검색 API 컨트롤러
 */
@RestController
@RequestMapping("/api/address")
@RequiredArgsConstructor
public class AddressController {

    private final VWorldClient vWorldClient;


    /**
     * 주소 검색 (VWorld Search API)
     *
     * [요청 Body]
     *   { "query": "검색어", "page": 1, "size": 10 }
     *
     * [응답]
     *   [ { "title": "주소명", "road": "도로명주소", "parcel": "지번주소", "x": "경도", "y": "위도" }, ... ]
     */
    @PostMapping("/search")
    public ResponseEntity<?> searchAddress(@RequestBody Map<String, Object> body) {
        String query = (String) body.getOrDefault("query", "");
        if (!StringUtils.hasText(query)) {
            return ResponseEntity.badRequest().body(Map.of("error", "검색어(query)를 입력해주세요."));
        }

        int page = ((Number) body.getOrDefault("page", 1)).intValue();
        int size = ((Number) body.getOrDefault("size", 10)).intValue();

        List<EgovMap> result = vWorldClient.searchAddress(query, page, size);
        return ResponseEntity.ok(result);
    }
}
