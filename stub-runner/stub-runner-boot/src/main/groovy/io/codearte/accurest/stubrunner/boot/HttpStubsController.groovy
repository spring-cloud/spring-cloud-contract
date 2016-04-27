package io.codearte.accurest.stubrunner.boot

import io.codearte.accurest.stubrunner.StubRunning
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * @author Marcin Grzejszczak
 */
@RestController
@RequestMapping("/stubs")
class HttpStubsController {

	private final StubRunning stubRunning

	@Autowired
	HttpStubsController(StubRunning stubRunning) {
		this.stubRunning = stubRunning
	}

	@RequestMapping
	Map<String, Integer> stubs() {
		return stubRunning.runStubs().toIvyToPortMapping()
	}

	@RequestMapping(path = "/{ivy:.*}")
	ResponseEntity<Integer> stub(@PathVariable String ivy) {
		Integer port = stubRunning.runStubs().getPort(ivy)
		if (port) {
			return ResponseEntity.ok(port)
		}
		return new ResponseEntity(HttpStatus.NOT_FOUND)
	}
}
