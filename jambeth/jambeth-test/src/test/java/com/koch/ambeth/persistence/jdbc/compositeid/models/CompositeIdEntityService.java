package com.koch.ambeth.persistence.jdbc.compositeid.models;

import com.koch.ambeth.service.proxy.Service;

@Service(ICompositeIdEntityService.class)
public class CompositeIdEntityService implements ICompositeIdEntityService {
	@Override
	public CompositeIdEntity create(CompositeIdEntity entity) {
		throw new UnsupportedOperationException();
	}

	@Override
	public CompositeIdEntity update(CompositeIdEntity entity) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete(CompositeIdEntity entity) {
		throw new UnsupportedOperationException();
	}
}
