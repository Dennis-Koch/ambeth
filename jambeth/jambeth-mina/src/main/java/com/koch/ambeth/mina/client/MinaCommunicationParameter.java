package com.koch.ambeth.mina.client;

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
