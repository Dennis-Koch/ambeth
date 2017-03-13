package com.koch.ambeth.util.sensor;

public interface ISensor
{
	void touch();

	void touch(Object... additionalData);

	void on(Object... additionalData);

	void on();

	void off();
}
