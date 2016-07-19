/*
 * Copyright 2012-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.contract.stubrunner.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

/**
 * @author Dave Syer
 *
 */
@ConfigurationProperties("stubrunner")
public class StubRunnerProperties {

	/**
	 * 
	 */
	private int minPort = 10000;

	/**
	 * 
	 */
	private int maxPort = 15000;

	/**
	 * 
	 */
	private boolean workOffline;

	private Stubs stubs = new Stubs();

	public int getMinPort() {
		return minPort;
	}

	public void setMinPort(int minPort) {
		this.minPort = minPort;
	}

	public int getMaxPort() {
		return maxPort;
	}

	public void setMaxPort(int maxPort) {
		this.maxPort = maxPort;
	}

	public boolean isWorkOffline() {
		return workOffline;
	}

	public void setWorkOffline(boolean workOffline) {
		this.workOffline = workOffline;
	}

	public Stubs getStubs() {
		return stubs;
	}

	public void setStubs(Stubs stubs) {
		this.stubs = stubs;
	}

	public static class Stubs {
		/**
		 * The repository root to use (defaults to local Maven repo).
		 */
		private Resource repositoryRoot;
		/**
		 * The ids of the stubs to run in "ivy" notation (groupId:artifactId[:classifier]:version[:port]).
		 */
		private String[] ids = new String[0];
		/**
		 * The classifier to use by default in ivy co-ordinates for a stub.
		 */
		private String classifier = "stubs";

		public Resource getRepositoryRoot() {
			return repositoryRoot;
		}

		public void setRepositoryRoot(Resource repositoryRoot) {
			this.repositoryRoot = repositoryRoot;
		}

		public String[] getIds() {
			return ids;
		}

		public void setIds(String[] ids) {
			this.ids = ids;
		}

		public String getClassifier() {
			return classifier;
		}

		public void setClassifier(String classifier) {
			this.classifier = classifier;
		}
	}

}
