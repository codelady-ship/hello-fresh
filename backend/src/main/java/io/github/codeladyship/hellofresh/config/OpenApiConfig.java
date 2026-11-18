package io.github.codeladyship.hellofresh.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.ExternalDocumentation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI helloFreshOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("HelloFresh clone API")
                        .version("0.1.0")
                        .description("REST API behind the HelloFresh landing page practice clone: meal kits, "
                                + "flexible plans and plan requests. Practice project, not affiliated with HelloFresh.")
                        .license(new License().name("Practice project")))
                .externalDocs(new ExternalDocumentation()
                        .description("Source code")
                        .url("https://github.com/codelady-ship/hello-fresh"));
    }
}
