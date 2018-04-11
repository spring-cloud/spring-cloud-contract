package com.example.fraud;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class TestController {

    @PostMapping("/tests")
    public Test createNew(@RequestPart MultipartFile file1,
                          @RequestPart MultipartFile file2,
                          @RequestPart Test test) {
        return new Test("ok");
    }
}

class Test {
    private String status;

    public Test(String status) {
        this.status = status;
    }

    public Test() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}