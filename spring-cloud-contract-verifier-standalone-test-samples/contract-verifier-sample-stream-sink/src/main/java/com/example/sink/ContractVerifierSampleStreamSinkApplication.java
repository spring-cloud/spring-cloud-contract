package com.example.sink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;

@SpringBootApplication
@EnableBinding(Sink.class)
public class ContractVerifierSampleStreamSinkApplication {

	@StreamListener(Sink.INPUT)
	public void logSensorData(SensorData data) {
		System.out.println(data);
	}

	public static void main(String[] args) {
		SpringApplication.run(ContractVerifierSampleStreamSinkApplication.class, args);
	}
}
