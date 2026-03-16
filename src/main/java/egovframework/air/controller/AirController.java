package egovframework.air.controller;

import egovframework.air.service.AirService;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.psl.dataaccess.util.EgovMap;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequiredArgsConstructor
public class AirController {

    private final AirService airService;

    // AirKorea API 전체 측정소 수집 후 DB 저장
    @PostMapping("/api/air/stations/collect")
    public ResponseEntity<Map<String, Object>> collectAllStations() {
        int saved = airService.collectAndSaveAllStations();
        return ResponseEntity.ok(Map.of("savedCount", saved));
    }

    // 측정소 목록
    // @GetMapping("/api/stations") // React 용
    @GetMapping("/api/air/stations") // PostMan 용
    public ResponseEntity<List<EgovMap>> getStations(
            @RequestParam(required = false) String keyword) {

        EgovMap searchMap = new EgovMap();
        searchMap.put("keyword", keyword);
        return ResponseEntity.ok(airService.getStationList(searchMap));
    }


}
