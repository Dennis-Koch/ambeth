package de.osthus.ambeth.audit;

import de.osthus.ambeth.model.IAbstractEntity;
import de.osthus.ambeth.security.User;
import de.osthus.ambeth.security.model.ISignature;

public interface Signature extends IAbstractEntity, ISignature
{
	@Override
	User getUser();

	void setUser(User user);
}
