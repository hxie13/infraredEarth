package cn.ac.sitp.infrared.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "spring.datasource.infrareddb")
public class DataBaseProperties {
    String url;
    String username;
    String password;
    String driverClassName;
}
