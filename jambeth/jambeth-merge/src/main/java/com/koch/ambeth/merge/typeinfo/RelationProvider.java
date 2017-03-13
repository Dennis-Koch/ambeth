package com.koch.ambeth.merge.typeinfo;

import java.util.Arrays;

import javax.persistence.Embeddable;

import com.koch.ambeth.ioc.extendable.ClassExtendableContainer;
import com.koch.ambeth.ioc.util.ImmutableTypeSet;
import com.koch.ambeth.util.IImmutableType;
import com.koch.ambeth.util.collections.SmartCopySet;
import com.koch.ambeth.util.typeinfo.INoEntityTypeExtendable;
import com.koch.ambeth.util.typeinfo.IRelationProvider;

public class RelationProvider implements IRelationProvider, INoEntityTypeExtendable
{
	protected final SmartCopySet<Class<?>> primitiveTypes = new SmartCopySet<Class<?>>();

	protected final ClassExtendableContainer<Boolean> noEntityTypeExtendables = new ClassExtendableContainer<Boolean>("flag", "noEntityType");

	public RelationProvider()
	{
		ImmutableTypeSet.addImmutableTypesTo(primitiveTypes);

		primitiveTypes.addAll(Arrays.asList(new Class<?>[] { Object.class, java.util.Date.class, java.sql.Date.class, java.sql.Timestamp.class,
				java.util.Calendar.class, java.lang.Integer.class, java.lang.Long.class, java.lang.Double.class, java.lang.Float.class, java.lang.Short.class,
				java.lang.Character.class, java.lang.Byte.class }));
		primitiveTypes.add(java.util.GregorianCalendar.class);
		primitiveTypes.add(javax.xml.datatype.XMLGregorianCalendar.class);
	}

	@Override
	public boolean isEntityType(Class<?> type)
	{
		if (type == null || type.isPrimitive() || type.isEnum() || primitiveTypes.contains(type) || Boolean.TRUE == noEntityTypeExtendables.getExtension(type))
		{
			return false;
		}
		if (type.isAnnotationPresent(Embeddable.class) || IImmutableType.class.isAssignableFrom(type))
		{
			return false;
		}
		return true;
	}

	@Override
	public void registerNoEntityType(Class<?> noEntityType)
	{
		noEntityTypeExtendables.register(Boolean.TRUE, noEntityType);
	}

	@Override
	public void unregisterNoEntityType(Class<?> noEntityType)
	{
		noEntityTypeExtendables.unregister(Boolean.TRUE, noEntityType);
	}
}
