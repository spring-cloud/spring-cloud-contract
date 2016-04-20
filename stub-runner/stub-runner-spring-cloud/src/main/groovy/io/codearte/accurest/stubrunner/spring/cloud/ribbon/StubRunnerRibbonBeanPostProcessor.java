package io.codearte.accurest.stubrunner.spring.cloud.ribbon;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.ServerList;
import io.codearte.accurest.stubrunner.StubFinder;
import io.codearte.accurest.stubrunner.spring.cloud.StubMapperProperties;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * Ribbon AutoConfiguration that manipulates the service id to make the service
 * be picked from the list of available WireMock instance if one is available.
 *
 * @author Marcin Grzejszczak
 */
class StubRunnerRibbonBeanPostProcessor implements BeanPostProcessor {

	private final BeanFactory beanFactory;
	private StubFinder stubFinder;
	private StubMapperProperties stubMapperProperties;
	private IClientConfig clientConfig;

	StubRunnerRibbonBeanPostProcessor(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	private StubFinder stubFinder() {
		if (stubFinder == null) {
			stubFinder = this.beanFactory.getBean(StubFinder.class);
		}
		return stubFinder;
	}

	private StubMapperProperties stubMapperProperties() {
		if (stubMapperProperties == null) {
			stubMapperProperties = this.beanFactory.getBean(StubMapperProperties.class);
		}
		return stubMapperProperties;
	}

	private IClientConfig clientConfig() {
		if (clientConfig == null) {
			clientConfig = this.beanFactory.getBean(IClientConfig.class);
		}
		return clientConfig;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof ServerList && !(bean instanceof StubRunnerRibbonServerList)) {
			return new StubRunnerRibbonServerList(stubFinder(), stubMapperProperties(), clientConfig(), (ServerList) bean);
		}
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}
}
