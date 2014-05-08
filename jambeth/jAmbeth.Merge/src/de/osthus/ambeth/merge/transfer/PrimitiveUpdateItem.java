package de.osthus.ambeth.merge.transfer;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import de.osthus.ambeth.merge.model.IPrimitiveUpdateItem;
import de.osthus.ambeth.util.IPrintable;

@XmlRootElement(name = "PrimitiveUpdateItem", namespace = "http://schemas.osthus.de/Ambeth")
@XmlAccessorType(XmlAccessType.FIELD)
public class PrimitiveUpdateItem implements IPrimitiveUpdateItem, IPrintable
{
	@XmlElement(required = true)
	public Object newValue;

	@XmlElement(required = true)
	public String memberName;

	@Override
	public Object getNewValue()
	{
		return newValue;
	}

	public void setNewValue(Object newValue)
	{
		this.newValue = newValue;
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
		sb.append("PUI: MemberName=").append(getMemberName()).append(" NewValue='").append(getNewValue()).append('\'');
	}
}
