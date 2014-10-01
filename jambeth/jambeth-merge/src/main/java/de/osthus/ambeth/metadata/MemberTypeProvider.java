package de.osthus.ambeth.metadata;

import java.lang.annotation.Annotation;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.osthus.ambeth.annotation.Cascade;
import de.osthus.ambeth.annotation.CascadeLoadMode;
import de.osthus.ambeth.bytecode.IBytecodeEnhancer;
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
import de.osthus.ambeth.util.ReflectUtil;

public class MemberTypeProvider implements IMemberTypeProvider, IIntermediateMemberTypeProvider
{
	public class TypeAndStringWeakMap<T> extends Tuple2KeyHashMap<Class<?>, String, Reference<T>>
	{
		@Override
		protected void transfer(Tuple2KeyEntry<Class<?>, String, Reference<T>>[] newTable)
		{
			final int newCapacityMinus1 = newTable.length - 1;
			final Tuple2KeyEntry<Class<?>, String, Reference<T>>[] table = this.table;

			for (int a = table.length; a-- > 0;)
			{
				Tuple2KeyEntry<Class<?>, String, Reference<T>> entry = table[a], next;
				while (entry != null)
				{
					next = entry.getNextEntry();

					// only handle this entry if it has still a valid value
					if (entry.getValue().get() != null)
					{
						int i = entry.getHash() & newCapacityMinus1;
						entry.setNextEntry(newTable[i]);
						newTable[i] = entry;
					}
					entry = next;
				}
			}
		}
	}

	public static IPropertyInfo[] buildPropertyPath(Class<?> entityType, String memberName, IPropertyInfoProvider propertyInfoProvider)
	{
		String[] memberPath = EmbeddedMember.split(memberName);
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

	protected final TypeAndStringWeakMap<PrimitiveMember> typeToPrimitiveMemberMap = new TypeAndStringWeakMap<PrimitiveMember>();

	protected final TypeAndStringWeakMap<Member> typeToMemberMap = new TypeAndStringWeakMap<Member>();

	protected final TypeAndStringWeakMap<RelationMember> typeToRelationMemberMap = new TypeAndStringWeakMap<RelationMember>();

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
	protected <T extends Member> T getMemberIntern(Class<?> type, String propertyName, TypeAndStringWeakMap<T> map, Class<?> baseType)
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

	@Override
	public IntermediatePrimitiveMember getIntermediatePrimitiveMember(Class<?> entityType, String propertyName)
	{
		String[] memberNamePath = EmbeddedMember.split(propertyName);
		Class<?> currDeclaringType = entityType;
		Member[] members = new Member[memberNamePath.length];
		for (int a = 0, size = memberNamePath.length; a < size; a++)
		{
			IPropertyInfo property = propertyInfoProvider.getProperty(currDeclaringType, memberNamePath[a]);
			if (property == null)
			{
				return null;
			}
			members[a] = new IntermediatePrimitiveMember(currDeclaringType, entityType, property.getPropertyType(), property.getElementType(),
					property.getName(), property.getAnnotations());
			currDeclaringType = property.getPropertyType();
		}
		if (members.length > 1)
		{
			Member[] memberPath = new Member[members.length - 1];
			System.arraycopy(members, 0, memberPath, 0, memberPath.length);
			PrimitiveMember lastMember = (PrimitiveMember) members[memberPath.length];
			return new IntermediateEmbeddedPrimitiveMember(entityType, lastMember.getRealType(), lastMember.getElementType(), propertyName, memberPath,
					lastMember);
		}
		return (IntermediatePrimitiveMember) members[0];
	}

	@Override
	public IntermediateRelationMember getIntermediateRelationMember(Class<?> entityType, String propertyName)
	{
		String[] memberNamePath = EmbeddedMember.split(propertyName);
		Class<?> currDeclaringType = entityType;
		Member[] members = new Member[memberNamePath.length];
		Annotation[] lastMemberAnnotations = null;
		for (int a = 0, size = memberNamePath.length; a < size; a++)
		{
			IPropertyInfo property = propertyInfoProvider.getProperty(currDeclaringType, memberNamePath[a]);
			if (property == null)
			{
				return null;
			}
			members[a] = new IntermediateRelationMember(currDeclaringType, entityType, property.getPropertyType(), property.getElementType(),
					property.getName(), property.getAnnotations());
			currDeclaringType = property.getPropertyType();
		}
		if (members.length > 1)
		{
			Member[] memberPath = new Member[members.length - 1];
			System.arraycopy(members, 0, memberPath, 0, memberPath.length);
			Member lastMember = members[memberPath.length];
			return new IntermediateEmbeddedRelationMember(entityType, lastMember.getRealType(), lastMember.getElementType(), propertyName, memberPath,
					lastMember);
		}
		return (IntermediateRelationMember) members[0];
	}
}
