package de.osthus.ambeth.metadata;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.osthus.ambeth.annotation.Cascade;
import de.osthus.ambeth.annotation.CascadeLoadMode;
import de.osthus.ambeth.bytecode.IBytecodeEnhancer;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.Tuple2KeyEntry;
import de.osthus.ambeth.collections.Tuple2KeyHashMap;
import de.osthus.ambeth.config.IProperties;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.typeinfo.FieldPropertyInfo;
import de.osthus.ambeth.typeinfo.IPropertyInfo;
import de.osthus.ambeth.typeinfo.IPropertyInfoProvider;
import de.osthus.ambeth.typeinfo.MethodPropertyInfo;
import de.osthus.ambeth.typeinfo.TypeInfoItemUtil;
import de.osthus.ambeth.util.ReflectUtil;

public class MemberTypeProvider implements IMemberTypeProvider
{
	public static IPropertyInfo[] buildPropertyPath(Class<?> entityType, String memberName, IPropertyInfoProvider propertyInfoProvider)
	{
		String[] memberPath = memberName.split(Pattern.quote("."));
		Class<?> currType = entityType;
		IPropertyInfo[] propertyPath = new IPropertyInfo[memberPath.length];
		for (int a = 0, size = propertyPath.length; a < size; a++)
		{
			IPropertyInfo property = propertyInfoProvider.getProperty(currType, memberPath[a]);
			if (property == null)
			{
				Field[] fields = ReflectUtil.getDeclaredFieldInHierarchy(currType, memberPath[a]);
				if (fields.length == 0)
				{
					throw new IllegalStateException("Path illegal: " + memberName);
				}
				property = new FieldPropertyInfo(currType, memberPath[a], fields[a]);
			}
			propertyPath[a] = property;
			currType = property.getPropertyType();
		}
		return propertyPath;
	}

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected final Tuple2KeyHashMap<Class<?>, String, Reference<PrimitiveMember>> typeToPrimitiveMemberMap = new Tuple2KeyHashMap<Class<?>, String, Reference<PrimitiveMember>>()
	{
		@Override
		protected void resize(int newCapacity)
		{
			ArrayList<Object[]> removeKeys = new ArrayList<Object[]>();
			for (Tuple2KeyEntry<Class<?>, String, Reference<PrimitiveMember>> entry : this)
			{
				if (entry.getValue().get() == null)
				{
					removeKeys.add(new Object[] { entry.getKey1(), entry.getKey2() });
				}
			}
			for (Object[] removeKey : removeKeys)
			{
				remove((Class<?>) removeKey[0], (String) removeKey[1]);
			}
			if (size() >= threshold)
			{
				super.resize(2 * table.length);
			}
		}
	};

	protected final Tuple2KeyHashMap<Class<?>, String, Reference<Member>> typeToMemberMap = new Tuple2KeyHashMap<Class<?>, String, Reference<Member>>()
	{
		@Override
		protected void resize(int newCapacity)
		{
			ArrayList<Object[]> removeKeys = new ArrayList<Object[]>();
			for (Tuple2KeyEntry<Class<?>, String, Reference<Member>> entry : this)
			{
				if (entry.getValue().get() == null)
				{
					removeKeys.add(new Object[] { entry.getKey1(), entry.getKey2() });
				}
			}
			for (Object[] removeKey : removeKeys)
			{
				remove((Class<?>) removeKey[0], (String) removeKey[1]);
			}
			if (size() >= threshold)
			{
				super.resize(2 * table.length);
			}
		}
	};

	protected final Tuple2KeyHashMap<Class<?>, String, Reference<RelationMember>> typeToRelationMemberMap = new Tuple2KeyHashMap<Class<?>, String, Reference<RelationMember>>()
	{
		@Override
		protected void resize(int newCapacity)
		{
			ArrayList<Object[]> removeKeys = new ArrayList<Object[]>();
			for (Tuple2KeyEntry<Class<?>, String, Reference<RelationMember>> entry : this)
			{
				if (entry.getValue().get() == null)
				{
					removeKeys.add(new Object[] { entry.getKey1(), entry.getKey2() });
				}
			}
			for (Object[] removeKey : removeKeys)
			{
				remove((Class<?>) removeKey[0], (String) removeKey[1]);
			}
			if (size() >= threshold)
			{
				super.resize(2 * table.length);
			}
		}
	};

	protected final Lock writeLock = new ReentrantLock();

	@Autowired
	protected IBytecodeEnhancer bytecodeEnhancer;

	@Autowired
	protected IProperties properties;

	@Autowired
	protected IPropertyInfoProvider propertyInfoProvider;

	@Override
	public RelationMember getRelationMember(Class<?> type, String propertyName)
	{
		return getMemberIntern(type, propertyName, typeToRelationMemberMap, RelationMember.class);
	}

	@Override
	public PrimitiveMember getPrimitiveMember(Class<?> type, String propertyName)
	{
		return getMemberIntern(type, propertyName, typeToPrimitiveMemberMap, PrimitiveMember.class);
	}

	@Override
	public Member getMember(Class<?> type, String propertyName)
	{
		return getMemberIntern(type, propertyName, typeToMemberMap, Member.class);
	}

	@SuppressWarnings("unchecked")
	protected <T extends Member> T getMemberIntern(Class<?> type, String propertyName, Tuple2KeyHashMap<Class<?>, String, Reference<T>> map, Class<?> baseType)
	{
		Reference<T> accessorR = map.get(type, propertyName);
		T member = accessorR != null ? accessorR.get() : null;
		if (member != null)
		{
			return member;
		}
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try
		{
			// concurrent thread might have been faster
			accessorR = map.get(type, propertyName);
			member = accessorR != null ? accessorR.get() : null;
			if (member != null)
			{
				return member;
			}
			member = (T) getMemberIntern(type, propertyName, baseType);
			if (member.getElementType() == null)
			{
				IPropertyInfo[] propertyPath = buildPropertyPath(type, propertyName, propertyInfoProvider);
				IPropertyInfo lastProperty = propertyPath[propertyPath.length - 1];
				Class<?> elementType;
				if (lastProperty instanceof MethodPropertyInfo)
				{
					Method getter = ((MethodPropertyInfo) propertyPath[propertyPath.length - 1]).getGetter();
					elementType = TypeInfoItemUtil.getElementTypeUsingReflection(getter.getReturnType(), getter.getGenericReturnType());
				}
				else
				{
					Field field = ((FieldPropertyInfo) propertyPath[propertyPath.length - 1]).getBackingField();
					elementType = TypeInfoItemUtil.getElementTypeUsingReflection(field.getType(), field.getGenericType());
				}
				member.setElementType(elementType);
			}
			if (member instanceof RelationMember)
			{
				CascadeLoadMode cascadeLoadMode = null;
				Cascade cascadeAnnotation = member.getAnnotation(Cascade.class);
				if (cascadeAnnotation != null)
				{
					cascadeLoadMode = cascadeAnnotation.load();
				}
				if (cascadeLoadMode == null || CascadeLoadMode.DEFAULT.equals(cascadeLoadMode))
				{
					cascadeLoadMode = CascadeLoadMode.valueOf(properties.getString(
							((RelationMember) member).isToMany() ? ServiceConfigurationConstants.ToManyDefaultCascadeLoadMode
									: ServiceConfigurationConstants.ToOneDefaultCascadeLoadMode, CascadeLoadMode.DEFAULT.toString()));
				}
				if (cascadeLoadMode == null || CascadeLoadMode.DEFAULT.equals(cascadeLoadMode))
				{
					cascadeLoadMode = CascadeLoadMode.LAZY;
				}
				((IRelationMemberWrite) member).setCascadeLoadMode(cascadeLoadMode);
			}
			map.put(type, propertyName, new WeakReference<T>(member));
			return member;
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			writeLock.unlock();
		}
	}

	protected Member getMemberIntern(Class<?> type, String propertyName, Class<?> baseType)
	{
		String[] memberNameSplit = propertyName.split(Pattern.quote("."));
		if (memberNameSplit.length > 1)
		{
			Member[] memberPath = new Member[memberNameSplit.length - 1];
			Class<?> currType = type;
			for (int a = 0, size = memberPath.length; a < size; a++)
			{
				memberPath[a] = getMember(currType, memberNameSplit[a]);
				currType = memberPath[a].getRealType();
			}
			if (baseType == RelationMember.class)
			{
				RelationMember lastRelationMember = getRelationMember(currType, memberNameSplit[memberNameSplit.length - 1]);
				return new EmbeddedRelationMember(propertyName, lastRelationMember, memberPath);
			}
			else if (baseType == PrimitiveMember.class)
			{
				PrimitiveMember lastMember = getPrimitiveMember(currType, memberNameSplit[memberNameSplit.length - 1]);
				return new EmbeddedPrimitiveMember(propertyName, lastMember, memberPath);
			}
			throw new IllegalStateException("Must never happen");
		}
		Class<?> enhancedType = getMemberTypeIntern(type, propertyName, baseType);
		if (enhancedType == baseType)
		{
			throw new IllegalStateException("Must never happen. No enhancement for " + baseType + " has been done");
		}
		try
		{
			Constructor<?> constructor = enhancedType.getConstructor(Class.class, IPropertyInfo.class);
			return (Member) constructor.newInstance(type, null);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected Class<?> getMemberTypeIntern(Class<?> targetType, String propertyName, Class<?> baseType)
	{
		String memberTypeName = targetType.getName() + "$" + baseType.getSimpleName() + "$"
				+ propertyName.replaceAll(Pattern.quote("."), Matcher.quoteReplacement("$"));
		if (memberTypeName.startsWith("java."))
		{
			memberTypeName = "ambeth." + memberTypeName;
		}
		if (baseType == RelationMember.class)
		{
			return bytecodeEnhancer.getEnhancedType(baseType, new RelationMemberEnhancementHint(targetType, propertyName));
		}
		return bytecodeEnhancer.getEnhancedType(baseType, new MemberEnhancementHint(targetType, propertyName));
	}
}
