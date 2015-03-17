package de.osthus.ambeth.objrefstore;

public abstract class IObjRefStoreEntryProvider
{
	public abstract ObjRefStore createObjRefStore(Class<?> entityType, byte idIndex, Object id);

	public abstract ObjRefStore createObjRefStore(Class<?> entityType, byte idIndex, Object id, ObjRefStore nextEntry);
}
