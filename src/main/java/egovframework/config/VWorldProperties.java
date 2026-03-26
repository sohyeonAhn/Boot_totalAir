package egovframework.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * application.properties의 vworld.* 설정을 바인딩하는 Properties 클래스
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "vworld")
public class VWorldProperties {

    private String key;
    private Search search = new Search();

    @Getter
    @Setter
    public static class Search {
        private String baseUrl;
    }
}
