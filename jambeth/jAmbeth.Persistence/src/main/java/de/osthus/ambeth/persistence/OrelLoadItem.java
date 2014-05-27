package de.osthus.ambeth.persistence;

import de.osthus.ambeth.cache.model.IObjRelation;
import de.osthus.ambeth.merge.model.IObjRef;

public class OrelLoadItem
{
	protected final IObjRef objRef;

	protected final IObjRelation objRel;

	public OrelLoadItem(IObjRef objRef, IObjRelation objRel)
	{
		this.objRef = objRef;
		this.objRel = objRel;
	}

	public IObjRef getObjRef()
	{
		return objRef;
	}

	public IObjRelation getObjRel()
	{
		return objRel;
	}
}
