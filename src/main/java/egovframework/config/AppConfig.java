package egovframework.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * 애플리케이션 공통 Bean 설정
 */
@Configuration
public class AppConfig {


    // AirKorea API HTTP 통신에 사용하는 RestTemplate Bean 등록
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * API 응답 JSON 파싱에 사용하는 ObjectMapper Bean 등록
     * - JavaTimeModule: LocalDateTime 등 Java 8 날짜 타입 직렬화/역직렬화 지원
     * - FAIL_ON_UNKNOWN_PROPERTIES false: API 응답에 알 수 없는 필드가 있어도 예외 미발생
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }
}
