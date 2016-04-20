package io.codearte.accurest.stubrunner.spring.cloud.ribbon;

import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerList;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
class StubRunnerRibbonConfiguration {
	@Bean
	StubRunnerRibbonBeanPostProcessor stubRunnerRibbonBeanPostProcessor(BeanFactory beanFactory) {
		return new StubRunnerRibbonBeanPostProcessor(beanFactory);
	}

	@Bean
	@ConditionalOnMissingBean
	public ServerList<?> noOpServerList() {
		return new ServerList<Server>() {
			@Override
			public List<Server> getInitialListOfServers() {
				return new ArrayList<>();
			}

			@Override
			public List<Server> getUpdatedListOfServers() {
				return new ArrayList<>();
			}
		};
	}
}
