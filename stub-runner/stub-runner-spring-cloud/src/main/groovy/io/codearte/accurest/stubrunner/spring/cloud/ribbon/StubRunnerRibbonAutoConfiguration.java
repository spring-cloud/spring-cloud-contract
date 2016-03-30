package io.codearte.accurest.stubrunner.spring.cloud.ribbon;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.netflix.ribbon.RibbonAutoConfiguration;
import org.springframework.cloud.netflix.ribbon.RibbonClients;
import org.springframework.context.annotation.Configuration;

import com.netflix.loadbalancer.ServerList;

import io.codearte.accurest.stubrunner.spring.cloud.StubMapperProperties;

@Configuration
@ConditionalOnClass(ServerList.class)
@ConditionalOnBean(StubMapperProperties.class)
@AutoConfigureAfter(RibbonAutoConfiguration.class)
@RibbonClients(defaultConfiguration = StubRunnerRibbonConfiguration.class)
@ConditionalOnProperty(value = "stubrunner.cloud.ribbon.enabled", matchIfMissing = true)
public class StubRunnerRibbonAutoConfiguration {

}
