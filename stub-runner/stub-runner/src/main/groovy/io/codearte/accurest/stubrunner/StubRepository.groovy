package io.codearte.accurest.stubrunner

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.util.logging.Slf4j
import io.codearte.accurest.dsl.GroovyDsl
import org.codehaus.groovy.control.CompilerConfiguration

/**
 * Wraps the folder with WireMock mappings.
 */
@CompileStatic
@PackageScope
@Slf4j
class StubRepository {

	private final File path

	StubRepository(File repository) {
		if (!repository.isDirectory()) {
			throw new FileNotFoundException('Missing descriptor repository')
		}
		this.path = repository
	}

	Collection<GroovyDslWrapper> getAccurestContracts() {
		List<GroovyDslWrapper> contracts = []
		contracts.addAll(accurestDescriptors())
		return contracts
	}

	/**
	 * Returns the list of WireMock JSON files wrapped in {@link WiremockMappingDescriptor}
	 */
	List<WiremockMappingDescriptor> getProjectDescriptors() {
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

	private Collection<GroovyDslWrapper> accurestDescriptors() {
		return path.exists() ? collectAccurestDescriptors(path) : []
	}

	private Collection<GroovyDslWrapper> collectAccurestDescriptors(File descriptorsDirectory) {
		List<GroovyDslWrapper> mappingDescriptors = []
		descriptorsDirectory.eachFileRecurse { File file ->
			if (isAccurestDescriptor(file)) {
				try {
					mappingDescriptors << new GroovyDslWrapper(
							((new GroovyShell(delegate.class.classLoader, new Binding(), new CompilerConfiguration(sourceEncoding:'UTF-8')).evaluate(file)) as GroovyDsl)
					)
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
