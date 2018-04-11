package com.example.fraud;

import com.example.fraud.TestController;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.Before;
import org.junit.runner.RunWith;

public class MultipartBase {

    @Before
    public void setUp() throws Exception {
        RestAssuredMockMvc.standaloneSetup(new TestController());
    }
}