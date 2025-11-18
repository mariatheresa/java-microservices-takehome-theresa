package com.example.gicjava.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI gicOpenAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("GIC-Java API")
            .version("0.0.1")
            .description("Order / Payment / Notification APIs for the take-home test")
            .termsOfService("https://example.com/terms")
            .license(new License().name("MIT").url("https://opensource.org/licenses/MIT"))
            .contact(new Contact().name("GIC Developer").email("dev@example.com")));
  }


}
