package org.springframework.cloud.contract.stubrunner.spring.cloud.eureka;

import com.netflix.appinfo.InstanceInfo;

public class ApplicationStatus {
	private Application application;
	private InstanceInfo.InstanceStatus status;

	public ApplicationStatus(Application application,
			InstanceInfo.InstanceStatus status) {
		this.application = application;
		this.status = status;
	}

	public ApplicationStatus() {
	}

	public Application getApplication() {
		return application;
	}

	public InstanceInfo.InstanceStatus getStatus() {
		return status;
	}
}
