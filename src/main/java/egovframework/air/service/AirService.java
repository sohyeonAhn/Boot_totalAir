package egovframework.air.service;

import org.egovframe.rte.psl.dataaccess.util.EgovMap;

import java.util.List;

/**
 * 대기오염 측정소 서비스 인터페이스
 */
public interface AirService {

    // 전체 측정소를 API에서 수집하여 DB에 저장, 저장된 수 반환
    int collectAndSaveAllStations();

    // DB에서 측정소 목록 조회
    List<EgovMap> getStationList(EgovMap searchMap);

}
