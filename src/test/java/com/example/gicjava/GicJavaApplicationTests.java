package com.example.gicjava;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled("Disabled during unit test runs to avoid full application context startup")
@SpringBootTest
class GicJavaApplicationTests {

  @Test
  void contextLoads() {
  }

}
