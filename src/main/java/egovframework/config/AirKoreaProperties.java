package egovframework.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * application.properties의 airkorea.* 설정을 바인딩하는 Properties 클래스
 * - @Value 대신 이 클래스를 주입받아 사용하면 IDE 경고 제거 및 타입 안전성 확보
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "airkorea")
public class AirKoreaProperties {

    private String serviceKey;

    private Msrstn msrstn = new Msrstn();
    private Arpltn arpltn = new Arpltn();

    @Getter
    @Setter
    public static class Msrstn {
        private String baseUrl;
    }

    @Getter
    @Setter
    public static class Arpltn {
        private String baseUrl;
    }
}
