package io.codearte.accurest.file

import com.google.common.collect.Multimap
import spock.lang.Specification

import java.nio.file.Path

/**
 * @author Jakub Kubrynski
 */
class ContractFileScannerSpec extends Specification {

	def "should find contract files"() {
		given:
			File baseDir = new File(this.getClass().getResource("/directory/with/stubs").toURI())
			Set<String> excluded = ["package/**"] as Set
			Set<String> ignored = ["other/different/**"] as Set
			ContractFileScanner scanner = new ContractFileScanner(baseDir, excluded, ignored)
		when:
			Multimap<Path, Contract> result = scanner.findContracts()
		then:
			result.keySet().size() == 3
			result.get(baseDir.toPath().resolve("different")).size() == 1
			result.get(baseDir.toPath().resolve("other")).size() == 2
		and:
			Collection<Contract> ignoredSet = result.get(baseDir.toPath().resolve("other").resolve("different"))
			ignoredSet.size() == 1
			ignoredSet.ignored == [true]
	}
}
