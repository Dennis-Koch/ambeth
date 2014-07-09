package de.osthus.ambeth.privilege.transfer;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.model.ISecurityScope;

@XmlRootElement(name = "PrivilegeResult", namespace = "http://schemas.osthus.de/Ambeth")
@XmlAccessorType(XmlAccessType.FIELD)
public class PrivilegeOfService implements IPrivilegeOfService
{
	@XmlElement(required = true)
	protected IObjRef reference;

	@XmlElement(required = true)
	protected ISecurityScope securityScope;

	@XmlElement(required = true)
	protected boolean readAllowed;

	@XmlElement(required = true)
	protected boolean createAllowed;

	@XmlElement(required = true)
	protected boolean updateAllowed;

	@XmlElement(required = true)
	protected boolean deleteAllowed;

	@XmlElement(required = true)
	protected boolean executeAllowed;

	@XmlElement(required = false)
	protected String[] propertyPrivilegeNames;

	@XmlElement(required = false)
	protected IPropertyPrivilegeOfService[] propertyPrivileges;

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
	public boolean isCreateAllowed()
	{
		return createAllowed;
	}

	public void setCreateAllowed(boolean createAllowed)
	{
		this.createAllowed = createAllowed;
	}

	@Override
	public boolean isReadAllowed()
	{
		return readAllowed;
	}

	public void setReadAllowed(boolean readAllowed)
	{
		this.readAllowed = readAllowed;
	}

	@Override
	public boolean isUpdateAllowed()
	{
		return updateAllowed;
	}

	public void setUpdateAllowed(boolean updateAllowed)
	{
		this.updateAllowed = updateAllowed;
	}

	@Override
	public boolean isDeleteAllowed()
	{
		return deleteAllowed;
	}

	public void setDeleteAllowed(boolean deleteAllowed)
	{
		this.deleteAllowed = deleteAllowed;
	}

	@Override
	public boolean isExecuteAllowed()
	{
		return executeAllowed;
	}

	public void setExecuteAllowed(boolean executeAllowed)
	{
		this.executeAllowed = executeAllowed;
	}

	@Override
	public String[] getPropertyPrivilegeNames()
	{
		return propertyPrivilegeNames;
	}

	public void setPropertyPrivilegeNames(String[] propertyPrivilegeNames)
	{
		this.propertyPrivilegeNames = propertyPrivilegeNames;
	}

	@Override
	public IPropertyPrivilegeOfService[] getPropertyPrivileges()
	{
		return propertyPrivileges;
	}

	public void setPropertyPrivileges(IPropertyPrivilegeOfService[] propertyPrivileges)
	{
		this.propertyPrivileges = propertyPrivileges;
	}
}
