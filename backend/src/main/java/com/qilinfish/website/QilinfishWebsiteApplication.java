package com.qilinfish.website;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Qilinfish Studio website — entry point.
 *
 * Architecture (MVVM under Spring Boot 3 + Thymeleaf):
 *   - Model       : pure data classes under model/
 *   - ViewBinder  : view-bound objects under vb/  (the "VM" in MVVM)
 *   - Controller  : controller/ — exposes ViewBinder to Thymeleaf views
 *   - View        : src/main/resources/templates/
 *
 * Naming rule: methods use lowerCamelCase double-hump, and all function
 * parameters are prefixed with `__` (e.g. doSomething(String __name)).
 */
@SpringBootApplication
public class QilinfishWebsiteApplication {

    public static void main(String[] __args) {
        SpringApplication.run(QilinfishWebsiteApplication.class, __args);
    }
}
