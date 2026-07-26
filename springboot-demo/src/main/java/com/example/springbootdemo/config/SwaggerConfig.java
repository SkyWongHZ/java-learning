package com.example.springbootdemo.config;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestParameterBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ParameterType;
import springfox.documentation.service.RequestParameter;
import springfox.documentation.schema.ScalarType;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableSwagger2
@EnableKnife4j
public class SwaggerConfig {

    @Value("${springfox.documentation.enabled:true}")
    private boolean documentationEnabled;

    @Bean
    public Docket apiDocket() {
        return new Docket(DocumentationType.SWAGGER_2)
                .enable(documentationEnabled)
                .apiInfo(apiInfo())
                .globalRequestParameters(authParameters())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.example.springbootdemo.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    private List<RequestParameter> authParameters() {
        RequestParameter token = new RequestParameterBuilder()
                .name("token")
                .description("登录成功后返回的鉴权令牌")
                .in(ParameterType.HEADER)
                .query(query -> query.model(model -> model.scalarModel(ScalarType.STRING)))
                .required(false)
                .build();
        RequestParameter system = new RequestParameterBuilder()
                .name("system")
                .description("客户端系统类型：1=PC，2=APP，其他取值与bjsm-cloud一致")
                .in(ParameterType.HEADER)
                .query(query -> query.model(model -> model.scalarModel(ScalarType.INTEGER)))
                .required(true)
                .build();
        return Arrays.asList(token, system);
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("springboot-demo 接口文档")
                .description("Spring Boot 分层开发、登录鉴权与业务接口")
                .version("1.1.0")
                .build();
    }
}
