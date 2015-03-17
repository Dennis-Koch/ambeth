package de.osthus.ambeth.audit;

import de.osthus.ambeth.model.IAbstractBusinessObject;

public interface IAbstractAuditEntity extends IAbstractBusinessObject
{
	Short getVersion();
}
