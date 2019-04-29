package hello;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import io.restassured.module.mockmvc.RestAssuredMockMvc;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ContractRestServiceApplication.class)
public abstract class BaseClass {

	@Autowired PersonRestController personRestController;

	@MockBean PersonService personService;

	@Before public void setup() {
		RestAssuredMockMvc.standaloneSetup(personRestController);

		Mockito.when(personService.findPersonById(1L))
				.thenReturn(new Person(1L, "foo", "bee"));
	}

}
