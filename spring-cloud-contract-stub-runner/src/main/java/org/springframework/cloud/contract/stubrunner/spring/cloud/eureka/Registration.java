package org.springframework.cloud.contract.stubrunner.spring.cloud.eureka;

import com.netflix.appinfo.InstanceInfo;

/**
 * Taken from https://github.com/spencergibb/spring-cloud-netflix-eureka-lite
 *
 * @author Spencer Gibb
 *
 * @since 1.0.0
 */
public class Registration {
	private final InstanceInfo instanceInfo;
	private final ApplicationStatus applicationStatus;

	public Registration(InstanceInfo instanceInfo, ApplicationStatus applicationStatus) {
		this.instanceInfo = instanceInfo;
		this.applicationStatus = applicationStatus;
	}

	public Registration(InstanceInfo instanceInfo, Application application) {
		this(instanceInfo, new ApplicationStatus(application, InstanceInfo.InstanceStatus.UP));
	}

	public String getRegistrationKey() {
		return this.applicationStatus.getApplication().getRegistrationKey();
	}

	public String getApplicationName() {
		return this.applicationStatus.getApplication().getName();
	}

	public InstanceInfo getInstanceInfo() {
		return this.instanceInfo;
	}

	public ApplicationStatus getApplicationStatus() {
		return this.applicationStatus;
	}

	@Override public String toString() {
		return "Registration{" + "instanceInfo=" + this.instanceInfo + ", applicationStatus="
				+ this.applicationStatus + '}';
	}
}
