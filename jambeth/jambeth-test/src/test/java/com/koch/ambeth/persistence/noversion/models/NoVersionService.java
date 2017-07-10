package com.koch.ambeth.persistence.noversion.models;

import com.koch.ambeth.service.proxy.Service;

@Service(INoVersionService.class)
public class NoVersionService implements INoVersionService {
	@Override
	public NoVersion create(NoVersion entity) {
		throw new UnsupportedOperationException();
	}

	@Override
	public NoVersion update(NoVersion entity) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete(NoVersion entity) {
		throw new UnsupportedOperationException();
	}
}
