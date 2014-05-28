package de.osthus.ambeth.template;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.typeinfo.ITypeInfoItem;
import de.osthus.ambeth.util.StringBuilderUtil;

public class CompositeIdTemplate
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	public boolean equalsCompositeId(ITypeInfoItem[] members, Object left, Object right)
	{
		if (left == null || right == null)
		{
			return false;
		}
		if (left == right)
		{
			return true;
		}
		if (!left.getClass().equals(right.getClass()))
		{
			return false;
		}
		for (ITypeInfoItem member : members)
		{
			Object leftValue = member.getValue(left, false);
			Object rightValue = member.getValue(right, false);
			if (leftValue == null || rightValue == null)
			{
				return false;
			}
			if (!leftValue.equals(rightValue))
			{
				return false;
			}
		}
		return true;
	}

	public int hashCodeCompositeId(ITypeInfoItem[] members, Object compositeId)
	{
		int hash = compositeId.getClass().hashCode();
		for (ITypeInfoItem member : members)
		{
			Object value = member.getValue(compositeId, false);
			if (value != null)
			{
				hash ^= value.hashCode();
			}
		}
		return hash;
	}

	public String toStringCompositeId(ITypeInfoItem[] members, Object compositeId)
	{
		StringBuilder sb = new StringBuilder();
		toStringSbCompositeId(members, compositeId, sb);
		return sb.toString();
	}

	public void toStringSbCompositeId(ITypeInfoItem[] members, Object compositeId, StringBuilder sb)
	{
		// order does matter here
		for (int a = 0, size = members.length; a < size; a++)
		{
			Object value = members[a].getValue(compositeId);
			if (a > 0)
			{
				sb.append('#');
			}
			if (value != null)
			{
				StringBuilderUtil.appendPrintable(sb, value);
			}
			else
			{
				sb.append("<null>");
			}
		}
	}
}
