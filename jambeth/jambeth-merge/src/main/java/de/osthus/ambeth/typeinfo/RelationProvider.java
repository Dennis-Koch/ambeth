package de.osthus.ambeth.typeinfo;

import java.util.Arrays;

import javax.persistence.Embeddable;
import javax.xml.datatype.XMLGregorianCalendar;

import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.util.IImmutableType;
import de.osthus.ambeth.util.ImmutableTypeSet;

public class RelationProvider implements IRelationProvider
{
	protected final HashSet<Class<?>> primitiveTypes = new HashSet<Class<?>>();

	public RelationProvider()
	{
		ImmutableTypeSet.addImmutableTypesTo(primitiveTypes);

		primitiveTypes.addAll(Arrays.asList(new Class<?>[] { Object.class, java.util.Date.class, java.sql.Date.class, java.sql.Timestamp.class,
				java.util.Calendar.class, java.lang.Integer.class, java.lang.Long.class, java.lang.Double.class, java.lang.Float.class, java.lang.Short.class,
				java.lang.Character.class, java.lang.Byte.class }));
		primitiveTypes.add(XMLGregorianCalendar.class);
	}

	@Override
	public boolean isEntityType(Class<?> type)
	{
		if (type == null || type.isPrimitive() || type.isEnum() || primitiveTypes.contains(type))
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
	public String getCreatedByMemberName()
	{
		return null;
	}

	@Override
	public String getCreatedOnMemberName()
	{
		return null;
	}

	@Override
	public String getIdMemberName()
	{
		return null;
	}

	@Override
	public String getUpdatedByMemberName()
	{
		return null;
	}

	@Override
	public String getUpdatedOnMemberName()
	{
		return null;
	}

	@Override
	public String getVersionMemberName()
	{
		return null;
	}
}
