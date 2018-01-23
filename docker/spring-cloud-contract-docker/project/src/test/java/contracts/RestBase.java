package contracts;

import io.restassured.RestAssured;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.validator.ValidateWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;

/**
 * @author Marcin Grzejszczak
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = RestBase.Config.class,
		webEnvironment = SpringBootTest.WebEnvironment.NONE)
public abstract class RestBase {

	@Value("${APPLICATION_BASE_URL}") String url;
	@Value("${APPLICATION_USERNAME:}") String username;
	@Value("${APPLICATION_PASSWORD:}") String password;

	@Before
	public void setup() {
		RestAssured.baseURI = this.url;
		if (StringUtils.hasText(this.username)) {
			RestAssured.authentication =
					RestAssured.basic(this.username, this.password);
		}
	}

	@Configuration
	@EnableAutoConfiguration
	protected static class Config {

	}
}
