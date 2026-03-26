package egovframework.air.service;

import org.egovframe.rte.psl.dataaccess.util.EgovMap;

import java.util.List;

/**
 * 대기오염 측정소 서비스 인터페이스
 */
public interface AirService {

    int collectAndSaveAllStations();    // 전체 측정소를 API 수집 후 DB에 저장, 저장된 수 반환

    List<EgovMap> getStationList(EgovMap searchMap);    // 측정소 목록 조회

    void insertStation(EgovMap map);    // 측정소 등록
    void updateStation(EgovMap map);    // 측정소 수정
    void deleteStation(EgovMap map);    // 측정소 삭제

    List<EgovMap> getHistoryList(EgovMap searchMap);          // 대기오염 이력 목록 조회
    List<EgovMap> getHistoryAggregate(EgovMap searchMap);     // 대기오염 이력 집계 (시간별/일별/월별/요일별/계절별)

    void insertHistory(EgovMap map);    // 대기오염 이력 등록
    void updateHistory(EgovMap map);    // 대기오염 이력 수정
    void deleteHistory(EgovMap map);    // 대기오염 이력 삭제

    int collectAndSaveHistory(String startDate, String endDate);    // DB의 전체 측정소에 대해
                                                                    // startDate ~ endDate 의 측정정보를 수집하여 저장
}
