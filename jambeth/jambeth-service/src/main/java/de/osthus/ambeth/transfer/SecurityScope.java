package de.osthus.ambeth.transfer;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import de.osthus.ambeth.model.ISecurityScope;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class SecurityScope implements ISecurityScope
{
	@XmlElement(required = true)
	protected String name;

	public void setName(String name)
	{
		this.name = name;
	}

	@Override
	public String getName()
	{
		return name;
	}
}
