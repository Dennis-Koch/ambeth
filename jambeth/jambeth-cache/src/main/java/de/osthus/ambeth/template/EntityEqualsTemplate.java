package de.osthus.ambeth.template;

import de.osthus.ambeth.proxy.IEntityEquals;
import de.osthus.ambeth.util.IPrintable;
import de.osthus.ambeth.util.StringBuilderUtil;

public class EntityEqualsTemplate
{
	public boolean equals(IEntityEquals left, Object right)
	{
		if (right == left)
		{
			return true;
		}
		if (!(right instanceof IEntityEquals))
		{
			return false;
		}
		Object id = left.get__Id();
		if (id == null)
		{
			// Null id can never be equal with something other than itself
			return false;
		}
		IEntityEquals other = (IEntityEquals) right;
		return id.equals(other.get__Id()) && left.get__BaseType().equals(other.get__BaseType());
	}

	public int hashCode(IEntityEquals left)
	{
		Object id = left.get__Id();
		if (id == null)
		{
			return System.identityHashCode(left);
		}
		return left.get__BaseType().hashCode() ^ id.hashCode();
	}

	public String toString(IEntityEquals left, IPrintable printable)
	{
		StringBuilder sb = new StringBuilder();
		printable.toString(sb);
		return sb.toString();
	}

	public void toString(IEntityEquals left, StringBuilder sb)
	{
		sb.append(left.get__BaseType().getName()).append('-');
		StringBuilderUtil.appendPrintable(sb, left.get__Id());
	}
}
