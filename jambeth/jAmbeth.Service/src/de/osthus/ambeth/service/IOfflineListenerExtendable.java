package de.osthus.ambeth.service;

public interface IOfflineListenerExtendable
{
	void addOfflineListener(IOfflineListener offlineListener);

	void removeOfflineListener(IOfflineListener offlineListener);
}