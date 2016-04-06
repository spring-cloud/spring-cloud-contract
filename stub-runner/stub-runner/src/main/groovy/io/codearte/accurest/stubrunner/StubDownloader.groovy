package io.codearte.accurest.stubrunner

import groovy.grape.Grape
import groovy.util.logging.Slf4j
import io.codearte.accurest.stubrunner.util.StringUtils
import io.codearte.accurest.stubrunner.util.ZipCategory

import static java.nio.file.Files.createTempDirectory

/**
 * Downloads stubs from an external repository and unpacks them locally
 */
@Slf4j
class StubDownloader {

	private static final String LATEST_MODULE = '*'
	private static final String REPOSITORY_NAME = 'dependency-repository'
	private static final String STUB_RUNNER_TEMP_DIR_PREFIX = 'stub-runner'
	private static final String GRAPE_CONFIG = 'grape.config'
	private static final String STUB_RUNNER_GRAPE_CONFIG = "accurest.stubrunner.grape.config"

	/**
	 * Downloads stubs from an external repository and unpacks them locally.
	 * Depending on the switch either uses only local repository to check for
	 * stub presence.
	 *
	 * @param workOffline -flag that defines whether only local cache should be used
	 * @param stubRepositoryRoot - address of the repo from which deps should be grabbed
	 * @param stubsGroup - group name of the jar containing stubs
	 * @param stubsModule - artifact id with a classifier name of the jar containing stubs
	 * @return file where the stubs where unpacked
	 */
	File downloadAndUnpackStubJar(boolean workOffline, String stubRepositoryRoot, String stubsGroup, String
			stubsModule) {
		log.warn("Downloading stubs for group [$stubsGroup] and module [$stubsModule] from repository [$stubRepositoryRoot]")
		URI stubJarUri = findGrabbedStubJars(workOffline, stubRepositoryRoot, stubsGroup, stubsModule)
		if (!stubJarUri) {
			log.warn("Failed to download stubs for group [$stubsGroup] and module [$stubsModule] from repository [$stubRepositoryRoot]")
			return null
		}
		File unzippedStubsDir = unpackStubJarToATemporaryFolder(stubJarUri)
		unzippedStubsDir.deleteOnExit()
		Thread.addShutdownHook {
			unzippedStubsDir.deleteDir()
		}
		return unzippedStubsDir
	}

	private File unpackStubJarToATemporaryFolder(URI stubJarUri) {
		File tmpDirWhereStubsWillBeUnzipped = createTempDirectory(STUB_RUNNER_TEMP_DIR_PREFIX).toFile()
		tmpDirWhereStubsWillBeUnzipped.deleteOnExit()
		log.info("Unpacking stub from JAR [URI: ${stubJarUri}]")
		use(ZipCategory) {
			new File(stubJarUri).unzipTo(tmpDirWhereStubsWillBeUnzipped)
		}
		return tmpDirWhereStubsWillBeUnzipped
	}

	private URI findGrabbedStubJars(boolean workOffline, String stubRepositoryRoot, String stubsGroup, String stubsModule) {
		Map depToGrab = [group: stubsGroup, module: stubsModule, version: LATEST_MODULE, transitive: false]
		String accurestStubrunnerGrapePath = System.getProperty(STUB_RUNNER_GRAPE_CONFIG, getDefaultAccurestGrapeConfigPath())
		initializeAccurestGrapeIfAbsent(accurestStubrunnerGrapePath)
		String oldGrapeConfig = System.getProperty(GRAPE_CONFIG)
		try {
			System.setProperty(GRAPE_CONFIG, accurestStubrunnerGrapePath)
			log.info("Setting default grapes path to [$accurestStubrunnerGrapePath]")
			if (StringUtils.hasText(stubRepositoryRoot)) {
				return buildResolver(workOffline).resolveDependency(stubRepositoryRoot, depToGrab)
			}
			log.warn("No repository passed so will try to resolve dependencies from local Maven Repo")
			return buildResolver(true).resolveDependency(stubRepositoryRoot, depToGrab)
		} finally {
			restoreOldGrapeConfigIfApplicable(oldGrapeConfig)
		}
	}

	private DependencyResolver buildResolver(boolean workOffline) {
		return workOffline ?  new LocalOnlyDependencyResolver() : new RemoteDependencyResolver()
	}

	private void initializeAccurestGrapeIfAbsent(String accurestGrapePath) {
		File accurestGrape = new File(accurestGrapePath)
		if (!accurestGrape.exists()) {
			accurestGrape.parentFile.mkdirs()
			accurestGrape.createNewFile()
			accurestGrape.text = StubDownloader.class.getResource('/accurestStubrunnerGrapeConfig.xml').text
		}
	}

	private void restoreOldGrapeConfigIfApplicable(String oldGrapeConfig) {
		if (oldGrapeConfig) {
			System.setProperty(GRAPE_CONFIG, oldGrapeConfig)
		}
	}

	private String getDefaultAccurestGrapeConfigPath() {
		return "${System.getProperty('user.home')}/.accurest/accurestStubrunnerGrapeConfig.xml"
	}

	/**
	 * Dependency resolver providing {@link URI} to remote dependencies.
	 */
	@Slf4j
	private class RemoteDependencyResolver extends DependencyResolver {

		URI resolveDependency(String stubRepositoryRoot, Map depToGrab) {
			try {
				return doResolveRemoteDependency(stubRepositoryRoot, depToGrab)
			} catch (UnknownHostException e) {
				failureHandler(stubRepositoryRoot, "unknown host error -> ${e.message}", e)
			} catch (Exception e) {
				failureHandler(stubRepositoryRoot, "connection error -> ${e.message}", e)
			}
		}

		private URI doResolveRemoteDependency(String stubRepositoryRoot, Map depToGrab) {
			Grape.addResolver(name: REPOSITORY_NAME, root: stubRepositoryRoot)
			log.info("Resolving dependency ${depToGrab} location in remote repository...")
			return resolveDependencyLocation(depToGrab)
		}

		private void failureHandler(String stubRepository, String reason, Exception cause) {
			log.warn("Unable to resolve dependency in stub repository [$stubRepository]. Reason: [$reason]", cause)
		}

	}

	/**
	 * Dependency resolver that first checks if a dependency is available in the local repository.
	 * If not, it will try to provide {@link URI} from the remote repository.
	 *
	 * @see RemoteDependencyResolver
	 */
	@Slf4j
	private class LocalOnlyDependencyResolver extends DependencyResolver {

		URI resolveDependency(String stubRepositoryRoot, Map depToGrab) {
			try {
				log.info("Resolving dependency ${depToGrab} location in local repository...")
				return resolveDependencyLocation(depToGrab)
			} catch (Exception e) { //Grape throws ordinary RuntimeException
				log.warn("Unable to find dependency $depToGrab in local repository, trying $stubRepositoryRoot")
				log.debug("Unable to find dependency $depToGrab in local repository: ${e.getClass()}: ${e.message}")
				return null
			}
		}
	}

	/**
	 * Base class of dependency resolvers providing {@link URI} to required dependency.
	 */
	abstract class DependencyResolver {

		/**
		 * Returns {@link URI} to a dependency.
		 *
		 * @param stubRepositoryRoot root of the repository where the dependency should be found
		 * @param depToGrab parameters describing dependency to search for
		 *
		 * @return {@link URI} to dependency
		 */
		abstract URI resolveDependency(String stubRepositoryRoot, Map depToGrab)

		URI resolveDependencyLocation(Map depToGrab) {
			return Grape.resolve([classLoader: new GroovyClassLoader()], depToGrab).first()
		}

	}

}
