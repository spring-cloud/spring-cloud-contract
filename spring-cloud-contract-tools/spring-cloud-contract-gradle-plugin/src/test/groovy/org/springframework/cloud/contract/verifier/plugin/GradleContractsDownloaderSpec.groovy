package org.springframework.cloud.contract.verifier.plugin

import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.springframework.cloud.contract.stubrunner.AetherStubDownloader
import org.springframework.cloud.contract.stubrunner.ContractDownloader
import org.springframework.cloud.contract.stubrunner.StubConfiguration
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties
import spock.lang.Specification

/**
 * @author Marcin Grzejszczak
 */
class GradleContractsDownloaderSpec extends Specification {

	Project project = Stub(Project)
	Logger logger = Stub(Logger)

	def "should parse dependency via string notation"() {
		given:
			String stringNotation = "com.example:foo:1.0.0:stubs"
			def dep = new ContractVerifierExtension.Dependency(stringNotation: stringNotation)
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
			def dep = new ContractVerifierExtension.Dependency(
					groupId: "com.example",
					artifactId: "foo",
					version: "1.0.0",
					classifier: "stubs"
			)
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
			def dep = new ContractVerifierExtension.Dependency()
			dep.stringNotation(stringNotation)
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
			def dep = new ContractVerifierExtension.Dependency()
			dep.groupId("com.example")
			dep.artifactId("foo")
			dep.version("1.0.0")
			dep.classifier("stubs")
		when:
			StubConfiguration stubConfig = new GradleContractsDownloader(null, null).stubConfiguration(dep)
		then:
			stubConfig.groupId == "com.example"
			stubConfig.artifactId == "foo"
			stubConfig.version == "1.0.0"
			stubConfig.classifier == "stubs"
	}

	def "should pick dependency from cache for a non snapshot contract dependency with old property"() {
		given:
			ContractVerifierExtension ext = new ContractVerifierExtension()
			ext.with {
				contractsMode = StubRunnerProperties.StubsMode.REMOTE
				contractDependency {
					groupId("com.example")
					artifactId("foo")
					version("1.0.0")
					classifier("stubs")
				}
				contractsRepositoryUrl = "foo"
			}
		and:
			final AetherStubDownloader downloader = Mock(AetherStubDownloader)
			final ContractDownloader contractDownloader = Mock(ContractDownloader)
		and:
			def gradleDownloader = stubbedContractDownloader(downloader, contractDownloader)
		and:
			StubConfiguration expectedStubConfig = new StubConfiguration("com.example:foo:1.0.0:stubs")
			File expectedFileFromCache = new File("foo/bar")
			GradleContractsDownloader.downloadedContract.put(expectedStubConfig, expectedFileFromCache)
		when:
			File file = gradleDownloader.downloadAndUnpackContractsIfRequired(ext, new ContractVerifierConfigProperties())
		then:
			file == expectedFileFromCache
	}

	def "should pick dependency from cache for a non snapshot contract dependency with new property"() {
		given:
			ContractVerifierExtension ext = new ContractVerifierExtension()
			ext.with {
				contractsMode = StubRunnerProperties.StubsMode.REMOTE
				contractDependency {
					groupId("com.example")
					artifactId("foo")
					version("1.0.0")
					classifier("stubs")
				}
				contractRepository {
					repositoryUrl("foo")
				}
			}
		and:
			final AetherStubDownloader downloader = Mock(AetherStubDownloader)
			final ContractDownloader contractDownloader = Mock(ContractDownloader)
		and:
			def gradleDownloader = stubbedContractDownloader(downloader, contractDownloader)
		and:
			StubConfiguration expectedStubConfig = new StubConfiguration("com.example:foo:1.0.0:stubs")
			File expectedFileFromCache = new File("foo/bar")
			GradleContractsDownloader.downloadedContract.put(expectedStubConfig, expectedFileFromCache)
		when:
			File file = gradleDownloader.downloadAndUnpackContractsIfRequired(ext, new ContractVerifierConfigProperties())
		then:
			file == expectedFileFromCache
	}

	def "should not pick dependency from cache for a non snapshot contract dependency with cache switch off"() {
		given:
			ContractVerifierExtension ext = new ContractVerifierExtension()
			ext.with {
				contractsMode = StubRunnerProperties.StubsMode.REMOTE
				contractDependency {
					groupId("com.example")
					artifactId("foo")
					version("1.0.0")
					classifier("stubs")
				}
				contractRepository {
					repositoryUrl("foo")
					cacheDownloadedContracts(false)
				}
				disableStubPublication(true)
			}
		and:
			final AetherStubDownloader downloader = Mock(AetherStubDownloader)
			final ContractDownloader contractDownloader = Mock(ContractDownloader)
		and:
			def gradleDownloader = stubbedContractDownloader(downloader, contractDownloader)
		and:
			StubConfiguration expectedStubConfig = new StubConfiguration("com.example:foo:1.0.0:stubs")
			File expectedFileFromCache = new File("foo/bar")
			GradleContractsDownloader.downloadedContract.put(expectedStubConfig, expectedFileFromCache)
		and:
			File expectedFileNotFromCache = new File("foo/bar/baz")
			contractDownloader.unpackedDownloadedContracts(_) >> expectedFileNotFromCache
		when:
			File file = gradleDownloader.downloadAndUnpackContractsIfRequired(ext, new ContractVerifierConfigProperties())
		then:
			file == expectedFileNotFromCache
	}

	def "should not pick dependency from cache for snapshot contract dependency"() {
		given:
			ContractVerifierExtension ext = new ContractVerifierExtension()
			ext.with {
				contractsMode = StubRunnerProperties.StubsMode.REMOTE
				contractDependency {
					groupId("com.example")
					artifactId("foo")
					version("1.0.0.BUILD-SNAPSHOT")
					classifier("stubs")
				}
				contractRepository {
					repositoryUrl("foo")
				}
			}
		and:
			final AetherStubDownloader downloader = Mock(AetherStubDownloader)
			final ContractDownloader contractDownloader = Mock(ContractDownloader)
			File expectedFileNotFromCache = new File("foo/bar/baz")
			contractDownloader.unpackedDownloadedContracts(_) >> expectedFileNotFromCache
		and:
			def gradleDownloader = stubbedContractDownloader(downloader, contractDownloader)
		when:
			File file = gradleDownloader.downloadAndUnpackContractsIfRequired(ext, new ContractVerifierConfigProperties())
		then:
			file == expectedFileNotFromCache
	}

	private GradleContractsDownloader stubbedContractDownloader(downloader, contractDownloader) {
		new GradleContractsDownloader(project, logger) {
			@Override
			protected AetherStubDownloader stubDownloader(ContractVerifierExtension extension) {
				return downloader
			}

			@Override
			protected ContractDownloader contractDownloader(ContractVerifierExtension extension, StubConfiguration configuration) {
				return contractDownloader
			}
		}
	}

	def "should pick contract directory location from extension"() {
		given:
			ContractVerifierExtension ext = new ContractVerifierExtension()
			ext.with {
				contractsDslDir = new File("/foo/bar/baz")
			}
		and:
			final AetherStubDownloader downloader = Mock(AetherStubDownloader)
			final ContractDownloader contractDownloader = Mock(ContractDownloader)
		and:
			def gradleDownloader = stubbedContractDownloader(downloader, contractDownloader)
		and:
			StubConfiguration expectedStubConfig = new StubConfiguration("com.example:foo:1.0.0:stubs")
			GradleContractsDownloader.downloadedContract.put(expectedStubConfig, new File("foo/bar"))
		when:
			File file = gradleDownloader.downloadAndUnpackContractsIfRequired(ext, new ContractVerifierConfigProperties())
		then:
			file == new File("/foo/bar/baz")
	}

	def "should pass contract dependency properties as a parameter to the builder"() {
		given:
		ContractVerifierExtension ext = new ContractVerifierExtension()
		ext.with {
			contractDependency {
				groupId("com.example")
				artifactId("foo")
				version("1.0.0.BUILD-SNAPSHOT")
				classifier("stubs")
			}
			contractRepository {
				repositoryUrl("foo")
				username("foo1")
				password("foo2")
				proxyHost("foo3")
				proxyPort(12)
			}
		}
		and:
			final AetherStubDownloader downloader = Mock(AetherStubDownloader)
			final ContractDownloader contractDownloader = Mock(ContractDownloader)
			File expectedFileNotFromCache = new File("foo/bar/baz")
			contractDownloader.unpackedDownloadedContracts(_) >> expectedFileNotFromCache
		and:
			def gradleDownloader = assertingContractDownloader(downloader, contractDownloader)
		when:
			gradleDownloader.downloadAndUnpackContractsIfRequired(ext, new ContractVerifierConfigProperties())
		then:
			noExceptionThrown()
	}

	private GradleContractsDownloader assertingContractDownloader(downloader, contractDownloader) {
		new GradleContractsDownloader(project, logger) {
			@Override
			protected AetherStubDownloader stubDownloader(ContractVerifierExtension extension) {
				assert extension.contractRepository.username == "foo1"
				assert extension.contractRepository.password == "foo2"
				assert extension.contractRepository.proxyHost == "foo3"
				assert extension.contractRepository.proxyPort == 12
				return downloader
			}

			@Override
			protected ContractDownloader contractDownloader(ContractVerifierExtension extension, StubConfiguration configuration) {
				return contractDownloader
			}
		}
	}

}
