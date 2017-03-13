package com.koch.ambeth.persistence;

import com.koch.ambeth.service.cache.model.IObjRelation;
import com.koch.ambeth.service.merge.model.IObjRef;

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
