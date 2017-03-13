package com.koch.ambeth.merge.objrefstore;

public abstract class IObjRefStoreFactory
{
	public abstract ObjRefStore createObjRef();

	public abstract ObjRefStore createObjRef(Object id, Object version);
}
