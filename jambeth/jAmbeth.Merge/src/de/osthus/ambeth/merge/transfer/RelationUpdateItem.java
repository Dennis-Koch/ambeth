package de.osthus.ambeth.merge.transfer;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.model.IRelationUpdateItem;
import de.osthus.ambeth.util.Arrays;
import de.osthus.ambeth.util.IPrintable;

@XmlRootElement(name = "RelationUpdateItem", namespace = "http://schemas.osthus.de/Ambeth")
@XmlAccessorType(XmlAccessType.FIELD)
public class RelationUpdateItem implements IRelationUpdateItem, IPrintable
{
	@XmlElement(required = true)
	protected String memberName;

	@XmlElement(required = false)
	protected IObjRef[] addedORIs;

	@XmlElement(required = false)
	protected IObjRef[] removedORIs;

	@Override
	public String getMemberName()
	{
		return memberName;
	}

	public void setMemberName(String memberName)
	{
		this.memberName = memberName;
	}

	@Override
	public IObjRef[] getAddedORIs()
	{
		return addedORIs;
	}

	public void setAddedORIs(IObjRef[] addedORIs)
	{
		this.addedORIs = addedORIs;
	}

	@Override
	public IObjRef[] getRemovedORIs()
	{
		return removedORIs;
	}

	public void setRemovedORIs(IObjRef[] removedORIs)
	{
		this.removedORIs = removedORIs;
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
		sb.append("RUI: MemberName=").append(getMemberName());
		IObjRef[] addedORIs = getAddedORIs();
		IObjRef[] removedORIs = getRemovedORIs();
		if (addedORIs != null && addedORIs.length > 0)
		{
			sb.append(" AddedORIs=");
			Arrays.toString(sb, addedORIs);
		}
		if (removedORIs != null && removedORIs.length > 0)
		{
			sb.append(" RemovedORIs=");
			Arrays.toString(sb, removedORIs);
		}
	}
}
