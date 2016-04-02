package hello;

import com.jayway.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.Before;

public class BaseAccurest {

    @Before
    public void setup() {
        RestAssuredMockMvc.standaloneSetup(new GreetingController());
    }

}
