package com.example.source;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Marcin Grzejszczak
 */
@RestController
class FooController {

    @GetMapping("/foo")
    void foo() {

    }
}
