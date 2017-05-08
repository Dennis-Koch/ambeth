package com.koch.ambeth.mina.client;

/*-
 * #%L
 * jambeth-mina
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

import org.apache.mina.transport.serial.SerialAddress;
import org.apache.mina.transport.serial.SerialAddress.DataBits;
import org.apache.mina.transport.serial.SerialAddress.FlowControl;
import org.apache.mina.transport.serial.SerialAddress.Parity;
import org.apache.mina.transport.serial.SerialAddress.StopBits;

import com.koch.ambeth.util.ParamChecker;

/**
 * Class that holds the communication parameters to configure the serial address.
 */
public class MinaCommunicationParameter {

	private int baudRate;
	private DataBits databits;
	private Parity parity;
	private StopBits stopBits;
	private FlowControl handShake;

	@SuppressWarnings("unused")
	private MinaCommunicationParameter() {
		// intended blank
	}

	public MinaCommunicationParameter(int baudRate, DataBits databits, Parity parity,
			StopBits stopBits, FlowControl handShake) {
		this.baudRate = baudRate;
		this.databits = databits;
		this.parity = parity;
		this.stopBits = stopBits;
		this.handShake = handShake;
	}

	public int getBaudRate() {
		return baudRate;
	}

	public DataBits getDatabits() {
		return databits;
	}

	public Parity getParity() {
		return parity;
	}

	public StopBits getStopBits() {
		return stopBits;
	}

	public FlowControl getHandShake() {
		return handShake;
	}

	/**
	 * Get the SerialAddress constructed with the communication parameter values and the given serial
	 * port.
	 *
	 * @param name Serial port (e.g. COM1); mandatory
	 * @return SerialAddress; never null
	 */
	public SerialAddress getSerialAddress(String name) {
		ParamChecker.assertNotNull(name, "serial port");
		return new SerialAddress(name, getBaudRate(), getDatabits(), getStopBits(), getParity(),
				getHandShake());
	}

}
