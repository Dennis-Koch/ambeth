package de.osthus.ambeth.objrefstore;

public abstract class IObjRefStoreFactory
{
	public abstract ObjRefStore createObjRef();

	public abstract ObjRefStore createObjRef(Object id, Object version);
}
