package com.koch.ambeth.service;

public class NoOpOfflineExtendable implements IOfflineListenerExtendable {
	@Override
	public void addOfflineListener(IOfflineListener offlineListener) {
		// Intended NoOp!
	}

	@Override
	public void removeOfflineListener(IOfflineListener offlineListener) {
		// Intended NoOp!
	}
}
