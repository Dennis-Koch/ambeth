package de.osthus.ambeth.template;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.util.StringBuilderUtil;

public class ObjRefTemplate
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	public boolean objRefEquals(IObjRef objRef, Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (!(obj instanceof IObjRef))
		{
			return false;
		}
		IObjRef other = (IObjRef) obj;
		if (objRef.getIdNameIndex() != other.getIdNameIndex() || !objRef.getRealType().equals(other.getRealType()))
		{
			return false;
		}
		Object id = objRef.getId();
		Object otherId = other.getId();
		if (id == null || otherId == null)
		{
			return false;
		}
		if (!id.getClass().isArray() || !otherId.getClass().isArray())
		{
			return id.equals(otherId);
		}
		Object[] idArray = (Object[]) id;
		Object[] otherIdArray = (Object[]) otherId;
		if (idArray.length != otherIdArray.length)
		{
			return false;
		}
		for (int a = idArray.length; a-- > 0;)
		{
			if (!idArray[a].equals(otherIdArray[a]))
			{
				return false;
			}
		}
		return true;
	}

	public int objRefHashCode(IObjRef objRef)
	{
		return objRef.getId().hashCode() ^ objRef.getRealType().hashCode() ^ objRef.getIdNameIndex();
	}

	public void objRefToString(IObjRef objRef, StringBuilder sb)
	{
		sb.append("ObjRef ");
		byte idIndex = objRef.getIdNameIndex();
		if (idIndex == ObjRef.PRIMARY_KEY_INDEX)
		{
			sb.append("PK=");
		}
		else
		{
			sb.append("AK").append(idIndex).append('=');
		}
		StringBuilderUtil.appendPrintable(sb, objRef.getId());
		sb.append(" version=").append(objRef.getVersion()).append(" type=").append(objRef.getRealType().getName());
	}
}
