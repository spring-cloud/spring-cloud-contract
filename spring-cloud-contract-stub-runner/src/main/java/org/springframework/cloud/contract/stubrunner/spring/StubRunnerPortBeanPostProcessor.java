/*
 * Copyright 2013-2019 the original author or authors.
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

package org.springframework.cloud.contract.stubrunner.spring;

import java.lang.reflect.Field;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.env.Environment;
import org.springframework.util.ReflectionUtils;

/**
 * Injects {@link StubRunnerPort} ports into fields.
 *
 * @author Marcin Grzejszczak
 * @since 2.0.0
 */
class StubRunnerPortBeanPostProcessor implements BeanPostProcessor {

	private final Environment environment;

	StubRunnerPortBeanPostProcessor(Environment environment) {
		this.environment = environment;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName)
			throws BeansException {
		injectStubRunnerPort(bean);
		return bean;
	}

	private void injectStubRunnerPort(Object bean) {
		Class<?> clazz = bean.getClass();
		ReflectionUtils.FieldCallback fieldCallback = new StubRunnerPortFieldCallback(
				this.environment, bean);
		ReflectionUtils.doWithFields(clazz, fieldCallback);
	}

}

class StubRunnerPortFieldCallback implements ReflectionUtils.FieldCallback {

	private final Environment environment;

	private final Object bean;

	StubRunnerPortFieldCallback(Environment environment, Object bean) {
		this.environment = environment;
		this.bean = bean;
	}

	@Override
	public void doWith(Field field)
			throws IllegalArgumentException, IllegalAccessException {
		if (!field.isAnnotationPresent(StubRunnerPort.class)) {
			return;
		}
		ReflectionUtils.makeAccessible(field);
		String stub = field.getDeclaredAnnotation(StubRunnerPort.class).value();
		Integer port = this.environment
				.getProperty(StubRunnerConfiguration.STUBRUNNER_PREFIX + "."
						+ stub.replace(":", ".") + ".port", Integer.class);
		if (port != null) {
			field.set(this.bean, port);
		}
	}

}
