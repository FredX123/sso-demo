package com.mccss.sso.demo.application.myb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.mccss.sso.demo.application.myb",
        "com.mccss.sso.demo.commonlib"           // shared library "common-lib"
})
public class MybMsApplication {

    public static void main(String[] args) {
        SpringApplication.run(MybMsApplication.class, args);
    }

}
