package com.koch.ambeth.merge.event;

public class RefreshEntitiesOfType {
	private final Class<?>[] entityTypes;

	public RefreshEntitiesOfType(Class<?>... entityTypes) {
		this.entityTypes = entityTypes;
	}

	public Class<?>[] getEntityTypes() {
		return entityTypes;
	}
}
