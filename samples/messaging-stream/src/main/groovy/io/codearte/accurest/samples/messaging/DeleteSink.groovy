package io.codearte.accurest.samples.messaging

import org.springframework.cloud.stream.annotation.Input
import org.springframework.cloud.stream.messaging.Sink
import org.springframework.messaging.SubscribableChannel

/**
 * @author Marcin Grzejszczak
 */
interface DeleteSink extends Sink {

	String INPUT = "delete";

	@Input(DeleteSink.INPUT)
	SubscribableChannel delete();
}
