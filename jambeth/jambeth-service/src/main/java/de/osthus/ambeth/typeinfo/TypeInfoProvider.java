package de.osthus.ambeth.typeinfo;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.locks.Lock;
import java.util.regex.Pattern;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.SmartCopyMap;
import de.osthus.ambeth.config.IProperties;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.repackaged.com.esotericsoftware.reflectasm.FieldAccess;
import de.osthus.ambeth.util.NamedItemComparator;
import de.osthus.ambeth.util.ParamChecker;

public class TypeInfoProvider extends SmartCopyMap<Class<?>, TypeInfo> implements ITypeInfoProvider, IInitializingBean
{
	private static final NamedItemComparator typeInfoItemComparator = new NamedItemComparator();

	private static final Pattern memberPathSplitPattern = Pattern.compile("\\.");

	@LogInstance
	private ILogger log;

	protected IProperties properties;

	protected IPropertyInfoProvider propertyInfoProvider;

	protected final ThreadLocal<HashMap<Class<?>, TypeInfo>> tempTypeInfoMapTL = new ThreadLocal<HashMap<Class<?>, TypeInfo>>();

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(properties, "Properties");
		ParamChecker.assertNotNull(propertyInfoProvider, "propertyInfoProvider");
	}

	public void setProperties(IProperties properties)
	{
		this.properties = properties;
	}

	public void setPropertyInfoProvider(IPropertyInfoProvider propertyInfoProvider)
	{
		this.propertyInfoProvider = propertyInfoProvider;
	}

	@Override
	public ITypeInfoProvider getInstance()
	{
		return this;
	}

	@Override
	public ITypeInfoItem getHierarchicMember(Class<?> entityType, String hierarchicMemberName)
	{
		String[] memberPath = memberPathSplitPattern.split(hierarchicMemberName);
		ITypeInfoItem[] members = new ITypeInfoItem[memberPath.length];
		Class<?> currentType = entityType;
		for (int a = 0, size = memberPath.length; a < size; a++)
		{
			String memberName = memberPath[a];
			ITypeInfo typeInfo = getTypeInfo(currentType);
			ITypeInfoItem member = typeInfo.getMemberByName(memberName);
			if (member == null)
			{
				String memberNameLower = memberName.toLowerCase();
				ITypeInfoItem[] allMembers = typeInfo.getMembers();
				for (int b = allMembers.length; b-- > 0;)
				{
					if (allMembers[b].getName().toLowerCase().equals(memberNameLower))
					{
						member = allMembers[b];
						break;
					}
				}
				if (member == null)
				{
					return null;
				}
			}
			currentType = member.getRealType();
			members[a] = member;
		}
		if (members.length == 1)
		{
			return members[0];
		}
		else if (members.length > 1)
		{
			ITypeInfoItem[] memberForParentPath = new ITypeInfoItem[members.length - 1];
			System.arraycopy(members, 0, memberForParentPath, 0, memberForParentPath.length);
			ITypeInfoItem lastMember = members[members.length - 1];
			return new EmbeddedTypeInfoItem(hierarchicMemberName, lastMember, memberForParentPath);
		}
		else
		{
			throw new IllegalArgumentException("Must never happen");
		}
	}

	@Override
	public ITypeInfoItem getMember(Class<?> entityType, IPropertyInfo propertyInfo)
	{
		return new PropertyInfoItem(propertyInfo);
	}

	@Override
	public ITypeInfoItem getMember(Field field)
	{
		ITypeInfoItem rii = null;
		if (!Modifier.isPrivate(field.getModifiers()))
		{
			ITypeInfo typeInfo = getTypeInfo(field.getDeclaringClass());
			FieldAccess fieldAccess = ((TypeInfo) typeInfo).getFieldAccess();
			if (fieldAccess != null)
			{
				rii = new FieldInfoItemASM(field, fieldAccess);
			}
		}
		if (rii == null)
		{
			rii = new FieldInfoItem(field);
		}
		return rii;
	}

	@Override
	public ITypeInfoItem getMember(String propertyName, Field field)
	{
		ITypeInfoItem rii = null;
		if (!Modifier.isPrivate(field.getModifiers()))
		{
			ITypeInfo typeInfo = getTypeInfo(field.getDeclaringClass());
			FieldAccess fieldAccess = ((TypeInfo) typeInfo).getFieldAccess();
			if (fieldAccess != null)
			{
				rii = new FieldInfoItemASM(field, propertyName, fieldAccess);
			}
		}
		if (rii == null)
		{
			rii = new FieldInfoItem(field, propertyName);
		}
		return rii;
	}

	@Override
	public ITypeInfo getTypeInfo(Class<?> type)
	{
		TypeInfo typeInfo = get(type);
		if (typeInfo != null)
		{
			return typeInfo;
		}
		boolean tempTypeInfoMapCreated = false;
		Lock writeLock = getWriteLock();
		writeLock.lock();
		try
		{
			typeInfo = get(type);
			if (typeInfo != null)
			{
				// Concurrent thread might have been faster
				return typeInfo;
			}
			HashMap<Class<?>, TypeInfo> tempTypeInfoMap = tempTypeInfoMapTL.get();
			if (tempTypeInfoMap != null)
			{
				typeInfo = tempTypeInfoMap.get(type);
				if (typeInfo != null)
				{
					// Current thread is currently created cascaded TypeInfo instances
					return typeInfo;
				}
			}
			else
			{
				tempTypeInfoMap = new HashMap<Class<?>, TypeInfo>();
				tempTypeInfoMapTL.set(tempTypeInfoMap);
				tempTypeInfoMapCreated = true;
			}
			ArrayList<Field> allFields = new ArrayList<Field>(0);
			ArrayList<IPropertyInfo> allProperties = new ArrayList<IPropertyInfo>();

			allFields.addAll(Arrays.asList(type.getFields()));
			for (int a = allFields.size(); a-- > 0;)
			{
				Field field = allFields.get(a);
				if ((Modifier.STATIC & field.getModifiers()) != 0)
				{
					allFields.remove(a);
				}
			}
			allProperties.addAll(Arrays.asList(propertyInfoProvider.getProperties(type)));

			ArrayList<ITypeInfoItem> memberList = new ArrayList<ITypeInfoItem>(allProperties.size() + allFields.size());

			for (int a = allFields.size(); a-- > 0;)
			{
				Field field = allFields.get(a);

				String fieldNameLower = field.getName().toLowerCase();
				for (IPropertyInfo property : allProperties)
				{
					if (property.getPropertyType().equals(field.getType()) && fieldNameLower.equals(property.getName().toLowerCase()))
					{
						allFields.remove(a);
						break;
					}
				}
			}
			typeInfo = new TypeInfo(type);
			tempTypeInfoMap.put(type, typeInfo);

			for (IPropertyInfo property : allProperties)
			{
				if (property.getAnnotation(Transient.class) != null)
				{
					continue; // Can not handle non datamember properties
				}
				memberList.add(getMember(type, property));
			}
			for (Field field : allFields)
			{
				if (field.getAnnotation(Transient.class) != null)
				{
					continue; // Can not handle non datamember fields
				}
				memberList.add(getMember(field));
			}

			Collections.sort(memberList, typeInfoItemComparator);

			ITypeInfoItem[] members = memberList.toArray(ITypeInfoItem.class);
			typeInfo.postInit(members);

			putAll(tempTypeInfoMap);
			return typeInfo;
		}
		finally
		{
			writeLock.unlock();
			if (tempTypeInfoMapCreated)
			{
				tempTypeInfoMapTL.remove();
			}
		}
	}
}
