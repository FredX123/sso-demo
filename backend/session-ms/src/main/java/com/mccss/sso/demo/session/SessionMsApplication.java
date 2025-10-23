package com.mccss.sso.demo.session;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.mccss.sso.demo.session",
        "com.mccss.sso.demo.commonlib"           // shared library "common-lib"
})
public class SessionMsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SessionMsApplication.class, args);
    }

}
