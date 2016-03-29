package io.codearte.accurest.stubrunner.spring.cloud.ribbon;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.netflix.ribbon.RibbonAutoConfiguration;
import org.springframework.cloud.netflix.ribbon.RibbonClients;
import org.springframework.context.annotation.Configuration;

import com.netflix.loadbalancer.ServerList;

@Configuration
@ConditionalOnClass(ServerList.class)
@AutoConfigureAfter(RibbonAutoConfiguration.class)
@RibbonClients(defaultConfiguration = StubRunnerRibbonConfiguration.class)
public class StubRunnerRibbonAutoConfiguration {

}
