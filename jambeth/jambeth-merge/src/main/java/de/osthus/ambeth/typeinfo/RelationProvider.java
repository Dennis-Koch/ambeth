package de.osthus.ambeth.typeinfo;

import java.util.Arrays;

import javax.persistence.Embeddable;

import de.osthus.ambeth.collections.SmartCopySet;
import de.osthus.ambeth.ioc.extendable.ClassExtendableContainer;
import de.osthus.ambeth.util.IImmutableType;
import de.osthus.ambeth.util.ImmutableTypeSet;

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
