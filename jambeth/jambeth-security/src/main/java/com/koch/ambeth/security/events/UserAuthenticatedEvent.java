package com.koch.ambeth.security.events;

import java.util.Objects;

import com.koch.ambeth.security.IAuthorization;
import com.koch.ambeth.util.IImmutableType;

public class UserAuthenticatedEvent implements IImmutableType {
	private final IAuthorization authorization;

	public UserAuthenticatedEvent(IAuthorization authorization) {
		Objects.requireNonNull(authorization, "authorization");
		this.authorization = authorization;
	}

	public IAuthorization getAuthorization() {
		return authorization;
	}

	@Override
	public String toString() {
		return authorization.toString();
	}
}
