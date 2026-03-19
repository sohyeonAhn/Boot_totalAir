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


    // //////////////////////////////////////////////
    // 측정소 이력
    // //////////////////////////////////////////////

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
    @PostMapping("/api/air/stations/update/{stationId}")
    public ResponseEntity<Map<String, Object>> updateStation(
            @PathVariable String stationId,
            @RequestBody EgovMap map) {
        map.put("stationId", stationId);
        airService.updateStation(map);
        return ResponseEntity.ok(Map.of("result", "success"));
    }

    // 측정소 삭제
    @PostMapping("/api/air/stations/delete/{stationId}")
    public ResponseEntity<Map<String, Object>> deleteStation(
            @PathVariable String stationId) {
        EgovMap param = new EgovMap();
        param.put("stationId", stationId);
        airService.deleteStation(param);
        return ResponseEntity.ok(Map.of("result", "success"));
    }


    // //////////////////////////////////////////////
    // 대기오렴 이력
    // //////////////////////////////////////////////

    // 대기오염 이력 목록 조회 (stationId, startDate, endDate 선택)
    @PostMapping("/api/air/history")
    public ResponseEntity<List<EgovMap>> getHistoryList(
            @RequestBody(required = false) EgovMap searchMap) {
        if (searchMap == null) searchMap = new EgovMap();
        return ResponseEntity.ok(airService.getHistoryList(searchMap));
    }

    // 대기오염 이력 등록
    @PostMapping("/api/air/history/insert")
    public ResponseEntity<Map<String, Object>> insertHistory(
            @RequestBody EgovMap map) {
        airService.insertHistory(map);
        return ResponseEntity.ok(Map.of("result", "success"));
    }

    // 대기오염 이력 수정 (stationId + dataTime 으로 대상 지정)
    @PostMapping("/api/air/history/update")
    public ResponseEntity<Map<String, Object>> updateHistory(
            @RequestBody EgovMap map) {
        airService.updateHistory(map);
        return ResponseEntity.ok(Map.of("result", "success"));
    }

    // 대기오염 이력 삭제 (historyId + dataTime 둘 다 일치해야 삭제)
    @PostMapping("/api/air/history/delete")
    public ResponseEntity<Map<String, Object>> deleteHistory(
            @RequestBody EgovMap map) {
        airService.deleteHistory(map);
        return ResponseEntity.ok(Map.of("result", "success"));
    }

    // 측정소별 실시간 측정정보를 지정 기간 동안 수집하여 sub_history_air에 저장
    @PostMapping("/api/air/history/collect")
    public ResponseEntity<Map<String, Object>> collectHistory(
            @RequestBody(required = false) Map<String, String> body) {
        String startDate = (body != null && body.containsKey("startDate")) ? body.get("startDate") : "2026-03-01";
        String endDate   = (body != null && body.containsKey("endDate"))   ? body.get("endDate")   : "2026-03-03";
        int saved = airService.collectAndSaveHistory(startDate, endDate);
        return ResponseEntity.ok(Map.of("savedCount", saved, "startDate", startDate, "endDate", endDate));
    }


}
