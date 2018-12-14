package com.example.fraud;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.Before;

public class MultipartBase {

	@Before
	public void setUp() throws Exception {
		RestAssuredMockMvc.standaloneSetup(new TestController());
	}
}