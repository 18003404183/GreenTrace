package com.neusoft.envsupervision.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI envSupervisionOpenApi() {
        return new OpenAPI().info(new Info()
                .title("环保公众监督系统 API")
                .version("1.0.0")
                .description("公众举报、任务派单、设备监测、监督预警、操作日志和报表导出接口。"));
    }
}
