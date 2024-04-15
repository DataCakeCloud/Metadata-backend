package com.lakecat.web.config;

import com.google.common.base.Predicates;
import com.lakecat.web.common.CommonParameters;
import com.lakecat.web.entity.CurrentUser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;


@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
            .apiInfo(apiInfo())
            .pathMapping("/")
            // 选择哪些路径和api会生成document
            .select()
            // 对所有api进行监控
            .apis(RequestHandlerSelectors.basePackage("com.lakecat.web.controller"))
            //错误路径不监控
            .paths(Predicates.not(PathSelectors.regex("/error.*")))
            // 对根下所有路径进行监控
            .paths(PathSelectors.regex("/.*"))
            .build()
                .globalOperationParameters(this.getParameterList());// 全局配置;
    }

    /**
     * 添加head参数配置
     */
    private List<Parameter> getParameterList() {
        ParameterBuilder clientIdTicket = new ParameterBuilder();
        List<Parameter> pars = new ArrayList<Parameter>();
        clientIdTicket.name(CommonParameters.CURRENT_LOGIN_USER).description("login user object: json string")
                .modelRef(new ModelRef("com.lakecat.web.entity.CurrentUser"))
                .parameterType("header")
                .required(false).build(); //设置false，表示clientId参数 非必填,可传可不传！
        pars.add(clientIdTicket.build());
        return pars;
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder().title("Lakecat Web 项目接口文档")
            .description("Swagger动态生成的接口文档")
            .termsOfServiceUrl("NO terms of service")
            //.license("The Apache License, Version 2.0")
            .license("Shareit")
            //.licenseUrl("http://www.apache.org/licenses/LICENSE-2.0.html")
            .version("1.0")
            .build();
    }
}
