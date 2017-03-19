package com.koch.ambeth.mina.client;

/**
 * MINA configuration constants.
 */
public final class MinaConfigurationConstants
{

	// ---- INNER CLASSES ------------------------------------------------------

	// ---- CONSTANTS ----------------------------------------------------------

	public static final String PROPERTY_NAME_CONNECT_TIMEOUT_IN_SECONDS = "Connect.Timeout.In.Seconds";
	public static final String PROPERTY_NAME_COMMAND_TIMEOUT_IN_SECONDS = "Command.Timeout.In.Seconds";

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

	private MinaConfigurationConstants()
	{
		// No instance allowed
	}

}
