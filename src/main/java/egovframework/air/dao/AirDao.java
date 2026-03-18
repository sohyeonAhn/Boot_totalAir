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

    // 측정소 Upsert (station_name 충돌 시 UPDATE)
    void upsertStation(EgovMap map);

    // 측정소 등록
    void insertStation(EgovMap map);

    // 측정소 수정
    int updateStation(EgovMap map);

    // 측정소 삭제
    int deleteStation(EgovMap map);

    // 측정소 목록 조회 (keyword 부분 일치 검색: station_name / addr / mang_name)
    List<EgovMap> selectStationList(EgovMap searchMap);

    // 저장된 측정소명 전체 목록 조회
    List<String> selectAllStationNames();

    // 특정 기간에 이미 데이터가 저장된 측정소명 목록 조회
    List<String> selectCollectedStationNames(EgovMap param);

    // 실시간 측정정보 Upsert (station_name + data_time 충돌 시 UPDATE)
    void upsertHistory(EgovMap map);
}
