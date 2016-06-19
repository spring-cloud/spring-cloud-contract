package org.springframework.cloud.frauddetection;

import java.util.Collections;
import java.util.Set;

public class FraudRestApplication extends javax.ws.rs.core.Application {

	@Override
	public Set<Class<?>> getClasses() {
		return Collections.<Class<?>>singleton(FraudDetectionController.class);
	}

}