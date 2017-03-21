package com.koch.ambeth.security.privilege.transfer;

/*-
 * #%L
 * jambeth-security
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.koch.ambeth.service.model.ISecurityScope;
import com.koch.ambeth.util.IPrintable;

@XmlRootElement(name = "TypePrivilegeOfService", namespace = "http://schema.kochdev.com/Ambeth")
@XmlAccessorType(XmlAccessType.FIELD)
public class TypePrivilegeOfService implements ITypePrivilegeOfService, IPrintable
{
	@XmlElement(required = true)
	protected Class<?> entityType;

	@XmlElement(required = true)
	protected ISecurityScope securityScope;

	@XmlElement(required = true)
	protected Boolean readAllowed;

	@XmlElement(required = true)
	protected Boolean createAllowed;

	@XmlElement(required = true)
	protected Boolean updateAllowed;

	@XmlElement(required = true)
	protected Boolean deleteAllowed;

	@XmlElement(required = true)
	protected Boolean executeAllowed;

	@XmlElement(required = false)
	protected String[] propertyPrivilegeNames;

	@XmlElement(required = false)
	protected ITypePropertyPrivilegeOfService[] propertyPrivileges;

	@Override
	public Class<?> getEntityType()
	{
		return entityType;
	}

	public void setEntityType(Class<?> entityType)
	{
		this.entityType = entityType;
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
	public Boolean isCreateAllowed()
	{
		return createAllowed;
	}

	public void setCreateAllowed(Boolean createAllowed)
	{
		this.createAllowed = createAllowed;
	}

	@Override
	public Boolean isReadAllowed()
	{
		return readAllowed;
	}

	public void setReadAllowed(Boolean readAllowed)
	{
		this.readAllowed = readAllowed;
	}

	@Override
	public Boolean isUpdateAllowed()
	{
		return updateAllowed;
	}

	public void setUpdateAllowed(Boolean updateAllowed)
	{
		this.updateAllowed = updateAllowed;
	}

	@Override
	public Boolean isDeleteAllowed()
	{
		return deleteAllowed;
	}

	public void setDeleteAllowed(Boolean deleteAllowed)
	{
		this.deleteAllowed = deleteAllowed;
	}

	@Override
	public Boolean isExecuteAllowed()
	{
		return executeAllowed;
	}

	public void setExecuteAllowed(Boolean executeAllowed)
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
	public ITypePropertyPrivilegeOfService[] getPropertyPrivileges()
	{
		return propertyPrivileges;
	}

	public void setPropertyPrivileges(ITypePropertyPrivilegeOfService[] propertyPrivileges)
	{
		this.propertyPrivileges = propertyPrivileges;
	}

	@Override
	public final String toString()
	{
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}

	@Override
	public void toString(StringBuilder sb)
	{
		sb.append(isReadAllowed() != null ? isReadAllowed() ? "+R" : "-R" : "nR");
		sb.append(isCreateAllowed() != null ? isCreateAllowed() ? "+C" : "-C" : "nC");
		sb.append(isUpdateAllowed() != null ? isUpdateAllowed() ? "+U" : "-U" : "nU");
		sb.append(isDeleteAllowed() != null ? isDeleteAllowed() ? "+D" : "-D" : "nD");
		sb.append(isExecuteAllowed() != null ? isExecuteAllowed() ? "+E" : "-E" : "nE");
	}
}
