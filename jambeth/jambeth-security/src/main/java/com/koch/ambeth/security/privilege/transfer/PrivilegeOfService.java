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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.model.ISecurityScope;
import com.koch.ambeth.util.IPrintable;

@XmlRootElement(name = "PrivilegeOfService", namespace = "http://schema.kochdev.com/Ambeth")
@XmlAccessorType(XmlAccessType.FIELD)
public class PrivilegeOfService implements IPrivilegeOfService, IPrintable {
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
	public IObjRef getReference() {
		return reference;
	}

	public void setReference(IObjRef reference) {
		this.reference = reference;
	}

	@Override
	public ISecurityScope getSecurityScope() {
		return securityScope;
	}

	public void setSecurityScope(ISecurityScope securityScope) {
		this.securityScope = securityScope;
	}

	@Override
	public boolean isCreateAllowed() {
		return createAllowed;
	}

	public void setCreateAllowed(boolean createAllowed) {
		this.createAllowed = createAllowed;
	}

	@Override
	public boolean isReadAllowed() {
		return readAllowed;
	}

	public void setReadAllowed(boolean readAllowed) {
		this.readAllowed = readAllowed;
	}

	@Override
	public boolean isUpdateAllowed() {
		return updateAllowed;
	}

	public void setUpdateAllowed(boolean updateAllowed) {
		this.updateAllowed = updateAllowed;
	}

	@Override
	public boolean isDeleteAllowed() {
		return deleteAllowed;
	}

	public void setDeleteAllowed(boolean deleteAllowed) {
		this.deleteAllowed = deleteAllowed;
	}

	@Override
	public boolean isExecuteAllowed() {
		return executeAllowed;
	}

	public void setExecuteAllowed(boolean executeAllowed) {
		this.executeAllowed = executeAllowed;
	}

	@Override
	public String[] getPropertyPrivilegeNames() {
		return propertyPrivilegeNames;
	}

	public void setPropertyPrivilegeNames(String[] propertyPrivilegeNames) {
		this.propertyPrivilegeNames = propertyPrivilegeNames;
	}

	@Override
	public IPropertyPrivilegeOfService[] getPropertyPrivileges() {
		return propertyPrivileges;
	}

	public void setPropertyPrivileges(IPropertyPrivilegeOfService[] propertyPrivileges) {
		this.propertyPrivileges = propertyPrivileges;
	}

	@Override
	public final String toString() {
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}

	@Override
	public void toString(StringBuilder sb) {
		sb.append(isReadAllowed() ? 'r' : '-');
		sb.append(isCreateAllowed() ? 'c' : '-');
		sb.append(isUpdateAllowed() ? 'u' : '-');
		sb.append(isDeleteAllowed() ? 'd' : '-');
		sb.append(isExecuteAllowed() ? 'e' : '-');
	}
}
