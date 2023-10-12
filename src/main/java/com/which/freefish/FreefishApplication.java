package com.which.freefish;

import com.which.freefish.jwt.JwtAuthenticationFilter;
import com.which.freefish.utils.CanalUtils;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;

import javax.annotation.Resource;

@MapperScan("com.which.freefish.mapper")
@SpringBootApplication
public class FreefishApplication extends SpringBootServletInitializer implements CommandLineRunner {
    @Resource
    private CanalUtils canalUtils;

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(FreefishApplication.class);
    }

    @Bean
    public FilterRegistrationBean jwtFilter() {
        final FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter();
        registrationBean.setFilter(filter);
        return registrationBean;
    }

    public static void main(String[] args) {
        SpringApplication.run(FreefishApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // 项目启动，执行canal客户端监听
        canalUtils.startCanal();
    }

}

