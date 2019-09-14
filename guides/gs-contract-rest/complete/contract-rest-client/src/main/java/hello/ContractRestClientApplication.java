package hello;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class ContractRestClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(ContractRestClientApplication.class, args);
	}
}

@RestController
class MessageRestController {

	private final RestTemplate restTemplate;

	MessageRestController(RestTemplateBuilder restTemplateBuilder) {
		this.restTemplate = restTemplateBuilder.build();
	}

	@RequestMapping("/message/{personId}")
	String getMessage(@PathVariable("personId") Long personId) {
		Person person = this.restTemplate.getForObject("http://localhost:8000/person/{personId}", Person.class, personId);
		return "Hello " + person.getName();
	}

}
