package de.osthus.ambeth.cache;

import de.osthus.ambeth.util.IPrintable;
import de.osthus.ambeth.util.StringBuilderUtil;

public abstract class AbstractCacheValue implements IPrintable
{
	public abstract Object getId();

	public abstract void setId(Object id);

	public abstract Object getVersion();

	public abstract void setVersion(Object version);

	public abstract Class<?> getEntityType();

	public abstract Object getPrimitive(int primitiveIndex);

	public abstract Object[] getPrimitives();

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}

	@Override
	public void toString(StringBuilder sb)
	{
		sb.append("EntityType=").append(getEntityType().getName()).append(" Id='");
		StringBuilderUtil.appendPrintable(sb, getId());
		sb.append("' Version='").append(getVersion()).append('\'');
	}
}
