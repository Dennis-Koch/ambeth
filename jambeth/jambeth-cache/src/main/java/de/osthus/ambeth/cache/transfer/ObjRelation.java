package de.osthus.ambeth.cache.transfer;

import java.util.Arrays;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import de.osthus.ambeth.cache.model.IObjRelation;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.util.EqualsUtil;
import de.osthus.ambeth.util.IPrintable;
import de.osthus.ambeth.util.StringBuilderUtil;

@XmlRootElement(name = "ObjRelation", namespace = "http://schemas.osthus.de/Ambeth")
@XmlAccessorType(XmlAccessType.FIELD)
public class ObjRelation implements IObjRelation, IPrintable
{
	@XmlElement(required = true)
	protected String memberName;

	@XmlElement(required = true)
	protected Class<?> realType;

	@XmlElement(required = true)
	protected Object[] ids;

	@XmlElement(required = true)
	protected Object version;

	@XmlElement(required = true)
	protected byte[] idIndices;

	protected transient IObjRef[] objRefs;

	public ObjRelation()
	{
	}

	public ObjRelation(IObjRef[] objRefs, String memberName)
	{
		setObjRefs(objRefs);
		setMemberName(memberName);
	}

	@Override
	public Class<?> getRealType()
	{
		return realType;
	}

	public void setRealType(Class<?> realType)
	{
		this.realType = realType;
	}

	@Override
	public String getMemberName()
	{
		return memberName;
	}

	public void setMemberName(String memberName)
	{
		this.memberName = memberName;
	}

	public Object[] getIds()
	{
		return ids;
	}

	public void setIds(Object[] ids)
	{
		this.ids = ids;
	}

	@Override
	public Object getVersion()
	{
		return version;
	}

	public void setVersion(Object version)
	{
		this.version = version;
	}

	public byte[] getIdIndices()
	{
		return idIndices;
	}

	public void setIdIndices(byte[] idIndices)
	{
		this.idIndices = idIndices;
	}

	@Override
	public IObjRef[] getObjRefs()
	{
		if (objRefs == null)
		{
			Class<?> realType = this.realType;
			Object version = this.version;
			Object[] ids = this.ids;
			byte[] idIndices = this.idIndices;
			IObjRef[] objRefs = new IObjRef[ids.length];
			for (int a = ids.length; a-- > 0;)
			{
				objRefs[a] = new ObjRef(realType, idIndices[a], ids[a], version);
			}
			this.objRefs = objRefs;
		}
		return objRefs;
	}

	public void setObjRefs(IObjRef[] objRefs)
	{
		this.objRefs = objRefs;
		if (objRefs == null)
		{
			ids = null;
			idIndices = null;
			version = null;
			realType = null;
		}
		else
		{
			int length = objRefs.length;
			ids = new Object[length];
			idIndices = new byte[length];
			for (int a = length; a-- > 0;)
			{
				IObjRef objRef = objRefs[a];
				ids[a] = objRef.getId();
				idIndices[a] = objRef.getIdNameIndex();
			}
			IObjRef firstObjRef = objRefs[0];
			version = firstObjRef.getVersion();
			realType = firstObjRef.getRealType();
		}
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		if (!(obj instanceof ObjRelation))
		{
			return false;
		}
		ObjRelation other = (ObjRelation) obj;
		return EqualsUtil.equals(getRealType(), other.getRealType()) && EqualsUtil.equals(getMemberName(), other.getMemberName())
				&& Arrays.equals(getObjRefs(), other.getObjRefs());
	}

	@Override
	public int hashCode()
	{
		return getRealType().hashCode() ^ getMemberName().hashCode() ^ Arrays.hashCode(getObjRefs());
	}

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
		sb.append("ObjRel: memberName=").append(memberName).append(", ref=");
		StringBuilderUtil.appendPrintable(sb, objRefs);
	}
}
