/*
 *  Copyright 2013-2016 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.springframework.cloud.contract.stubrunner

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
