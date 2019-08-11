package org.springframework.cloud.contract.verifier.plugin

import org.gradle.api.Project
import org.gradle.api.internal.provider.DefaultPropertyState
import org.gradle.api.logging.Logger
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.springframework.cloud.contract.stubrunner.AetherStubDownloader
import org.springframework.cloud.contract.stubrunner.ContractDownloader
import org.springframework.cloud.contract.stubrunner.StubConfiguration
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties
import spock.lang.Specification

/**
 * @author Marcin Grzejszczak
 * @author Anatoliy Balakirev
 */
class GradleContractsDownloaderSpec extends Specification {

	Project project = Stub(Project)
	Logger logger = Stub(Logger)
	ObjectFactory objectFactory = Mock(ObjectFactory)

	def setup() {
		// Is there any better way to say that I need a new object on each interaction with mock?
		objectFactory.property(String) >>> [prop(String), prop(String), prop(String), prop(String), prop(String), prop(String), prop(String), prop(String), prop(String)]
		objectFactory.property(Integer) >> prop(Integer)
		objectFactory.property(Boolean) >> prop(Boolean)
	}

	def "should parse dependency via string notation"() {
		given:
			String stringNotation = "com.example:foo:1.0.0:stubs"
			ContractVerifierExtension.Dependency dep = new ContractVerifierExtension.Dependency(objectFactory)
			dep.stringNotation.set(stringNotation)
		when:
			StubConfiguration stubConfig = new GradleContractsDownloader(null, null).stubConfiguration(dep)
		then:
			stubConfig.groupId == "com.example"
			stubConfig.artifactId == "foo"
			stubConfig.version == "1.0.0"
			stubConfig.classifier == "stubs"
	}

	def "should parse dependency via direct setting"() {
		given:
			ContractVerifierExtension.Dependency dep = new ContractVerifierExtension.Dependency(objectFactory)
			dep.groupId.set("com.example")
			dep.artifactId.set("foo")
			dep.version.set("1.0.0")
			dep.classifier.set("stubs")
		when:
			StubConfiguration stubConfig = new GradleContractsDownloader(null, null).stubConfiguration(dep)
		then:
			stubConfig.groupId == "com.example"
			stubConfig.artifactId == "foo"
			stubConfig.version == "1.0.0"
			stubConfig.classifier == "stubs"
	}

	def "should parse dependency via string notation with methods"() {
		given:
			String stringNotation = "com.example:foo:1.0.0:stubs"
			ContractVerifierExtension.Dependency dep = new ContractVerifierExtension.Dependency(objectFactory)
			dep.stringNotation.set(stringNotation)
		when:
			StubConfiguration stubConfig = new GradleContractsDownloader(null, null).stubConfiguration(dep)
		then:
			stubConfig.groupId == "com.example"
			stubConfig.artifactId == "foo"
			stubConfig.version == "1.0.0"
			stubConfig.classifier == "stubs"
	}

	def "should parse dependency via direct setting with methods"() {
		given:
			ContractVerifierExtension.Dependency dep = new ContractVerifierExtension.Dependency(objectFactory)
			dep.groupId.set("com.example")
			dep.artifactId.set("foo")
			dep.version.set("1.0.0")
			dep.classifier.set("stubs")
		when:
			StubConfiguration stubConfig = new GradleContractsDownloader(null, null).stubConfiguration(dep)
		then:
			stubConfig.groupId == "com.example"
			stubConfig.artifactId == "foo"
			stubConfig.version == "1.0.0"
			stubConfig.classifier == "stubs"
	}

	def "should pick dependency from cache for a non snapshot contract dependency with new property"() {
		given:
			ContractVerifierExtension.Dependency contractDependency = new ContractVerifierExtension.Dependency(objectFactory)
			contractDependency.groupId.set("com.example")
			contractDependency.artifactId.set("foo")
			contractDependency.version.set("1.0.0")
			contractDependency.classifier.set("stubs")
			ContractVerifierExtension.ContractRepository contractRepository = new ContractVerifierExtension.ContractRepository(objectFactory)
			contractRepository.repositoryUrl.set("foo")
		and:
			final AetherStubDownloader downloader = Mock(AetherStubDownloader)
			final ContractDownloader contractDownloader = Mock(ContractDownloader)
		and:
			GradleContractsDownloader gradleDownloader = stubbedContractDownloader(downloader, contractDownloader)
		and:
			StubConfiguration expectedStubConfig = new StubConfiguration("com.example:foo:1.0.0:stubs")
			File expectedFileFromCache = new File("foo/bar")
			GradleContractsDownloader.downloadedContract.put(expectedStubConfig, expectedFileFromCache)
		when:
			GradleContractsDownloader.DownloadedData downloaded = gradleDownloader.downloadAndUnpackContractsIfRequired(contractDependency, contractRepository, null, StubRunnerProperties.StubsMode.REMOTE, true, [:], true)
		then:
			downloaded.downloadedContracts == expectedFileFromCache
	}

	def "should not pick dependency from cache for a non snapshot contract dependency with cache switch off"() {
		given:
			ContractVerifierExtension.Dependency contractDependency = new ContractVerifierExtension.Dependency(objectFactory)
			contractDependency.groupId.set("com.example")
			contractDependency.artifactId.set("foo")
			contractDependency.version.set("1.0.0")
			contractDependency.classifier.set("stubs")
			ContractVerifierExtension.ContractRepository contractRepository = new ContractVerifierExtension.ContractRepository(objectFactory)
			contractRepository.repositoryUrl.set("foo")
			contractRepository.cacheDownloadedContracts.set(false)
		and:
			final AetherStubDownloader downloader = Mock(AetherStubDownloader)
			final ContractDownloader contractDownloader = Mock(ContractDownloader)
		and:
			GradleContractsDownloader gradleDownloader = stubbedContractDownloader(downloader, contractDownloader)
		and:
			StubConfiguration expectedStubConfig = new StubConfiguration("com.example:foo:1.0.0:stubs")
			File expectedFileFromCache = new File("foo/bar")
			GradleContractsDownloader.downloadedContract.put(expectedStubConfig, expectedFileFromCache)
		and:
			File expectedFileNotFromCache = new File("foo/bar/baz")
			contractDownloader.unpackAndDownloadContracts() >> expectedFileNotFromCache
		when:
			GradleContractsDownloader.DownloadedData downloaded = gradleDownloader.downloadAndUnpackContractsIfRequired(contractDependency, contractRepository, null, StubRunnerProperties.StubsMode.REMOTE, true, [:], true)
		then:
			downloaded.downloadedContracts == expectedFileNotFromCache
	}

	def "should not pick dependency from cache for snapshot contract dependency"() {
		given:
			ContractVerifierExtension.Dependency contractDependency = new ContractVerifierExtension.Dependency(objectFactory)
			contractDependency.groupId.set("com.example")
			contractDependency.artifactId.set("foo")
			contractDependency.version.set("1.0.0.BUILD-SNAPSHOT")
			contractDependency.classifier.set("stubs")
			ContractVerifierExtension.ContractRepository contractRepository = new ContractVerifierExtension.ContractRepository(objectFactory)
			contractRepository.repositoryUrl.set("foo")
		and:
			final AetherStubDownloader downloader = Mock(AetherStubDownloader)
			final ContractDownloader contractDownloader = Mock(ContractDownloader)
			File expectedFileNotFromCache = new File("foo/bar/baz")
			contractDownloader.unpackAndDownloadContracts() >> expectedFileNotFromCache
		and:
			GradleContractsDownloader gradleDownloader = stubbedContractDownloader(downloader, contractDownloader)
		when:
			GradleContractsDownloader.DownloadedData downloaded = gradleDownloader.downloadAndUnpackContractsIfRequired(contractDependency, contractRepository, null, StubRunnerProperties.StubsMode.REMOTE, true, [:], true)
		then:
			downloaded.downloadedContracts == expectedFileNotFromCache
	}

	private GradleContractsDownloader stubbedContractDownloader(downloader, contractDownloader) {
		new GradleContractsDownloader(project, logger) {
			@Override
			protected AetherStubDownloader stubDownloader(ContractVerifierExtension.ContractRepository contractRepository,
														  StubRunnerProperties.StubsMode contractsMode, boolean deleteStubsAfterTest,
														  Map<String, String> contractsProperties, boolean failOnNoContracts) {
				return downloader
			}

			@Override
			protected ContractDownloader contractDownloader(StubConfiguration configuration,
															ContractVerifierExtension.ContractRepository contractRepository,
															String contractsPath, StubRunnerProperties.StubsMode contractsMode,
															boolean deleteStubsAfterTest, Map<String, String> contractsProperties,
															boolean failOnNoContracts) {
				return contractDownloader
			}
		}
	}

	def "should not start downloading"() {
		given:
			ContractVerifierExtension.Dependency contractDependency = new ContractVerifierExtension.Dependency(objectFactory)
			ContractVerifierExtension.ContractRepository contractRepository = new ContractVerifierExtension.ContractRepository(objectFactory)
		and:
			final AetherStubDownloader downloader = Mock(AetherStubDownloader)
			final ContractDownloader contractDownloader = Mock(ContractDownloader)
		and:
			GradleContractsDownloader gradleDownloader = stubbedContractDownloader(downloader, contractDownloader)
		and:
			StubConfiguration expectedStubConfig = new StubConfiguration("com.example:foo:1.0.0:stubs")
			GradleContractsDownloader.downloadedContract.put(expectedStubConfig, new File("foo/bar"))
		when:
			GradleContractsDownloader.DownloadedData downloaded = gradleDownloader.downloadAndUnpackContractsIfRequired(contractDependency, contractRepository, null, StubRunnerProperties.StubsMode.CLASSPATH, true, [:], true)
		then:
			downloaded == null
	}

	def "should pass contract dependency properties as a parameter to the builder"() {
		given:
			ContractVerifierExtension.Dependency contractDependency = new ContractVerifierExtension.Dependency(objectFactory)
			contractDependency.groupId.set("com.example")
			contractDependency.artifactId.set("foo")
			contractDependency.version.set("1.0.0.BUILD-SNAPSHOT")
			contractDependency.classifier.set("stubs")
			ContractVerifierExtension.ContractRepository contractRepository = new ContractVerifierExtension.ContractRepository(objectFactory)
			contractRepository.repositoryUrl.set("foo")
			contractRepository.username.set("foo1")
			contractRepository.password.set("foo2")
			contractRepository.proxyHost.set("foo3")
			contractRepository.proxyPort.set(12)
		and:
			final AetherStubDownloader downloader = Mock(AetherStubDownloader)
			final ContractDownloader contractDownloader = Mock(ContractDownloader)
			File expectedFileNotFromCache = new File("foo/bar/baz")
			contractDownloader.unpackAndDownloadContracts() >> expectedFileNotFromCache
		and:
			GradleContractsDownloader gradleDownloader = assertingContractDownloader(downloader, contractDownloader)
		when:
			gradleDownloader.downloadAndUnpackContractsIfRequired(contractDependency, contractRepository, null, StubRunnerProperties.StubsMode.CLASSPATH, true, [:], true)
		then:
			noExceptionThrown()
	}

	private GradleContractsDownloader assertingContractDownloader(downloader, contractDownloader) {
		new GradleContractsDownloader(project, logger) {
			@Override
			protected AetherStubDownloader stubDownloader(ContractVerifierExtension.ContractRepository contractRepository,
														  StubRunnerProperties.StubsMode contractsMode, boolean deleteStubsAfterTest,
														  Map<String, String> contractsProperties, boolean failOnNoContracts) {
				assert extension.contractRepository.username == "foo1"
				assert extension.contractRepository.password == "foo2"
				assert extension.contractRepository.proxyHost == "foo3"
				assert extension.contractRepository.proxyPort == 12
				return downloader
			}

			@Override
			protected ContractDownloader contractDownloader(StubConfiguration configuration,
															ContractVerifierExtension.ContractRepository contractRepository,
															String contractsPath, StubRunnerProperties.StubsMode contractsMode,
															boolean deleteStubsAfterTest, Map<String, String> contractsProperties,
															boolean failOnNoContracts) {
				return contractDownloader
			}
		}
	}

	// Have to use this internal property impl here. Is there some better way?
	static <T> Property<T> prop(Class<T> aClass) {
		return new DefaultPropertyState(aClass)
	}
}
