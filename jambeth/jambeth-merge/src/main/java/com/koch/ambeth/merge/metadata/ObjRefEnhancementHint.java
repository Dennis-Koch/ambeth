package com.koch.ambeth.merge.metadata;

import java.io.Serializable;

import org.objectweb.asm.Type;

import com.koch.ambeth.ioc.bytecode.IEnhancementHint;
import com.koch.ambeth.ioc.bytecode.ITargetNameEnhancementHint;
import com.koch.ambeth.merge.transfer.ObjRef;

public class ObjRefEnhancementHint implements IEnhancementHint, ITargetNameEnhancementHint, Serializable
{
	private static final long serialVersionUID = 4547369981492803630L;

	protected final Class<?> entityType;

	protected final int idIndex;

	public ObjRefEnhancementHint(Class<?> entityType, int idIndex)
	{
		this.entityType = entityType;
		this.idIndex = idIndex;
	}

	public Class<?> getEntityType()
	{
		return entityType;
	}

	public int getIdIndex()
	{
		return idIndex;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		if (!getClass().equals(obj.getClass()))
		{
			return false;
		}
		ObjRefEnhancementHint other = (ObjRefEnhancementHint) obj;
		return getEntityType().equals(other.getEntityType()) && idIndex == other.getIdIndex();
	}

	@Override
	public int hashCode()
	{
		return getClass().hashCode() ^ getEntityType().hashCode() ^ getIdIndex();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends IEnhancementHint> T unwrap(Class<T> includedHintType)
	{
		if (ObjRefEnhancementHint.class.equals(includedHintType))
		{
			return (T) this;
		}
		return null;
	}

	@Override
	public String getTargetName(Class<?> typeToEnhance)
	{
		return Type.getInternalName(entityType) + "$" + ObjRef.class.getSimpleName() + "$" + (idIndex == ObjRef.PRIMARY_KEY_INDEX ? "PK" : "AK" + idIndex);
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + ": " + entityType.getSimpleName() + "-" + (idIndex == ObjRef.PRIMARY_KEY_INDEX ? "PK" : "AK" + idIndex);
	}
}
