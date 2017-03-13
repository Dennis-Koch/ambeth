package com.koch.ambeth.audit;

import com.koch.ambeth.model.IAbstractBusinessObject;

public interface IAbstractAuditEntity extends IAbstractBusinessObject
{
	Short getVersion();
}
