package de.osthus.ambeth.merge.transfer;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.util.IPrintable;
import de.osthus.ambeth.util.StringBuilderUtil;

@XmlRootElement(name = "ObjRef", namespace = "http://schemas.osthus.de/Ambeth")
@XmlAccessorType(XmlAccessType.FIELD)
public class ObjRef implements IObjRef, IPrintable
{
	public static final IObjRef[] EMPTY_ARRAY = new IObjRef[0];

	public static final IObjRef[][] EMPTY_ARRAY_ARRAY = new IObjRef[0][];

	public static final byte PRIMARY_KEY_INDEX = -1;

	public static final byte UNDEFINED_KEY_INDEX = Byte.MIN_VALUE;

	@XmlElement(required = true)
	protected byte idNameIndex = ObjRef.PRIMARY_KEY_INDEX;

	@XmlElement(required = true)
	protected Object id;

	@XmlElement
	protected Object version;

	@XmlElement(required = true)
	protected Class<?> realType;

	public ObjRef()
	{
		// Intended blank
	}

	public ObjRef(Class<?> realType, Object id, Object version)
	{
		this(realType, ObjRef.PRIMARY_KEY_INDEX, id, version);
	}

	public ObjRef(Class<?> realType, byte idNameIndex, Object id, Object version)
	{
		setRealType(realType);
		setIdNameIndex(idNameIndex);
		setId(id);
		setVersion(version);
	}

	public void init(Class<?> entityType, byte idNameIndex, Object id, Object version)
	{
		setRealType(entityType);
		setIdNameIndex(idNameIndex);
		setId(id);
		setVersion(version);
	}

	@Override
	public Object getId()
	{
		return id;
	}

	@Override
	public void setId(Object id)
	{
		this.id = id;
	}

	@Override
	public byte getIdNameIndex()
	{
		return idNameIndex;
	}

	@Override
	public void setIdNameIndex(byte idNameIndex)
	{
		this.idNameIndex = idNameIndex;
	}

	@Override
	public Object getVersion()
	{
		return version;
	}

	@Override
	public void setVersion(Object version)
	{
		this.version = version;
	}

	@Override
	public Class<?> getRealType()
	{
		return realType;
	}

	@Override
	public void setRealType(Class<?> realType)
	{
		this.realType = realType;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (!(obj instanceof IObjRef))
		{
			return false;
		}
		return this.equals((IObjRef) obj);
	}

	public boolean equals(IObjRef obj)
	{
		if (obj == null)
		{
			return false;
		}
		if (getIdNameIndex() != obj.getIdNameIndex() || !getRealType().equals(obj.getRealType()))
		{
			return false;
		}
		Object id = getId();
		Object otherId = obj.getId();
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

	@Override
	public int hashCode()
	{
		return getId().hashCode() ^ getRealType().hashCode();
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}

	protected String getClassName()
	{
		return "ORI";
	}

	@Override
	public void toString(StringBuilder sb)
	{
		sb.append(getClassName()).append(" id=").append(idNameIndex).append(",");
		StringBuilderUtil.appendPrintable(sb, getId());
		sb.append(" version=").append(getVersion()).append(" type=").append(getRealType().getName());
	}
}
