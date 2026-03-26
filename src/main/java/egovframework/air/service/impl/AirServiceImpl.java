package egovframework.air.service.impl;

import egovframework.air.dao.AirDao;
import egovframework.air.service.AirService;
import egovframework.client.AirKoreaClient;
import egovframework.client.VWorldClient;
import egovframework.util.CityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egovframe.rte.psl.dataaccess.util.EgovMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 대기오염 측정소 Service 구현체
    - AirKoreaClient가 반환하는 EgovMap을 DAO로 바로 전달
 */
@Slf4j
@Service("airService")
@RequiredArgsConstructor
@Transactional
public class AirServiceImpl implements AirService {

    private final AirKoreaClient airKoreaClient;
    private final VWorldClient   vWorldClient;
    private final AirDao         airDao;

    // AirKorea API 1회 호출 시 가져올 최대 행 수
    private static final int NUM_OF_ROWS = 100;


    // 전체 측정소를 AirKorea API에서 수집하여 DB에 저장
    @Override
    public int collectAndSaveAllStations() {
        int total  = 0;
        int pageNo = 1;

        while (true) {
            // API 호출: 해당 페이지의 측정소 목록 수신
            List<EgovMap> rows = airKoreaClient.getStations(pageNo, NUM_OF_ROWS);

            // 수신한 행을 하나씩 DB에 Upsert
            for (EgovMap row : rows) {
                // API 응답의 addr로 city 자동 설정
                row.put("city", CityUtils.extractCity((String) row.get("addr")));
                airDao.upsertStation(row);
                total++;
            }

            // 마지막 페이지 도달 시 종료 (응답 건수 < 요청 건수)
            if (rows.size() < NUM_OF_ROWS) break;
            pageNo++;
        }

        log.info("[AirService] 전체 측정소 수집 완료 total={}", total);
        return total;
    }

    // //////////////////////////////////////////////
    // 측정소 이력
    // //////////////////////////////////////////////

    // DB에 저장된 측정소 목록 조회 (keyword 부분 일치)
    @Override
    @Transactional(readOnly = true)
    public List<EgovMap> getStationList(EgovMap searchMap) {
        return airDao.selectStationList(searchMap);
    }

    // 측정소 등록 (addr 입력 시 VWorld로 도로명주소·좌표 자동 보정)
    @Override
    public void insertStation(EgovMap map) {
        resolveAddrCoords(map);
        airDao.insertStation(map);
    }

    // 측정소 수정 (addr 입력 시 VWorld로 도로명주소·좌표 자동 보정)
    @Override
    public void updateStation(EgovMap map) {
        resolveAddrCoords(map);
        int affected = airDao.updateStation(map);
        if (affected == 0) {
            throw new IllegalArgumentException("수정할 측정소가 없습니다. stationId=" + map.get("stationId"));
        }
    }

    // 입력한 주소를 VWorld 검색 API로 조회
    // 도로명주소와 좌표를 map에 자동 보정, 이후 city 자동 설정
    private void resolveAddrCoords(EgovMap map) {
        String addr = (String) map.get("addr");
        if (!StringUtils.hasText(addr)) return;  // addr 없으면 보정 불필요

        // 가장 일치도 높은 결과 1건만 요청 (page=1, size=1)
        List<EgovMap> results = vWorldClient.searchAddress(addr, 1, 1);
        if (results.isEmpty()) {
            // VWorld 검색 결과가 없어도 원본 addr로 city 추출
            map.put("city", CityUtils.extractCity(addr));
            return;
        }

        EgovMap hit = results.get(0);
        String road = (String) hit.get("road");  // 도로명주소
        String x    = (String) hit.get("x");     // 경도
        String y    = (String) hit.get("y");     // 위도

        if (StringUtils.hasText(road)) map.put("addr", road);  // 도로명주소로 정규화
        if (StringUtils.hasText(x))    map.put("dmX",  x);     // 경도 자동 채움
        if (StringUtils.hasText(y))    map.put("dmY",  y);     // 위도 자동 채움

        // 정규화된 주소(도로명주소 우선, 없으면 원본)로 city 추출
        String resolvedAddr = StringUtils.hasText(road) ? road : addr;
        map.put("city", CityUtils.extractCity(resolvedAddr));
    }


    // 측정소 삭제
    @Override
    public void deleteStation(EgovMap map) {
        int affected = airDao.deleteStation(map);
        if (affected == 0) {
            throw new IllegalArgumentException("삭제할 측정소가 없습니다. stationId=" + map.get("stationId"));
        }
    }


    // //////////////////////////////////////////////
    // 대기오렴 이력
    // //////////////////////////////////////////////

    // 대기오염 이력 목록 조회
    @Override
    @Transactional(readOnly = true)
    public List<EgovMap> getHistoryList(EgovMap searchMap) {
        return airDao.selectHistoryList(searchMap);
    }

    // 대기오염 이력 등록
    @Override
    public void insertHistory(EgovMap map) {
        airDao.insertHistory(map);
    }

    // 대기오염 이력 수정
    @Override
    public void updateHistory(EgovMap map) {
        int affected = airDao.updateHistory(map);
        if (affected == 0) {
            throw new IllegalArgumentException("수정할 이력이 없습니다. stationId=" + map.get("stationId") + ", dataTime=" + map.get("dataTime"));
        }
    }

    // 대기오염 이력 삭제
    @Override
    public void deleteHistory(EgovMap map) {
        int affected = airDao.deleteHistory(map);
        if (affected == 0) {
            throw new IllegalArgumentException("삭제할 이력이 없습니다. stationId=" + map.get("stationId") + ", dataTime=" + map.get("dataTime"));
        }
    }

    // DB의 전체 측정소에 대해 startDate ~ endDate 범위의 측정정보를 API에서 수집하여 sub_history_air에 저장
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public int collectAndSaveHistory(String startDate, String endDate) {
        LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
        LocalDateTime end   = LocalDate.parse(endDate).atTime(23, 59, 59);

        // 이미 수집된 측정소는 API 호출 생략
        EgovMap rangeParam = new EgovMap();
        rangeParam.put("startTime", start);
        rangeParam.put("endTime",   end);
        Set<String> alreadyCollected = new HashSet<>(airDao.selectCollectedStationNames(rangeParam));

        List<String> stationNames = airDao.selectAllStationNames();
        int total = 0;

        for (String stationName : stationNames) {
            if (alreadyCollected.contains(stationName)) {
                log.debug("[AirService] 이미 수집됨, 건너뜀 station={}", stationName);
                continue;
            }

            int pageNo = 1;
            while (true) {
                List<EgovMap> rows = airKoreaClient.getMeasurements(stationName, "MONTH", pageNo, NUM_OF_ROWS);

                for (EgovMap row : rows) {
                    LocalDateTime dt = (LocalDateTime) row.get("dataTime");
                    if (dt == null || dt.isBefore(start) || dt.isAfter(end)) continue;

                    row.put("stationName", stationName);
                    airDao.upsertHistory(row);
                    total++;
                }

                if (rows.size() < NUM_OF_ROWS) break;
                pageNo++;
            }
        }

        log.info("[AirService] 측정정보 이력 수집 완료 startDate={}, endDate={}, total={}", startDate, endDate, total);
        return total;
    }


}
