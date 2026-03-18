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

    // 측정소 등록
    void insertStation(EgovMap map);

    // 측정소 수정
    void updateStation(EgovMap map);

    // 측정소 삭제
    void deleteStation(EgovMap map);

    // DB의 전체 측정소에 대해 startDate ~ endDate 범위의 측정정보를 API에서 수집하여 sub_history_air에 저장
    int collectAndSaveHistory(String startDate, String endDate);

}
