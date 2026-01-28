package cn.ac.sitp.infrared;

import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.logging.stdout.StdOutImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        LogFactory.useCustomLogging(StdOutImpl.class);
        SpringApplication springApplication = new SpringApplication(Application.class);
        springApplication.run(args);
    }
}
