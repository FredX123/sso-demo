package com.mccss.sso.demo.backoffice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.mccss.sso.demo.backoffice",
        "com.mccss.sso.demo.commonlib"           // shared library "common-lib"
})
public class BackofficeMsApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackofficeMsApplication.class, args);
    }

}
