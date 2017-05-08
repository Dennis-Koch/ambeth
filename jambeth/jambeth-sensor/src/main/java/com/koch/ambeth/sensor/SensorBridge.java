package com.koch.ambeth.sensor;

/*-
 * #%L
 * jambeth-sensor
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import java.util.List;
import java.util.concurrent.locks.Lock;

import com.koch.ambeth.util.sensor.ISensor;
import com.koch.ambeth.util.sensor.ISensorReceiver;

public class SensorBridge implements ISensor {
	protected final List<ISensorReceiver> sensors;

	protected final String sensorName;

	protected final Lock writeLock;

	public SensorBridge(String sensorName, List<ISensorReceiver> sensors, Lock writeLock) {
		this.sensorName = sensorName;
		this.sensors = sensors;
		this.writeLock = writeLock;
	}

	@Override
	public void touch() {
		List<ISensorReceiver> sensors = this.sensors;
		if (sensors.size() == 0) {
			return;
		}
		String sensorName = this.sensorName;
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try {
			for (int a = 0, size = sensors.size(); a < size; a++) {
				sensors.get(a).touch(sensorName);
			}
		}
		finally {
			writeLock.unlock();
		}
	}

	@Override
	public void touch(Object... additionalData) {
		List<ISensorReceiver> sensors = this.sensors;
		if (sensors.size() == 0) {
			return;
		}
		String sensorName = this.sensorName;
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try {
			for (int a = 0, size = sensors.size(); a < size; a++) {
				sensors.get(a).touch(sensorName, additionalData);
			}
		}
		finally {
			writeLock.unlock();
		}
	}

	@Override
	public void on() {
		List<ISensorReceiver> sensors = this.sensors;
		if (sensors.size() == 0) {
			return;
		}
		String sensorName = this.sensorName;
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try {
			for (int a = 0, size = sensors.size(); a < size; a++) {
				sensors.get(a).on(sensorName);
			}
		}
		finally {
			writeLock.unlock();
		}
	}

	@Override
	public void on(Object... additionalData) {
		List<ISensorReceiver> sensors = this.sensors;
		if (sensors.size() == 0) {
			return;
		}
		String sensorName = this.sensorName;
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try {
			for (int a = 0, size = sensors.size(); a < size; a++) {
				sensors.get(a).on(sensorName, additionalData);
			}
		}
		finally {
			writeLock.unlock();
		}
	}

	@Override
	public void off() {
		List<ISensorReceiver> sensors = this.sensors;
		if (sensors.size() == 0) {
			return;
		}
		String sensorName = this.sensorName;
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try {
			for (int a = 0, size = sensors.size(); a < size; a++) {
				sensors.get(a).off(sensorName);
			}
		}
		finally {
			writeLock.unlock();
		}
	}

}
