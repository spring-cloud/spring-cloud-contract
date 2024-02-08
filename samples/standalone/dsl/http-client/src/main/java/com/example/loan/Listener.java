/*
 * Copyright 2013-2020 the original author or authors.
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

package com.example.loan;

import java.util.function.Consumer;

import org.springframework.stereotype.Component;

@Component("listener")
class Listener implements Consumer<SensorData> {

	int count = 0;

	public void logSensorData(SensorData data) {
		System.out.println(data);
		this.count = this.count + 1;
	}

	public int getCount() {
		return this.count;
	}

	@Override
	public void accept(SensorData sensorData) {
		this.logSensorData(sensorData);
	}
}
