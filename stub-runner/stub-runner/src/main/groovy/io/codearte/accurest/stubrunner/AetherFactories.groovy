package io.codearte.accurest.stubrunner

import org.apache.maven.repository.internal.MavenRepositorySystemUtils
import org.eclipse.aether.DefaultRepositorySystemSession
import org.eclipse.aether.RepositorySystem
import org.eclipse.aether.RepositorySystemSession
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory
import org.eclipse.aether.impl.DefaultServiceLocator
import org.eclipse.aether.repository.LocalRepository
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.repository.RepositoryPolicy
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory
import org.eclipse.aether.spi.connector.transport.TransporterFactory
import org.eclipse.aether.transport.file.FileTransporterFactory
import org.eclipse.aether.transport.http.HttpTransporterFactory

class AetherFactories {

	private static final String MAVEN_LOCAL_REPOSITORY_LOCATION = 'maven.repo.local'

	static RepositorySystem newRepositorySystem() {
		DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator()
		locator.addService(RepositoryConnectorFactory, BasicRepositoryConnectorFactory)
		locator.addService(TransporterFactory, FileTransporterFactory)
		locator.addService(TransporterFactory, HttpTransporterFactory)
		return locator.getService(RepositorySystem)
	}

	static RepositorySystemSession newSession(RepositorySystem system, boolean workOffline) {
		DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession()
		session.setOffline(workOffline)
		if (!workOffline) {
			session.setUpdatePolicy(RepositoryPolicy.UPDATE_POLICY_ALWAYS)
		}
		LocalRepository localRepo = new LocalRepository(localRepositoryDirectory())
		session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo))
		return session
	}

	static List<RemoteRepository> newRepositories(List<String> repositories) {
		return repositories.withIndex()
				.findAll { String repo, int index -> repo }
				.collect { String repo, int index ->
			new RemoteRepository.Builder('remote' + index, 'default', repo).build()
		}
	}

	static String localRepositoryDirectory() {
		System.getProperty(MAVEN_LOCAL_REPOSITORY_LOCATION, "${System.getProperty("user.home")}/.m2/repository")
	}

}
