package io.codearte.accurest.stubrunner

import groovy.transform.CompileStatic
import groovy.transform.PackageScope

/**
 * Wraps the folder with WireMock mappings.
 */
@CompileStatic
@PackageScope
class StubRepository {

	private final File path

	StubRepository(File repository) {
		if (!repository.isDirectory()) {
			throw new FileNotFoundException('Missing descriptor repository')
		}
		this.path = repository
	}

	/**
	 * Returns the list of WireMock JSON files wrapped in {@link MappingDescriptor}
	 */
	List<MappingDescriptor> getProjectDescriptors() {
		List<MappingDescriptor> mappingDescriptors = []
		mappingDescriptors.addAll(contextDescriptors())
		return mappingDescriptors
	}

	private List<MappingDescriptor> contextDescriptors() {
		return path.exists() ? collectMappingDescriptors(path) : []
	}

	private List<MappingDescriptor> collectMappingDescriptors(File descriptorsDirectory) {
		List<MappingDescriptor> mappingDescriptors = []
		descriptorsDirectory.eachFileRecurse { File file ->
			if (isMappingDescriptor(file)) {
				mappingDescriptors << new MappingDescriptor(file)
			}
		}
		return mappingDescriptors
	}

	private static boolean isMappingDescriptor(File file) {
		return file.isFile() && file.name.endsWith('.json')
	}

}
