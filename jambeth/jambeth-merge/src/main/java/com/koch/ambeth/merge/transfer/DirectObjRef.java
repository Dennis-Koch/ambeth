package com.koch.ambeth.merge.transfer;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.koch.ambeth.merge.model.IDirectObjRef;
import com.koch.ambeth.service.merge.model.IObjRef;

@XmlRootElement(name = "DirectObjRef", namespace = "http://schema.kochdev.com/Ambeth")
@XmlAccessorType(XmlAccessType.FIELD)
public class DirectObjRef extends ObjRef implements IDirectObjRef
{
	protected transient Object direct;

	@XmlElement(required = true)
	protected int createContainerIndex = -1;

	public DirectObjRef()
	{
		// Intended blank
	}

	public DirectObjRef(Class<?> realType, Object direct)
	{
		this.realType = realType;
		this.direct = direct;
	}

	@Override
	public Object getDirect()
	{
		return direct;
	}

	@Override
	public void setDirect(Object direct)
	{
		this.direct = direct;
	}

	@Override
	public void setId(Object id)
	{
		super.setId(id);
	}

	@Override
	public int getCreateContainerIndex()
	{
		return createContainerIndex;
	}

	@Override
	public void setCreateContainerIndex(int createContainerIndex)
	{
		this.createContainerIndex = createContainerIndex;
	}

	@Override
	public boolean equals(IObjRef obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (direct != null)
		{
			if (!(obj instanceof IDirectObjRef))
			{
				return false;
			}
			// Identity - not equals - intentionally here!
			return direct == ((IDirectObjRef) obj).getDirect();
		}
		return id.equals(obj.getId()) && realType.equals(obj.getRealType());
	}

	@Override
	public int hashCode()
	{
		if (direct != null)
		{
			return direct.hashCode();
		}
		return super.hashCode();
	}

	@Override
	public String toString()
	{
		if (direct != null)
		{
			return "ObjRef (new) type=" + getRealType().getName();
		}
		return super.toString();
	}

}
