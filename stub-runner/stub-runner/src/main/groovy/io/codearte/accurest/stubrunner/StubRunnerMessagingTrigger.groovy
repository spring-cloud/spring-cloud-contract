package io.codearte.accurest.stubrunner

import groovy.transform.PackageScope
import io.codearte.accurest.messaging.AccurestMessaging
/**
 * @author Marcin Grzejszczak
 */
@PackageScope
class StubRunnerMessagingTrigger {

	private final AccurestMessaging accurestMessaging

	StubRunnerMessagingTrigger(AccurestMessaging accurestMessaging) {
		this.accurestMessaging = accurestMessaging
	}

	void trigger
}
