package hello

import com.jayway.restassured.module.mockmvc.RestAssuredMockMvc
import spock.lang.Specification

public class BaseAccurest extends Specification {

    def setup() {
        RestAssuredMockMvc.standaloneSetup(new GreetingController())
    }

}