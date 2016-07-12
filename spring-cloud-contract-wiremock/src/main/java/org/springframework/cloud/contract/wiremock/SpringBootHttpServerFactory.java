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

import org.apache.catalina.connector.Connector;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.autoconfigure.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.web.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.EmbeddedServletContainerAutoConfiguration.BeanPostProcessorsRegistrar;
import org.springframework.boot.autoconfigure.web.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.ServerPropertiesAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.Ssl;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.web.context.ServletContextAware;

import com.github.tomakehurst.wiremock.common.HttpsSettings;
import com.github.tomakehurst.wiremock.common.Notifier;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.core.WireMockApp;
import com.github.tomakehurst.wiremock.http.AdminRequestHandler;
import com.github.tomakehurst.wiremock.http.HttpServer;
import com.github.tomakehurst.wiremock.http.HttpServerFactory;
import com.github.tomakehurst.wiremock.http.RequestHandler;
import com.github.tomakehurst.wiremock.http.StubRequestHandler;
import com.github.tomakehurst.wiremock.servlet.WireMockHandlerDispatchingServlet;

/**
 * @author Dave Syer
 *
 */
public class SpringBootHttpServerFactory implements HttpServerFactory {

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
				.logStartupInfo(false).bannerMode(Mode.OFF).listeners(this).run();
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
		return this.options.portNumber();
	}

	@Override
	public int httpsPort() {
		return this.options.httpsSettings().port();
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
			if (bean instanceof ServerProperties) {
				ServerProperties server = (ServerProperties) bean;
				server.setPort(getPort());
				setupHttps(server, SpringBootHttpServer.this.options.httpsSettings());
				// TODO: other options
			}
			return bean;
		}

		private void setupHttps(ServerProperties server, HttpsSettings httpsSettings) {
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

@Configuration
@Import({ ServerPropertiesAutoConfiguration.class, BeanPostProcessorsRegistrar.class,
		ConfigurationPropertiesAutoConfiguration.class, JacksonAutoConfiguration.class,
		HttpMessageConvertersAutoConfiguration.class,
		PropertyPlaceholderAutoConfiguration.class })
class WiremockServerConfiguration {

	@Autowired
	private AdminRequestHandler adminRequestHandler;
	@Autowired
	private StubRequestHandler stubRequestHandler;
	@Autowired
	private Options options;

	@Bean(name = DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_REGISTRATION_BEAN_NAME)
	public ServletRegistrationBean stubServletRegistration() {
		ServletRegistrationBean reg = new ServletRegistrationBean();
		reg.addInitParameter(RequestHandler.HANDLER_CLASS_KEY,
				StubRequestHandler.class.getName());
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
				servletContext.setAttribute(Notifier.KEY, options.notifier());
			}
		};
	}

	@Bean
	public EmbeddedServletContainerFactory servletContainer() {
		// TODO support for other containers
		TomcatEmbeddedServletContainerFactory tomcat = new TomcatEmbeddedServletContainerFactory();
		if (this.options.httpsSettings().enabled()) {
			tomcat.addAdditionalTomcatConnectors(createStandardConnector());
		}
		return tomcat;
	}

	private Connector createStandardConnector() {
		Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
		connector.setPort(this.options.portNumber());
		return connector;
	}

}
