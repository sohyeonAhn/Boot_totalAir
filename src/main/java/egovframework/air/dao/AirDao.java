package egovframework.air.dao;

import org.apache.ibatis.annotations.Mapper;
import org.egovframe.rte.psl.dataaccess.util.EgovMap;

import java.util.List;

/**
 * 대기오염 측정소 DAO
 * - @Mapper 로 AirMapper.xml의 SQL과 연결
 */
@Mapper
public interface AirDao {

    // //////////////////////////////////////////////
    // 측정소 이력
    // //////////////////////////////////////////////

    void upsertStation(EgovMap map);    // 측정소 Upsert (station_name 충돌 시 UPDATE)
    void insertStation(EgovMap map);    // 측정소 등록
    int updateStation(EgovMap map);     // 측정소 수정
    int deleteStation(EgovMap map);     // 측정소 삭제

    List<EgovMap> selectStationList(EgovMap searchMap);         // 측정소 목록 조회 (keyword 부분 일치 검색)
    List<String> selectAllStationNames();                       // 저장된 측정소명 전체 목록 조회
    List<String> selectCollectedStationNames(EgovMap param);    // 특정 기간에 이미 데이터가 저장된 측정소명 목록 조회

    // //////////////////////////////////////////////
    // 대기오염 이력
    // //////////////////////////////////////////////

    List<EgovMap> selectHistoryList(EgovMap searchMap);      // 대기오염 이력 목록 조회
    List<EgovMap> selectHistoryAggregate(EgovMap searchMap); // 대기오염 이력 집계 (시간별/일별/월별/요일별/계절별)

    void insertHistory(EgovMap map);    // 대기오염 이력 등록
    int updateHistory(EgovMap map);     // 대기오염 이력 수정
    int deleteHistory(EgovMap map);     // 대기오염 이력 삭제
    void upsertHistory(EgovMap map);    // 실시간 측정정보 Upsert (station_name + data_time 충돌 시 UPDATE)
}
