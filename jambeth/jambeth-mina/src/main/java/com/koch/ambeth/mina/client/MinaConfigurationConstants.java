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

/**
 * MINA configuration constants.
 */
public final class MinaConfigurationConstants {

	// ---- INNER CLASSES ------------------------------------------------------

	// ---- CONSTANTS ----------------------------------------------------------

	public static final String PROPERTY_NAME_CONNECT_TIMEOUT_IN_SECONDS =
			"Connect.Timeout.In.Seconds";
	public static final String PROPERTY_NAME_COMMAND_TIMEOUT_IN_SECONDS =
			"Command.Timeout.In.Seconds";

	public static final String PROPERTY_NAME_COM_BAUD_RATE = "Com.Baud.Rate=9600";
	public static final String PROPERTY_NAME_COM_DATABITS = "Com.Databits";
	public static final String PROPERTY_NAME_COM_PARITY = "Com.Parity";
	public static final String PROPERTY_NAME_COM_STOP_BITS = "Com.Stop.Bits=StopBits";
	public static final String PROPERTY_NAME_COM_HAND_SHAKE = "Com.Hand.Shake";

	public static final String DEFAULT_COM_BAUD_RATE = "9600";
	public static final String DEFAULT_COM_DATABITS = "DATABITS_7";
	public static final String DEFAULT_COM_PARITY = "EVEN";
	public static final String DEFAULT_COM_STOP_BITS = "BITS_1";
	public static final String DEFAULT_COM_HAND_SHAKE = "NONE";

	private MinaConfigurationConstants() {
		// No instance allowed
	}

}
