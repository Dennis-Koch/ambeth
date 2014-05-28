package de.osthus.ambeth.service;

public interface IOfflineListener
{
	void beginOnline();

	void handleOnline();

	void endOnline();

	void beginOffline();

	void handleOffline();

	void endOffline();
}