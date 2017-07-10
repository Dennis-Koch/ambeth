package com.koch.ambeth.testutil.contextstore;

public class BlaDServiceProviderImpl implements BlaDServiceProvider {
	private BlaDServicePortType service;

	@Override
	public BlaDServicePortType getService() {
		return service;
	}

	public void setService(BlaDServicePortType service) {
		this.service = service;
	}
}
