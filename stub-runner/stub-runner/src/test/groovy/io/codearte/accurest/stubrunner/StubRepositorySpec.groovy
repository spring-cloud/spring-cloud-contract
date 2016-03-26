package io.codearte.accurest.stubrunner

import spock.lang.Specification

class StubRepositorySpec extends Specification {
	public static
	final File REPOSITORY_LOCATION = new File('src/test/resources/repository')

	def 'should retrieve all descriptors for given project'() {
		given:
		StubRepository repository = new StubRepository(REPOSITORY_LOCATION)
		int expectedDescriptorsSize = 8
		when:
		List<MappingDescriptor> descriptors = repository.getProjectDescriptors()
		then:
		descriptors.size() == expectedDescriptorsSize
	}

	def 'should return empty list if files are missing'() {
		given:
		StubRepository repository = new StubRepository(new File('src/test/resources/emptyrepo'))
		when:
		List<MappingDescriptor> descriptors = repository.getProjectDescriptors()
		then:
		descriptors.empty
	}

	def 'should throw an exception if directory with mappings is missing'() {
		when:
		new StubRepository(new File('src/test/resources/nonexistingrepo'))
		then:
		thrown(FileNotFoundException)
	}
}
