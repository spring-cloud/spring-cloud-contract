package io.codearte.accurest.file

import com.google.common.collect.ListMultimap
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
			ListMultimap<Path, Contract> result = scanner.findContracts()
		then:
			result.keySet().size() == 3
			result.get(baseDir.toPath().resolve("different")).size() == 1
			result.get(baseDir.toPath().resolve("other")).size() == 2
		and:
			Collection<Contract> ignoredSet = result.get(baseDir.toPath().resolve("other").resolve("different"))
			ignoredSet.size() == 1
			ignoredSet.ignored == [true]
	}

	def "should find contracts group in scenario"() {
		given:
			File baseDir = new File(this.getClass().getResource("/directory/with/scenario").toURI())
			ContractFileScanner scanner = new ContractFileScanner(baseDir, [] as Set, [] as Set)
		when:
			ListMultimap<Path, Contract> contracts = scanner.findContracts()
		then:
			contracts.values().size() == 3
			contracts.values().find { it.path.fileName.toString().startsWith('01') }.groupSize == 3
			contracts.values().find { it.path.fileName.toString().startsWith('01') }.order == 0
			contracts.values().find { it.path.fileName.toString().startsWith('02') }.order == 1
			contracts.values().find { it.path.fileName.toString().startsWith('03') }.order == 2
	}
}
