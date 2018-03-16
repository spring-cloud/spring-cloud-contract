package org.springframework.cloud.contract.stubrunner.spring;

import java.lang.reflect.Field;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.env.Environment;
import org.springframework.util.ReflectionUtils;

/**
 * Injects {@link StubRunnerPort} ports into fields
 *
 * @author Marcin Grzejszczak
 * @since 2.0.0
 */
class StubRunnerPortBeanPostProcessor implements BeanPostProcessor {

	private final Environment environment;

	StubRunnerPortBeanPostProcessor(Environment environment) {
		this.environment = environment;
	}

	@Override public Object postProcessAfterInitialization(Object bean, String beanName)
			throws BeansException {
		injectStubRunnerPort(bean);
		return bean;
	}

	private void injectStubRunnerPort(Object bean) {
		Class<?> clazz = bean.getClass();
		ReflectionUtils.FieldCallback fieldCallback =
				new StubRunnerPortFieldCallback(this.environment, bean);
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

	@Override public void doWith(Field field)
			throws IllegalArgumentException, IllegalAccessException {
		if (!field.isAnnotationPresent(StubRunnerPort.class)) {
			return;
		}
		ReflectionUtils.makeAccessible(field);
		String stub = field.getDeclaredAnnotation(StubRunnerPort.class).value();
		Integer port = this.environment.getProperty(
				StubRunnerConfiguration.STUBRUNNER_PREFIX + "." + stub.replace(":", ".") + ".port", Integer.class);
		if (port != null) {
			field.set(this.bean, port);
		}
	}
}