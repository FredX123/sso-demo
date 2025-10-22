package com.mccss.sso.demo.application.fo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.mccss.sso.demo.application.fo",
        "com.mccss.sso.demo.commonlib"           // shared library "common-lib"
})
public class FrontofficeMsApplication {

    public static void main(String[] args) {
        SpringApplication.run(FrontofficeMsApplication.class, args);
    }

}
