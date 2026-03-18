package egovframework.air.controller;

import egovframework.air.service.AirService;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.psl.dataaccess.util.EgovMap;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;


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
    @PostMapping("/api/air/stations")
    public ResponseEntity<List<EgovMap>> getStations(
            @RequestBody(required = false) EgovMap searchMap) {

        if (searchMap == null) searchMap = new EgovMap();
        return ResponseEntity.ok(airService.getStationList(searchMap));
    }

    // 측정소 등록
    @PostMapping("/api/air/stations/insert")
    public ResponseEntity<Map<String, Object>> insertStation(
            @RequestBody EgovMap map) {
        airService.insertStation(map);
        return ResponseEntity.ok(Map.of("result", "success"));
    }

    // 측정소 수정
    @PutMapping("/api/air/stations/{stationId}")
    public ResponseEntity<Map<String, Object>> updateStation(
            @PathVariable Long stationId,
            @RequestBody EgovMap map) {
        map.put("stationId", stationId);
        airService.updateStation(map);
        return ResponseEntity.ok(Map.of("result", "success"));
    }

    // 측정소 삭제
    @DeleteMapping("/api/air/stations/{stationId}")
    public ResponseEntity<Map<String, Object>> deleteStation(
            @PathVariable Long stationId) {
        EgovMap param = new EgovMap();
        param.put("stationId", stationId);
        airService.deleteStation(param);
        return ResponseEntity.ok(Map.of("result", "success"));
    }

    // 측정소별 실시간 측정정보를 지정 기간 동안 수집하여 sub_history_air에 저장
    // Body: { "startDate": "2026-03-01", "endDate": "2026-03-03" }
    @PostMapping("/api/air/history/collect")
    public ResponseEntity<Map<String, Object>> collectHistory(
            @RequestBody(required = false) Map<String, String> body) {
        String startDate = (body != null && body.containsKey("startDate")) ? body.get("startDate") : "2026-03-01";
        String endDate   = (body != null && body.containsKey("endDate"))   ? body.get("endDate")   : "2026-03-03";
        int saved = airService.collectAndSaveHistory(startDate, endDate);
        return ResponseEntity.ok(Map.of("savedCount", saved, "startDate", startDate, "endDate", endDate));
    }


}
