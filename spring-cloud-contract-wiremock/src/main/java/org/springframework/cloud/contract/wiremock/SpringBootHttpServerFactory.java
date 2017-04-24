/*
 * Copyright 2015 the original author or authors.
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

package org.springframework.cloud.contract.wiremock;

import javax.servlet.ServletContext;

import io.undertow.Undertow.Builder;

import com.github.tomakehurst.wiremock.common.HttpsSettings;
import com.github.tomakehurst.wiremock.common.Notifier;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.core.WireMockApp;
import com.github.tomakehurst.wiremock.http.AdminRequestHandler;
import com.github.tomakehurst.wiremock.http.HttpServer;
import com.github.tomakehurst.wiremock.http.HttpServerFactory;
import com.github.tomakehurst.wiremock.http.RequestHandler;
import com.github.tomakehurst.wiremock.http.StubRequestHandler;
import com.github.tomakehurst.wiremock.servlet.FaultInjectorFactory;
import com.github.tomakehurst.wiremock.servlet.NoFaultInjectorFactory;
import com.github.tomakehurst.wiremock.servlet.WireMockHandlerDispatchingServlet;

import org.apache.catalina.connector.Connector;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.autoconfigure.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.web.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.EmbeddedServletContainerAutoConfiguration.BeanPostProcessorsRegistrar;
import org.springframework.boot.autoconfigure.web.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.boot.context.embedded.EmbeddedWebApplicationContext;
import org.springframework.boot.context.embedded.Ssl;
import org.springframework.boot.context.embedded.jetty.JettyEmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.jetty.JettyServerCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.undertow.UndertowBuilderCustomizer;
import org.springframework.boot.context.embedded.undertow.UndertowEmbeddedServletContainerFactory;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.cloud.contract.wiremock.ContainerConfiguration.JettyContainerConfiguration;
import org.springframework.cloud.contract.wiremock.ContainerConfiguration.TomcatContainerConfiguration;
import org.springframework.cloud.contract.wiremock.ContainerConfiguration.UndertowContainerConfiguration;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ServletContextAware;

/**
 * @author Dave Syer
 *
 */
class SpringBootHttpServerFactory implements HttpServerFactory {

	@Override
	public HttpServer buildHttpServer(Options options,
			AdminRequestHandler adminRequestHandler,
			StubRequestHandler stubRequestHandler) {
		return new SpringBootHttpServer(options, adminRequestHandler, stubRequestHandler);
	}

}

class SpringBootHttpServer
		implements HttpServer, ApplicationListener<ApplicationPreparedEvent> {

	private volatile boolean running;
	private Options options;
	private AdminRequestHandler adminRequestHandler;
	private StubRequestHandler stubRequestHandler;
	private ConfigurableApplicationContext context;

	public SpringBootHttpServer(Options options, AdminRequestHandler adminRequestHandler,
			StubRequestHandler stubRequestHandler) {
		this.options = options;
		this.adminRequestHandler = adminRequestHandler;
		this.stubRequestHandler = stubRequestHandler;
	}

	@Override
	public void start() {
		this.context = new SpringApplicationBuilder(WiremockServerConfiguration.class)
				.logStartupInfo(false).bannerMode(Mode.OFF)
				.properties("spring.cloud.bootstrap.enabled=false").listeners(this).run();
		this.running = true;
	}

	@Override
	public void stop() {
		if (this.context != null) {
			this.context.close();
		}
		this.running = false;
	}

	@Override
	public boolean isRunning() {
		return this.running;
	}

	@Override
	public int port() {
		return container().port();
	}

	@Override
	public int httpsPort() {
		return container().httpsPort();
	}

	private ContainerProperties container() {
		if (this.context != null) {
			return this.context.getBean(ContainerProperties.class);
		}
		return new ContainerProperties(this.options);
	}

	@Override
	public void onApplicationEvent(ApplicationPreparedEvent event) {
		GenericApplicationContext context = (GenericApplicationContext) event
				.getApplicationContext();
		DefaultListableBeanFactory beanFactory = context.getDefaultListableBeanFactory();
		if (beanFactory.containsBean("wireMockOptions")) {
			// Be idempotent (the event might be emitted more than once)
			return;
		}
		beanFactory.registerSingleton("wireMockOptions", this.options);
		beanFactory.registerSingleton("adminRequestHandler", this.adminRequestHandler);
		beanFactory.registerSingleton("stubRequestHandler", this.stubRequestHandler);
		beanFactory.addBeanPostProcessor(new ServerPropertiesPostProcessor());
	}

	class ServerPropertiesPostProcessor implements BeanPostProcessor {

		@Override
		public Object postProcessBeforeInitialization(Object bean, String beanName)
				throws BeansException {
			return bean;
		}

		@Override
		public Object postProcessAfterInitialization(Object bean, String beanName)
				throws BeansException {
			if (bean instanceof WiremockServerProperties) {
				WiremockServerProperties server = (WiremockServerProperties) bean;
				server.setPort(getPort());
				setupHttps(server, SpringBootHttpServer.this.options.httpsSettings());
				// TODO: other options
			}
			return bean;
		}

		private void setupHttps(WiremockServerProperties server,
				HttpsSettings httpsSettings) {
			if (httpsSettings.port() < 0 || !httpsSettings.enabled()) {
				return;
			}
			Ssl ssl = server.getSsl();
			if (ssl == null) {
				ssl = new Ssl();
				server.setSsl(ssl);
			}
			ssl.setKeyStore(httpsSettings.keyStorePath());
			ssl.setKeyPassword(httpsSettings.keyStorePassword());
			if (httpsSettings.hasTrustStore()) {
				ssl.setTrustStore(httpsSettings.trustStorePath());
				ssl.setTrustStorePassword(httpsSettings.trustStorePassword());
			}
		}

		private int getPort() {
			if (SpringBootHttpServer.this.options.httpsSettings().port() >= 0) {
				return SpringBootHttpServer.this.options.httpsSettings().port();
			}
			return SpringBootHttpServer.this.options.portNumber();
		}

	}

}

class WiremockServerProperties implements EmbeddedServletContainerCustomizer {

	private ServerProperties delegate = new ServerProperties();

	public Integer getPort() {
		return this.delegate.getPort();
	}

	public void setPort(Integer port) {
		this.delegate.setPort(port);
	}

	public Ssl getSsl() {
		return this.delegate.getSsl();
	}

	public void setSsl(Ssl ssl) {
		this.delegate.setSsl(ssl);
	}

	@Override
	public void customize(ConfigurableEmbeddedServletContainer container) {
		this.delegate.customize(container);
	}
}

@Configuration
class ServerPropertiesConfiguration {
	@Bean
	public WiremockServerProperties serverProperties() {
		// Needs to be something that doesn't bind to "server.*"
		return new WiremockServerProperties();
	}
}

@Configuration
@Import({ TomcatContainerConfiguration.class, JettyContainerConfiguration.class,
		UndertowContainerConfiguration.class, ServerPropertiesConfiguration.class,
		BeanPostProcessorsRegistrar.class, ConfigurationPropertiesAutoConfiguration.class,
		JacksonAutoConfiguration.class, HttpMessageConvertersAutoConfiguration.class,
		PropertyPlaceholderAutoConfiguration.class, ContainerProperties.class })
class WiremockServerConfiguration {

	@Autowired
	private AdminRequestHandler adminRequestHandler;
	@Autowired
	private StubRequestHandler stubRequestHandler;
	@Autowired
	private FaultInjectorFactory faultInjectorFactory;
	@Autowired
	private Options options;

	@Bean(name = DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_REGISTRATION_BEAN_NAME)
	public ServletRegistrationBean stubServletRegistration() {
		ServletRegistrationBean reg = new ServletRegistrationBean();
		reg.addInitParameter(RequestHandler.HANDLER_CLASS_KEY,
				StubRequestHandler.class.getName());
		if (WiremockServerConfiguration.this.faultInjectorFactory != null) {
			reg.addInitParameter(FaultInjectorFactory.INJECTOR_CLASS_KEY,
					FaultInjectorFactory.class.getName());
		}
		reg.setServlet(new WireMockHandlerDispatchingServlet());
		reg.setName("stub");
		reg.addUrlMappings("/");
		return reg;
	}

	@Bean
	public ServletRegistrationBean adminServletRegistration() {
		ServletRegistrationBean reg = new ServletRegistrationBean();
		reg.addInitParameter(RequestHandler.HANDLER_CLASS_KEY,
				AdminRequestHandler.class.getName());
		reg.setServlet(new WireMockHandlerDispatchingServlet());
		reg.setName("admin");
		reg.addUrlMappings(WireMockApp.ADMIN_CONTEXT_ROOT + "/*");
		return reg;
	}

	@Bean
	public ServletContextAware servletContextSetUp() {
		return new ServletContextAware() {
			@Override
			public void setServletContext(ServletContext servletContext) {
				servletContext.setAttribute(AdminRequestHandler.class.getName(),
						WiremockServerConfiguration.this.adminRequestHandler);
				servletContext.setAttribute(StubRequestHandler.class.getName(),
						WiremockServerConfiguration.this.stubRequestHandler);
				servletContext.setAttribute(Notifier.KEY,
						WiremockServerConfiguration.this.options.notifier());
				if (WiremockServerConfiguration.this.faultInjectorFactory != null) {
					servletContext.setAttribute(FaultInjectorFactory.class.getName(),
							WiremockServerConfiguration.this.faultInjectorFactory);
				}
			}
		};
	}

}

@Component
class ContainerProperties {

	private Options options;

	private Integer localPort;

	private Integer localHttpsPort;

	@Autowired
	private ConfigurableApplicationContext context;

	@Autowired
	public ContainerProperties(Options options) {
		this.options = options;
	}

	public int port() {
		if (this.options.httpsSettings().enabled()) {
			return this.options.portNumber();
		}
		if (this.localPort != null) {
			return this.localPort;
		}
		EmbeddedWebApplicationContext embedded = (EmbeddedWebApplicationContext) this.context;
		return embedded.getEmbeddedServletContainer().getPort();
	}

	public int httpsPort() {
		if (this.localHttpsPort != null) {
			return this.localHttpsPort;
		}
		return this.options.httpsSettings().port();
	}

	public void setLocalPort(int localPort) {
		this.localPort = localPort;
	}

	public void setLocalHttpsPort(int localHttpsPort) {
		this.localHttpsPort = localHttpsPort;
	}

}

class ContainerConfiguration {

	@Configuration
	@ConditionalOnMissingBean(EmbeddedServletContainerFactory.class)
	@ConditionalOnClass({ TomcatEmbeddedServletContainerFactory.class, Connector.class })
	static class TomcatContainerConfiguration {
		@Autowired
		private Options options;

		@Autowired
		private ContainerProperties container;

		private Connector connector;

		@Bean
		public EmbeddedServletContainerFactory servletContainer() {
			TomcatEmbeddedServletContainerFactory tomcat = new TomcatEmbeddedServletContainerFactory();
			if (this.options.httpsSettings().enabled()) {
				tomcat.addAdditionalTomcatConnectors(createStandardConnector());
			}
			return tomcat;
		}

		@Bean
		public FaultInjectorFactory faultInjectorFactory() {
			return new TomcatFaultInjectorFactory();
		}

		@EventListener
		public void serverUp(EmbeddedServletContainerInitializedEvent event) {
			if (this.connector != null) {
				this.container.setLocalPort(this.connector.getLocalPort());
				this.container
						.setLocalHttpsPort(event.getEmbeddedServletContainer().getPort());
			}
		}

		private Connector createStandardConnector() {
			Connector connector = new Connector(
					"org.apache.coyote.http11.Http11NioProtocol");
			connector.setPort(this.options.portNumber());
			this.connector = connector;
			return connector;
		}

	}

	@Configuration
	@ConditionalOnMissingBean(EmbeddedServletContainerFactory.class)
	@ConditionalOnClass({ UndertowEmbeddedServletContainerFactory.class, Builder.class })
	static class UndertowContainerConfiguration {

		@Autowired
		private Options options;

		@Autowired
		private ContainerProperties container;

		private Integer port;

		@Bean
		public EmbeddedServletContainerFactory servletContainer() {
			UndertowEmbeddedServletContainerFactory undertow = new UndertowEmbeddedServletContainerFactory();
			if (this.options.httpsSettings().enabled()) {
				undertow.addBuilderCustomizers(new UndertowBuilderCustomizer() {
					@Override
					public void customize(Builder builder) {
						builder.addHttpListener(
								UndertowContainerConfiguration.this.options.portNumber(),
								"localhost");
						UndertowContainerConfiguration.this.port = UndertowContainerConfiguration.this.options
								.portNumber();
					}
				});
			}
			return undertow;
		}

		@Bean
		public FaultInjectorFactory faultInjectorFactory() {
			return new NoFaultInjectorFactory();
		}

		@EventListener
		public void serverUp(EmbeddedServletContainerInitializedEvent event) {
			if (this.port != null) {
				// TODO: make it dynamic as well
				this.container.setLocalPort(this.port);
				this.container
						.setLocalHttpsPort(event.getEmbeddedServletContainer().getPort());
			}
		}

	}

	@Configuration
	@ConditionalOnMissingBean(EmbeddedServletContainerFactory.class)
	@ConditionalOnClass({ JettyEmbeddedServletContainerFactory.class,
			ServerConnector.class })
	static class JettyContainerConfiguration {

		@Autowired
		private ContainerProperties container;

		@Autowired
		private Options options;

		private ServerConnector connector;

		@Bean
		public EmbeddedServletContainerFactory servletContainer() {
			final JettyEmbeddedServletContainerFactory jetty = new JettyEmbeddedServletContainerFactory();
			if (this.options.httpsSettings().enabled()) {
				jetty.addServerCustomizers(new JettyServerCustomizer() {
					@Override
					public void customize(Server server) {
						server.addConnector(createStandardConnector(server));
					}
				});
			}
			return jetty;
		}

		@Bean
		public JettyFaultInjectorFactory faultInjectorFactory() {
			return new JettyFaultInjectorFactory();
		}

		private org.eclipse.jetty.server.Connector createStandardConnector(
				Server server) {
			ServerConnector connector = new ServerConnector(server, -1, -1);
			connector.setHost("localhost");
			connector.setPort(this.options.portNumber());
			for (ConnectionFactory connectionFactory : connector
					.getConnectionFactories()) {
				if (connectionFactory instanceof HttpConfiguration.ConnectionFactory) {
					((HttpConfiguration.ConnectionFactory) connectionFactory)
							.getHttpConfiguration().setSendServerVersion(false);
				}
			}
			this.connector = connector;
			return connector;
		}

		@EventListener
		public void serverUp(EmbeddedServletContainerInitializedEvent event) {
			if (this.connector != null) {
				this.container.setLocalPort(this.connector.getLocalPort());
				this.container
						.setLocalHttpsPort(event.getEmbeddedServletContainer().getPort());
			}
		}
	}

}
