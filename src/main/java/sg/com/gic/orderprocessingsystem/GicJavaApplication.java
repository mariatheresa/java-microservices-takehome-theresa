package sg.com.gic.orderprocessingsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class GicJavaApplication {

  public static void main(String[] args) {
    SpringApplication.run(GicJavaApplication.class, args);
  }

}
