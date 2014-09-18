package de.osthus.ambeth.compositeid;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.metadata.Member;
import de.osthus.ambeth.metadata.PrimitiveMember;
import de.osthus.ambeth.repackaged.com.esotericsoftware.reflectasm.FieldAccess;

public class CompositeIdMember extends PrimitiveMember
{
	public static String filterEmbeddedFieldName(String fieldName)
	{
		return fieldName.replaceAll(Pattern.quote("."), Matcher.quoteReplacement("__"));
	}

	protected final Class<?> declaringType;

	protected final Class<?> realType;

	protected final PrimitiveMember[] members;

	protected final Constructor<?> realTypeConstructorAccess;

	protected final FieldAccess realTypeFieldAccess;

	protected final int[] fieldIndexOfMembers;

	protected final String name;

	protected boolean technicalMember;

	public CompositeIdMember(Class<?> declaringType, Class<?> realType, String name, PrimitiveMember[] members)
	{
		super(declaringType, null);
		this.declaringType = declaringType;
		this.realType = realType;
		this.name = name;
		this.members = members;
		realTypeFieldAccess = FieldAccess.get(realType);
		fieldIndexOfMembers = new int[members.length];
		Class<?>[] paramTypes = new Class<?>[members.length];
		for (int a = 0, size = members.length; a < size; a++)
		{
			Member member = members[a];
			fieldIndexOfMembers[a] = realTypeFieldAccess.getIndex(CompositeIdMember.filterEmbeddedFieldName(member.getName()));
			paramTypes[a] = member.getRealType();
		}
		try
		{
			realTypeConstructorAccess = realType.getConstructor(paramTypes);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public boolean isTechnicalMember()
	{
		return technicalMember;
	}

	public void setTechnicalMember(boolean technicalMember)
	{
		this.technicalMember = technicalMember;
	}

	@Override
	public Class<?> getRealType()
	{
		return realType;
	}

	@Override
	public Class<?> getElementType()
	{
		return realType;
	}

	@Override
	public Class<?> getDeclaringType()
	{
		return declaringType;
	}

	@Override
	public boolean canRead()
	{
		return true;
	}

	@Override
	public boolean canWrite()
	{
		return true;
	}

	public Object getDecompositedValue(Object compositeId, int compositeMemberIndex)
	{
		int fieldIndexOfMember = fieldIndexOfMembers[compositeMemberIndex];
		return realTypeFieldAccess.get(compositeId, fieldIndexOfMember);
	}

	public Object getDecompositedValueOfObject(Object obj, int compositeMemberIndex)
	{
		return members[compositeMemberIndex].getValue(obj, false);
	}

	public Constructor<?> getRealTypeConstructorAccess()
	{
		return realTypeConstructorAccess;
	}

	public FieldAccess getRealTypeFieldAccess()
	{
		return realTypeFieldAccess;
	}

	public int[] getFieldIndexOfMembers()
	{
		return fieldIndexOfMembers;
	}

	public PrimitiveMember[] getMembers()
	{
		return members;
	}

	@Override
	public Object getNullEquivalentValue()
	{
		return null;
	}

	@Override
	public Object getValue(Object obj)
	{
		return getValue(obj, true);
	}

	@Override
	public Object getValue(Object obj, boolean allowNullEquivalentValue)
	{
		Member[] members = this.members;
		Object[] args = new Object[members.length];
		for (int a = members.length; a-- > 0;)
		{
			Object memberValue = members[a].getValue(obj, allowNullEquivalentValue);
			if (memberValue == null)
			{
				return null;
			}
			args[a] = memberValue;
		}
		try
		{
			return realTypeConstructorAccess.newInstance(args);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public void setValue(Object obj, Object compositeId)
	{
		Member[] members = this.members;
		FieldAccess realTypeFieldAccess = this.realTypeFieldAccess;
		int[] fieldIndexOfMembers = this.fieldIndexOfMembers;
		if (compositeId != null)
		{
			for (int a = members.length; a-- > 0;)
			{
				int fieldIndexOfMember = fieldIndexOfMembers[a];
				Object memberValue = realTypeFieldAccess.get(compositeId, fieldIndexOfMember);
				members[a].setValue(obj, memberValue);
			}
		}
		else
		{
			for (int a = members.length; a-- > 0;)
			{
				members[a].setValue(obj, null);
			}
		}
	}

	@Override
	public <V extends Annotation> V getAnnotation(Class<V> annotationType)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public String toString()
	{
		return getName();
	}
}
