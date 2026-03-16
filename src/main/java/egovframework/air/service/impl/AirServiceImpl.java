package egovframework.air.service.impl;

import egovframework.air.dao.AirDao;
import egovframework.air.service.AirService;
import egovframework.client.AirKoreaClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egovframe.rte.psl.dataaccess.util.EgovMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    private final AirDao airDao;         

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


    // DB에 저장된 측정소 목록 조회 (keyword 부분 일치)
    @Override
    @Transactional(readOnly = true)
    public List<EgovMap> getStationList(EgovMap searchMap) {
        return airDao.selectStationList(searchMap);
    }


}
