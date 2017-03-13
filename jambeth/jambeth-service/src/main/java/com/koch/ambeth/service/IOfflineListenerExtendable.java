package com.koch.ambeth.service;

public interface IOfflineListenerExtendable
{
	void addOfflineListener(IOfflineListener offlineListener);

	void removeOfflineListener(IOfflineListener offlineListener);
}