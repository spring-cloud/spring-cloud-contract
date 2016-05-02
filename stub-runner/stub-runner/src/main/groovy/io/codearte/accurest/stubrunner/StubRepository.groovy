package io.codearte.accurest.stubrunner

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.util.logging.Slf4j
import io.codearte.accurest.dsl.GroovyDsl
import io.codearte.accurest.util.AccurestDslConverter
/**
 * Wraps the folder with WireMock mappings.
 */
@CompileStatic
@PackageScope
@Slf4j
class StubRepository {

	private final File path
	final List<WiremockMappingDescriptor> projectDescriptors
	final Collection<GroovyDsl> accurestContracts

	StubRepository(File repository) {
		if (!repository.isDirectory()) {
			throw new FileNotFoundException("Missing descriptor repository under path [$path]")
		}
		this.path = repository
		this.projectDescriptors = projectDescriptors()
		this.accurestContracts = accurestContracts()
	}

	/**
	 * Returns a list of {@link GroovyDsl}
	 */
	private Collection<GroovyDsl> accurestContracts() {
		List<GroovyDsl> contracts = []
		contracts.addAll(accurestDescriptors())
		return contracts
	}

	/**
	 * Returns the list of WireMock JSON files wrapped in {@link WiremockMappingDescriptor}
	 */
	private List<WiremockMappingDescriptor> projectDescriptors() {
		List<WiremockMappingDescriptor> mappingDescriptors = []
		mappingDescriptors.addAll(contextDescriptors())
		return mappingDescriptors
	}

	private List<WiremockMappingDescriptor> contextDescriptors() {
		return path.exists() ? collectMappingDescriptors(path) : []
	}

	private List<WiremockMappingDescriptor> collectMappingDescriptors(File descriptorsDirectory) {
		List<WiremockMappingDescriptor> mappingDescriptors = []
		descriptorsDirectory.eachFileRecurse { File file ->
			if (isMappingDescriptor(file)) {
				mappingDescriptors << new WiremockMappingDescriptor(file)
			}
		}
		return mappingDescriptors
	}

	private Collection<GroovyDsl> accurestDescriptors() {
		return path.exists() ? collectAccurestDescriptors(path) : []
	}

	private Collection<GroovyDsl> collectAccurestDescriptors(File descriptorsDirectory) {
		List<GroovyDsl> mappingDescriptors = []
		descriptorsDirectory.eachFileRecurse { File file ->
			if (isAccurestDescriptor(file)) {
				try {
					mappingDescriptors << AccurestDslConverter.convert(file)
				} catch (Exception e) {
					log.warn("Exception occurred while trying to parse file [$file]", e)
				}
			}
		}
		return mappingDescriptors
	}

	private static boolean isMappingDescriptor(File file) {
		return file.isFile() && file.name.endsWith('.json')
	}

	private static boolean isAccurestDescriptor(File file) {
		//TODO: Consider script injections implications...
		return file.isFile() && file.name.endsWith('.groovy')
	}

}
