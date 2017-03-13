package com.koch.ambeth.audit;

import com.koch.ambeth.model.IAbstractEntity;
import com.koch.ambeth.security.model.ISignature;

public interface Signature extends IAbstractEntity, ISignature
{
	@Override
	User getUser();

	void setUser(User user);
}
