package de.osthus.ambeth.metadata;

import java.lang.annotation.Annotation;
import java.util.regex.Pattern;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;

public class EmbeddedMember extends Member implements IEmbeddedMember
{
	private static final Pattern pattern = Pattern.compile(Pattern.quote("."));

	public static String[] split(String memberName)
	{
		return pattern.split(memberName);
	}

	public static String buildMemberPathString(Member[] memberPath)
	{
		StringBuilder sb = new StringBuilder();
		for (int a = 0, size = memberPath.length; a < size; a++)
		{
			Member member = memberPath[a];
			if (a > 0)
			{
				sb.append('.');
			}
			sb.append(member.getName());
		}
		return sb.toString();
	}

	public static String[] buildMemberPathToken(Member[] memberPath)
	{
		String[] token = new String[memberPath.length];
		for (int a = memberPath.length; a-- > 0;)
		{
			Member member = memberPath[a];
			token[a] = member.getName();
		}
		return token;
	}

	private final Member childMember;

	private final Member[] memberPath;

	private final String name;

	public EmbeddedMember(String name, Member childMember, Member[] memberPath)
	{
		super(null, null);
		this.name = name;
		this.childMember = childMember;
		this.memberPath = memberPath;
	}

	@Override
	public Member[] getMemberPath()
	{
		return memberPath;
	}

	@Override
	public String getMemberPathString()
	{
		return buildMemberPathString(memberPath);
	}

	@Override
	public Object getNullEquivalentValue()
	{
		return childMember.getNullEquivalentValue();
	}

	@Override
	public String[] getMemberPathToken()
	{
		return buildMemberPathToken(memberPath);
	}

	@Override
	public Member getChildMember()
	{
		return childMember;
	}

	@Override
	public Class<?> getDeclaringType()
	{
		return memberPath[0].getDeclaringType();
	}

	@Override
	public Class<?> getEntityType()
	{
		return memberPath[0].getEntityType();
	}

	@Override
	public Class<?> getElementType()
	{
		return childMember.getElementType();
	}

	@Override
	public Class<?> getRealType()
	{
		return childMember.getRealType();
	}

	@Override
	public boolean isToMany()
	{
		return childMember.isToMany();
	}

	@Override
	public <V extends Annotation> V getAnnotation(Class<V> annotationType)
	{
		return childMember.getAnnotation(annotationType);
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public boolean canRead()
	{
		return childMember.canRead();
	}

	@Override
	public boolean canWrite()
	{
		return childMember.canWrite();
	}

	@Override
	public Object getValue(Object obj)
	{
		Object currentObj = obj;
		for (int a = 0, size = memberPath.length; a < size; a++)
		{
			Member memberPathItem = memberPath[a];
			currentObj = memberPathItem.getValue(currentObj, false);
			if (currentObj == null)
			{
				return null;
			}
		}
		return childMember.getValue(currentObj);
	}

	@Override
	public Object getValue(Object obj, boolean allowNullEquivalentValue)
	{
		Object currentObj = obj;
		for (int a = 0, size = memberPath.length; a < size; a++)
		{
			Member memberPathItem = memberPath[a];
			currentObj = memberPathItem.getValue(currentObj, false);
			if (currentObj == null)
			{
				return null;
			}
		}
		return childMember.getValue(currentObj, allowNullEquivalentValue);
	}

	@Override
	public void setValue(Object obj, Object value)
	{
		Object currentObj = obj;
		for (int a = 0, size = memberPath.length; a < size; a++)
		{
			Member memberPathItem = memberPath[a];
			Object childObj = memberPathItem.getValue(currentObj, false);
			if (childObj == null)
			{
				try
				{
					childObj = memberPathItem.getRealType().newInstance();
				}
				catch (InstantiationException e)
				{
					throw RuntimeExceptionUtil.mask(e);
				}
				catch (IllegalAccessException e)
				{
					throw RuntimeExceptionUtil.mask(e);
				}
				memberPathItem.setValue(currentObj, childObj);
			}
			currentObj = childObj;
		}
		childMember.setValue(currentObj, value);
	}
}
