package hello;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
class PersonService {

	private final Map<Long, Person> personMap;
	
	public PersonService() {
		personMap = new HashMap<>();
		personMap.put(1L, new Person(1L, "Richard", "Gere"));
		personMap.put(2L, new Person(2L, "Emma", "Choplin"));
		personMap.put(3L, new Person(3L, "Anna", "Carolina"));
	}
	
	Person findPersonById(Long id) {
		return personMap.get(id);
	}
}
