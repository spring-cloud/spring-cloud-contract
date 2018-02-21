package com.example.sink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.stereotype.Component;

@SpringBootApplication
@EnableBinding(Sink.class)
public class ContractVerifierSampleStreamSinkApplication {



	public static void main(String[] args) {
		SpringApplication.run(ContractVerifierSampleStreamSinkApplication.class, args);
	}
}

@Component
class Listener {

	int count = 0;

	@StreamListener(Sink.INPUT)
	public void logSensorData(SensorData data) {
		System.out.println(data);
		this.count = this.count + 1;
	}

	public int getCount() {
		return this.count;
	}
}