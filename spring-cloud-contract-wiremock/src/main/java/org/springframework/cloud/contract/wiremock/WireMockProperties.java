/*
 * Copyright 2013-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.contract.wiremock;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("wiremock")
public class WireMockProperties {

	private Server server = new Server();

	private Placeholders placeholders = new Placeholders();

	private boolean restTemplateSslEnabled;

	private boolean resetMappingsAfterEachTest;

	public boolean isRestTemplateSslEnabled() {
		return this.restTemplateSslEnabled;
	}

	public void setRestTemplateSslEnabled(boolean restTemplateSslEnabled) {
		this.restTemplateSslEnabled = restTemplateSslEnabled;
	}

	public boolean isResetMappingsAfterEachTest() {
		return this.resetMappingsAfterEachTest;
	}

	public void setResetMappingsAfterEachTest(boolean resetMappingsAfterEachTest) {
		this.resetMappingsAfterEachTest = resetMappingsAfterEachTest;
	}

	public Server getServer() {
		return this.server;
	}

	public void setServer(Server server) {
		this.server = server;
	}

	public Placeholders getPlaceholders() {
		return this.placeholders;
	}

	public void setPlaceholders(Placeholders placeholders) {
		this.placeholders = placeholders;
	}

	public class Placeholders {

		/**
		 * Flag to indicate that http URLs in generated wiremock stubs should be filtered
		 * to add or resolve a placeholder for a dynamic port.
		 */
		private boolean enabled = true;

		public boolean isEnabled() {
			return this.enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

	}

	public static class Server {

		private int port = 8080;

		private boolean disableHttp = false;

		private int httpsPort = -1;

		private String bindAddress = "0.0.0.0";

		private String httpsKeystore;

		private String keystoreType;

		private String keystorePassword;

		private String keyManagerPassword;

		private String httpsTruststore;

		private String truststoreType;

		private String truststorePassword;

		private String httpsRequireClientCert;

		private boolean verbose = false;

		private String rootDir;

		private String caKeystore;

		private String caKeystorePassword;

		private String caKeystoreType;

		private boolean noRequestJournal = false;

		private int containerThreads = 10;

		private int maxRequestJournalEntries;

		private int jettyAcceptorThreads;

		private int jettyAcceptQueueSize;

		private int jettyHeaderBufferSize;

		private boolean asyncResponseEnabled = false;

		private int asyncResponseThreads = 10;

		private boolean printAllNetworkTraffic = false;

		private boolean globalResponseTemplating = false;

		private boolean localResponseTemplating = false;

		private int maxTemplateCacheEntries;

		private boolean useChunkedEncoding = false;

		private boolean disableGzip = false;

		private boolean disableRequestLogging = false;

		private boolean disableBanner = false;

		private boolean enableStubCors = false;

		private String[] stubs = new String[0];

		private String[] files = new String[0];

		private boolean portDynamic = false;

		private boolean httpsPortDynamic = false;

		public int getPort() {
			return this.port;
		}

		public void setPort(int port) {
			this.port = port;
		}

		public boolean isDisableHttp() {
			return disableHttp;
		}

		public void setDisableHttp(boolean disableHttp) {
			this.disableHttp = disableHttp;
		}

		public int getHttpsPort() {
			return this.httpsPort;
		}

		public void setHttpsPort(int httpsPort) {
			this.httpsPort = httpsPort;
		}

		public String getBindAddress() {
			return bindAddress;
		}

		public void setBindAddress(String bindAddress) {
			this.bindAddress = bindAddress;
		}

		public String getHttpsKeystore() {
			return httpsKeystore;
		}

		public void setHttpsKeystore(String httpsKeystore) {
			this.httpsKeystore = httpsKeystore;
		}

		public String getKeystoreType() {
			return keystoreType;
		}

		public void setKeystoreType(String keystoreType) {
			this.keystoreType = keystoreType;
		}

		public String getKeystorePassword() {
			return keystorePassword;
		}

		public void setKeystorePassword(String keystorePassword) {
			this.keystorePassword = keystorePassword;
		}

		public String getKeyManagerPassword() {
			return keyManagerPassword;
		}

		public void setKeyManagerPassword(String keyManagerPassword) {
			this.keyManagerPassword = keyManagerPassword;
		}

		public String getHttpsTruststore() {
			return httpsTruststore;
		}

		public void setHttpsTruststore(String httpsTruststore) {
			this.httpsTruststore = httpsTruststore;
		}

		public String getTruststoreType() {
			return truststoreType;
		}

		public void setTruststoreType(String truststoreType) {
			this.truststoreType = truststoreType;
		}

		public String getTruststorePassword() {
			return truststorePassword;
		}

		public void setTruststorePassword(String truststorePassword) {
			this.truststorePassword = truststorePassword;
		}

		public String getHttpsRequireClientCert() {
			return httpsRequireClientCert;
		}

		public void setHttpsRequireClientCert(String httpsRequireClientCert) {
			this.httpsRequireClientCert = httpsRequireClientCert;
		}

		public boolean isVerbose() {
			return verbose;
		}

		public void setVerbose(boolean verbose) {
			this.verbose = verbose;
		}

		public String getRootDir() {
			return rootDir;
		}

		public void setRootDir(String rootDir) {
			this.rootDir = rootDir;
		}

		public String getCaKeystore() {
			return caKeystore;
		}

		public void setCaKeystore(String caKeystore) {
			this.caKeystore = caKeystore;
		}

		public String getCaKeystorePassword() {
			return caKeystorePassword;
		}

		public void setCaKeystorePassword(String caKeystorePassword) {
			this.caKeystorePassword = caKeystorePassword;
		}

		public String getCaKeystoreType() {
			return caKeystoreType;
		}

		public void setCaKeystoreType(String caKeystoreType) {
			this.caKeystoreType = caKeystoreType;
		}

		public boolean isNoRequestJournal() {
			return noRequestJournal;
		}

		public void setNoRequestJournal(boolean noRequestJournal) {
			this.noRequestJournal = noRequestJournal;
		}

		public int getContainerThreads() {
			return containerThreads;
		}

		public void setContainerThreads(int containerThreads) {
			this.containerThreads = containerThreads;
		}

		public int getMaxRequestJournalEntries() {
			return maxRequestJournalEntries;
		}

		public void setMaxRequestJournalEntries(int maxRequestJournalEntries) {
			this.maxRequestJournalEntries = maxRequestJournalEntries;
		}

		public int getJettyAcceptorThreads() {
			return jettyAcceptorThreads;
		}

		public void setJettyAcceptorThreads(int jettyAcceptorThreads) {
			this.jettyAcceptorThreads = jettyAcceptorThreads;
		}

		public int getJettyAcceptQueueSize() {
			return jettyAcceptQueueSize;
		}

		public void setJettyAcceptQueueSize(int jettyAcceptQueueSize) {
			this.jettyAcceptQueueSize = jettyAcceptQueueSize;
		}

		public int getJettyHeaderBufferSize() {
			return jettyHeaderBufferSize;
		}

		public void setJettyHeaderBufferSize(int jettyHeaderBufferSize) {
			this.jettyHeaderBufferSize = jettyHeaderBufferSize;
		}

		public boolean isAsyncResponseEnabled() {
			return asyncResponseEnabled;
		}

		public void setAsyncResponseEnabled(boolean asyncResponseEnabled) {
			this.asyncResponseEnabled = asyncResponseEnabled;
		}

		public int getAsyncResponseThreads() {
			return asyncResponseThreads;
		}

		public void setAsyncResponseThreads(int asyncResponseThreads) {
			this.asyncResponseThreads = asyncResponseThreads;
		}

		public boolean isPrintAllNetworkTraffic() {
			return printAllNetworkTraffic;
		}

		public void setPrintAllNetworkTraffic(boolean printAllNetworkTraffic) {
			this.printAllNetworkTraffic = printAllNetworkTraffic;
		}

		public boolean isGlobalResponseTemplating() {
			return globalResponseTemplating;
		}

		public void setGlobalResponseTemplating(boolean globalResponseTemplating) {
			this.globalResponseTemplating = globalResponseTemplating;
		}

		public boolean isLocalResponseTemplating() {
			return localResponseTemplating;
		}

		public void setLocalResponseTemplating(boolean localResponseTemplating) {
			this.localResponseTemplating = localResponseTemplating;
		}

		public int getMaxTemplateCacheEntries() {
			return maxTemplateCacheEntries;
		}

		public void setMaxTemplateCacheEntries(int maxTemplateCacheEntries) {
			this.maxTemplateCacheEntries = maxTemplateCacheEntries;
		}

		public boolean isUseChunkedEncoding() {
			return useChunkedEncoding;
		}

		public void setUseChunkedEncoding(boolean useChunkedEncoding) {
			this.useChunkedEncoding = useChunkedEncoding;
		}

		public boolean isDisableGzip() {
			return disableGzip;
		}

		public void setDisableGzip(boolean disableGzip) {
			this.disableGzip = disableGzip;
		}

		public boolean isDisableRequestLogging() {
			return disableRequestLogging;
		}

		public void setDisableRequestLogging(boolean disableRequestLogging) {
			this.disableRequestLogging = disableRequestLogging;
		}

		public boolean isDisableBanner() {
			return disableBanner;
		}

		public void setDisableBanner(boolean disableBanner) {
			this.disableBanner = disableBanner;
		}

		public boolean isEnableStubCors() {
			return enableStubCors;
		}

		public void setEnableStubCors(boolean enableStubCors) {
			this.enableStubCors = enableStubCors;
		}

		public String[] getStubs() {
			return this.stubs;
		}

		public void setStubs(String[] stubs) {
			this.stubs = stubs;
		}

		public String[] getFiles() {
			return this.files;
		}

		public void setFiles(String[] files) {
			this.files = files;
		}

		public boolean isPortDynamic() {
			return this.portDynamic;
		}

		public void setPortDynamic(boolean portDynamic) {
			this.portDynamic = portDynamic;
		}

		public boolean isHttpsPortDynamic() {
			return this.httpsPortDynamic;
		}

		public void setHttpsPortDynamic(boolean httpsPortDynamic) {
			this.httpsPortDynamic = httpsPortDynamic;
		}

	}

}
