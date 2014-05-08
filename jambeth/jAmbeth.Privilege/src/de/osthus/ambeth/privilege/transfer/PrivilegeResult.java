package de.osthus.ambeth.privilege.transfer;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.model.ISecurityScope;
import de.osthus.ambeth.privilege.model.IPrivilegeResult;
import de.osthus.ambeth.privilege.model.PrivilegeEnum;

@XmlRootElement(name = "PrivilegeResult", namespace = "http://schemas.osthus.de/Ambeth")
@XmlAccessorType(XmlAccessType.FIELD)
public class PrivilegeResult implements IPrivilegeResult
{
	@XmlElement(required = true)
	protected IObjRef reference;

	@XmlElement(required = true)
	protected ISecurityScope securityScope;

	@XmlElement(required = true)
	protected PrivilegeEnum[] privileges;

	@Override
	public IObjRef getReference()
	{
		return reference;
	}

	public void setReference(IObjRef reference)
	{
		this.reference = reference;
	}

	@Override
	public ISecurityScope getSecurityScope()
	{
		return securityScope;
	}

	public void setSecurityScope(ISecurityScope securityScope)
	{
		this.securityScope = securityScope;
	}

	@Override
	public PrivilegeEnum[] getPrivileges()
	{
		return privileges;
	}

	public void setPrivileges(PrivilegeEnum[] privileges)
	{
		this.privileges = privileges;
	}
}
