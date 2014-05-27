package de.osthus.ambeth.sensor;

public interface ISensor
{
	void touch();

	void touch(Object... additionalData);

	void on(Object... additionalData);

	void on();

	void off();
}
