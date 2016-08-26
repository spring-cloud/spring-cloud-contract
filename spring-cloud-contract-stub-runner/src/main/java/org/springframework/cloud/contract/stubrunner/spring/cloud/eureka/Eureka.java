package org.springframework.cloud.contract.stubrunner.spring.cloud.eureka;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.netflix.eureka.CloudEurekaClient;
import org.springframework.cloud.netflix.eureka.EurekaClientConfigBean;
import org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean;
import org.springframework.cloud.netflix.eureka.InstanceInfoFactory;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClientConfig;
import com.netflix.discovery.shared.Applications;
import com.netflix.discovery.shared.resolver.ClosableResolver;
import com.netflix.discovery.shared.resolver.EurekaEndpoint;
import com.netflix.discovery.shared.resolver.aws.ApplicationsResolver;
import com.netflix.discovery.shared.resolver.aws.AwsEndpoint;
import com.netflix.discovery.shared.transport.EurekaHttpClient;
import com.netflix.discovery.shared.transport.EurekaHttpClientFactory;
import com.netflix.discovery.shared.transport.EurekaHttpClients;
import com.netflix.discovery.shared.transport.EurekaHttpResponse;
import com.netflix.discovery.shared.transport.EurekaTransportConfig;
import com.netflix.discovery.shared.transport.TransportClientFactory;
import com.netflix.discovery.shared.transport.decorator.MetricsCollectingEurekaHttpClient;
import com.netflix.discovery.shared.transport.jersey.JerseyEurekaHttpClientFactory;
import com.sun.jersey.api.client.filter.ClientFilter;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Taken from https://github.com/spencergibb/spring-cloud-netflix-eureka-lite
 *
 * @author Spencer Gibb
 *
 * @since 1.0.0
 */
public class Eureka {

	private static final Log log = LogFactory.getLog(MethodHandles.lookup().lookupClass());

	private final InetUtils inetUtils;
	final CloudEurekaClient eurekaClient;
	final EurekaClientConfigBean clientConfig;
	final EurekaTransport transport;

	public Eureka(InetUtils inetUtils, CloudEurekaClient eurekaClient) {
		this.inetUtils = inetUtils;
		this.eurekaClient = eurekaClient;
		this.clientConfig = new EurekaClientConfigBean();
		this.clientConfig.setRegisterWithEureka(false); // turn off registering with eureka, let apps send heartbeats.
		this.transport = createTransport();
	}

	public Registration register(Application application) {
		long start = System.currentTimeMillis();
		if (log.isDebugEnabled()) {
			log.debug(String.format("Starting registration of %s", application));
		}
		InstanceInfo instanceInfo = getInstanceInfo(application);
		Registration registration = new Registration(instanceInfo, application);
		long duration = (System.currentTimeMillis() - start) ;
		if (log.isDebugEnabled()) {
			log.debug(String.format("Created registration for %s in %s ms", application, duration));
		}
		register(registration);
		return registration;
	}

	public InstanceInfo getInstanceInfo(Application application, long lastUpdatedTimestamp, long lastDirtyTimestamp) {
		InstanceInfo instanceInfo = getInstanceInfo(application);
		instanceInfo = new InstanceInfo.Builder(instanceInfo)
				.setLastDirtyTimestamp(lastDirtyTimestamp)
				.setLastUpdatedTimestamp(lastUpdatedTimestamp)
				.build();
		return instanceInfo;
	}

	public InstanceInfo getInstanceInfo(Application application) {
		EurekaInstanceConfigBean instanceConfig = new EurekaInstanceConfigBean(inetUtils);
		instanceConfig.setInstanceEnabledOnit(true);
		instanceConfig.setAppname(application.getName());
		instanceConfig.setVirtualHostName(application.getName());
		instanceConfig.setInstanceId(application.getInstance_id());
		instanceConfig.setHostname(application.getHostname());
		instanceConfig.setNonSecurePort(application.getPort());

		return new InstanceInfoFactory().create(instanceConfig);
	}

	public EurekaTransport createTransport() {
		TransportClientFactory transportClientFactory = newTransportClientFactory(clientConfig, Collections.<ClientFilter>emptyList());
		EurekaTransportConfig transportConfig = clientConfig.getTransportConfig();

		ClosableResolver<AwsEndpoint> bootstrapResolver = EurekaHttpClients.newBootstrapResolver(
				clientConfig,
				transportConfig,
				transportClientFactory,
				null,
				new ApplicationsResolver.ApplicationsSource() {
					@Override
					public Applications getApplications(int stalenessThreshold, TimeUnit timeUnit) {
						long thresholdInMs = TimeUnit.MILLISECONDS.convert(stalenessThreshold, timeUnit);
						long delay = eurekaClient.getLastSuccessfulRegistryFetchTimePeriod();
						if (delay > thresholdInMs) {
							log.info(String.format("Local registry is too stale for local lookup. Threshold:%s, actual:%s",
									thresholdInMs, delay));
							return null;
						} else {
							return eurekaClient.getApplications();
						}
					}
				}
		);

		EurekaHttpClientFactory httpClientFactory;
		try {
			httpClientFactory = EurekaHttpClients.registrationClientFactory(
					bootstrapResolver,
					transportClientFactory,
					transportConfig
			);
		} catch (Exception e) {
			log.warn("Experimental transport initialization failure", e);
			throw new RuntimeException(e);
		}

		return new EurekaTransport(httpClientFactory, httpClientFactory.newClient(), transportClientFactory, bootstrapResolver);
	}

	public static TransportClientFactory newTransportClientFactory(
			final EurekaClientConfig clientConfig,
			final Collection<ClientFilter> additionalFilters) {
		final TransportClientFactory jerseyFactory = JerseyEurekaHttpClientFactory.create(
				clientConfig, additionalFilters, null, null);
		final TransportClientFactory metricsFactory = MetricsCollectingEurekaHttpClient.createFactory(jerseyFactory);

		return new TransportClientFactory() {
			@Override
			public EurekaHttpClient newClient(EurekaEndpoint serviceUrl) {
				return metricsFactory.newClient(serviceUrl);
			}

			@Override
			public void shutdown() {
				metricsFactory.shutdown();
				jerseyFactory.shutdown();
			}
		};
	}

	/**
	 * Renew with the eureka service by making the appropriate REST call
	 */
	public boolean renew(Registration registration) {
		InstanceInfo instanceInfo = registration.getInstanceInfo();
		EurekaHttpResponse<InstanceInfo> httpResponse;
		try {
			httpResponse = this.transport.getEurekaHttpClient().sendHeartBeat(instanceInfo.getAppName(), instanceInfo.getId(), instanceInfo, null);
			if (log.isDebugEnabled()) {
				log.debug(String.format("EurekaLite_%s/%s - Heartbeat status: %s", instanceInfo.getAppName(), instanceInfo.getId(), httpResponse.getStatusCode()));
			}
			if (httpResponse.getStatusCode() == HttpStatus.NOT_FOUND.value()) {
				log.info(String.format("EurekaLite_%s/%s - Re-registering apps/%s", instanceInfo.getAppName(), instanceInfo.getId(), instanceInfo.getAppName()));
				return register(registration);
			}
			return httpResponse.getStatusCode() == HttpStatus.OK.value();
		} catch (Exception e) {
			log.error("EurekaLite_"+instanceInfo.getAppName()+"/"+ instanceInfo.getId() + " - was unable to send heartbeat!", e);
			return false;
		}
	}

	/**
	 * Register with the eureka service by making the appropriate REST call.
	 */
	protected boolean register(Registration registration) {
		InstanceInfo instanceInfo = registration.getInstanceInfo();
		log.info(String.format("EurekaLite_%s/%s: registering service...", instanceInfo.getAppName(), instanceInfo.getId()));
		EurekaHttpResponse<Void> httpResponse;
		try {
			httpResponse = this.transport.getEurekaHttpClient().register(instanceInfo);
		} catch (Exception e) {
			log.warn("EurekaLite_"+instanceInfo.getAppName()+"/"+ instanceInfo.getId() + " - registration failed " + e.getMessage(), e);
			throw e;
		}
		if (log.isInfoEnabled()) {
			log.info(String.format("EurekaLite_%s/%s - registration status: %s", instanceInfo.getAppName(), instanceInfo.getId(), httpResponse.getStatusCode()));
		}
		return httpResponse.getStatusCode() == HttpStatus.NO_CONTENT.value();
	}

	public void shutdown(Registration registration) {
		InstanceInfo instanceInfo = registration.getInstanceInfo();
		try {
			EurekaHttpResponse<Void> httpResponse = this.transport.getEurekaHttpClient().cancel(instanceInfo.getAppName(), instanceInfo.getInstanceId());
			log.info(String.format("EurekaLite_%s/%s - deregister  status: %s", instanceInfo.getAppName(), instanceInfo.getId(), httpResponse.getStatusCode()));
		} catch (Exception e) {
			log.error("EurekaLite_"+instanceInfo.getAppName()+"/"+ instanceInfo.getId() + " - de-registration failed " + e.getMessage(), e);
		}
		this.transport.shutdown();
	}
}

/**
 * Taken from https://github.com/spencergibb/spring-cloud-netflix-eureka-lite
 *
 * @author Spencer Gibb
 *
 * @since 1.0.0
 */
class EurekaTransport {
	private final EurekaHttpClientFactory eurekaHttpClientFactory;
	private final EurekaHttpClient eurekaHttpClient;
	private final TransportClientFactory transportClientFactory;
	private final ClosableResolver closableResolver;

	public EurekaTransport(EurekaHttpClientFactory eurekaHttpClientFactory,
			EurekaHttpClient eurekaHttpClient,
			TransportClientFactory transportClientFactory,
			ClosableResolver closableResolver) {
		this.eurekaHttpClientFactory = eurekaHttpClientFactory;
		this.eurekaHttpClient = eurekaHttpClient;
		this.transportClientFactory = transportClientFactory;
		this.closableResolver = closableResolver;
	}

	public void shutdown() {
		eurekaHttpClientFactory.shutdown();
		eurekaHttpClient.shutdown();
		transportClientFactory.shutdown();
		closableResolver.shutdown();
	}

	public EurekaHttpClientFactory getEurekaHttpClientFactory() {
		return eurekaHttpClientFactory;
	}

	public EurekaHttpClient getEurekaHttpClient() {
		return eurekaHttpClient;
	}

	public TransportClientFactory getTransportClientFactory() {
		return transportClientFactory;
	}

	public ClosableResolver getClosableResolver() {
		return closableResolver;
	}
}

/**
 * Taken from https://github.com/spencergibb/spring-cloud-netflix-eureka-lite
 *
 * @author Spencer Gibb
 *
 * @since 1.0.0
 */
class Application {
	private String name;
	private String instance_id;
	private String hostname;
	private int port;

	public Application(String name, String instance_id, String hostname, int port) {
		this.name = name;
		this.instance_id = instance_id;
		this.hostname = hostname;
		this.port = port;
	}

	public Application() {
	}

	@JsonIgnore
	public String getRegistrationKey() {
		return computeRegistrationKey(this.name, instance_id);
	}

	static String computeRegistrationKey(String name, String instanceId) {
		return name + ":" + instanceId;
	}

	public String getName() {
		return name;
	}

	public String getInstance_id() {
		return instance_id;
	}

	public String getHostname() {
		return hostname;
	}

	public int getPort() {
		return port;
	}
}

/**
 * Scheduled service that automatically will renew registrations in Eureka
 */
class Renewer implements Runnable {

	private static final Log log = LogFactory.getLog(MethodHandles.lookup().lookupClass());

	final Eureka eureka;
	final Registration registration;
	final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	Renewer(int flushInterval, Eureka eureka,
			Registration registration) {
		this.eureka = eureka;
		this.registration = registration;
		this.scheduler.scheduleWithFixedDelay(this, 0, flushInterval, SECONDS);
	}

	@Override
	public void run() {
		if (log.isTraceEnabled()) {
			log.trace("Renewing registration [" + this.registration + "]");
		}
		this.eureka.renew(this.registration);
	}
}