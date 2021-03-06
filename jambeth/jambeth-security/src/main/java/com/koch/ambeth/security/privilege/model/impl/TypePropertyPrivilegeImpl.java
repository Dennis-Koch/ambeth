package com.koch.ambeth.security.privilege.model.impl;

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

import java.io.ObjectStreamException;
import java.io.Serializable;

import com.koch.ambeth.security.privilege.model.ITypePrivilege;
import com.koch.ambeth.security.privilege.model.ITypePropertyPrivilege;
import com.koch.ambeth.security.privilege.transfer.ITypePrivilegeOfService;
import com.koch.ambeth.security.privilege.transfer.ITypePropertyPrivilegeOfService;
import com.koch.ambeth.util.IImmutableType;
import com.koch.ambeth.util.IPrintable;

public final class TypePropertyPrivilegeImpl
		implements ITypePropertyPrivilege, IPrintable, IImmutableType, Serializable {
	private static final long serialVersionUID = -8157005380166070281L;

	public static final ITypePropertyPrivilege[] EMPTY_PROPERTY_PRIVILEGES =
			new ITypePropertyPrivilege[0];

	private static final TypePropertyPrivilegeImpl[] array =
			new TypePropertyPrivilegeImpl[arraySizeForIndex()];

	static {
		put1();
	}

	private static void put1() {
		put2(null);
		put2(Boolean.FALSE);
		put2(Boolean.TRUE);
	}

	private static void put2(Boolean create) {
		put3(create, null);
		put3(create, Boolean.FALSE);
		put3(create, Boolean.TRUE);
	}

	private static void put3(Boolean create, Boolean read) {
		put4(create, read, null);
		put4(create, read, Boolean.FALSE);
		put4(create, read, Boolean.TRUE);
	}

	private static void put4(Boolean create, Boolean read, Boolean update) {
		put(create, read, update, null);
		put(create, read, update, Boolean.FALSE);
		put(create, read, update, Boolean.TRUE);
	}

	public static int arraySizeForIndex() {
		return 27 * 3;
	}

	public static int calcIndex(Boolean create, Boolean read, Boolean update, Boolean delete) {
		return AbstractTypePrivilege.toBitValue(create, 1, 1 * 2)
				+ AbstractTypePrivilege.toBitValue(read, 3, 3 * 2)
				+ AbstractTypePrivilege.toBitValue(update, 9, 9 * 2)
				+ AbstractTypePrivilege.toBitValue(delete, 27, 27 * 2);
	}

	private static void put(Boolean create, Boolean read, Boolean update, Boolean delete) {
		int index = calcIndex(create, read, update, delete);
		array[index] = new TypePropertyPrivilegeImpl(create, read, update, delete);
	}

	public static ITypePropertyPrivilege create(Boolean create, Boolean read, Boolean update,
			Boolean delete) {
		int index = calcIndex(create, read, update, delete);
		return array[index];
	}

	public static ITypePropertyPrivilege createFrom(ITypePrivilege privilegeAsTemplate) {
		return create(privilegeAsTemplate.isCreateAllowed(), privilegeAsTemplate.isReadAllowed(),
				privilegeAsTemplate.isUpdateAllowed(), privilegeAsTemplate.isDeleteAllowed());
	}

	public static ITypePropertyPrivilege createFrom(ITypePrivilegeOfService privilegeOfService) {
		return create(privilegeOfService.isCreateAllowed(), privilegeOfService.isReadAllowed(),
				privilegeOfService.isUpdateAllowed(), privilegeOfService.isDeleteAllowed());
	}

	public static ITypePropertyPrivilege createFrom(
			ITypePropertyPrivilegeOfService propertyPrivilegeResult) {
		return create(propertyPrivilegeResult.isCreateAllowed(),
				propertyPrivilegeResult.isReadAllowed(), propertyPrivilegeResult.isUpdateAllowed(),
				propertyPrivilegeResult.isDeleteAllowed());
	}

	private final Boolean create;
	private final Boolean read;
	private final Boolean update;
	private final Boolean delete;

	private TypePropertyPrivilegeImpl(Boolean create, Boolean read, Boolean update, Boolean delete) {
		this.create = create;
		this.read = read;
		this.update = update;
		this.delete = delete;
	}

	@Override
	public Boolean isCreateAllowed() {
		return create;
	}

	@Override
	public Boolean isReadAllowed() {
		return read;
	}

	@Override
	public Boolean isUpdateAllowed() {
		return update;
	}

	@Override
	public Boolean isDeleteAllowed() {
		return delete;
	}

	@Override
	public final String toString() {
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}

	@Override
	public void toString(StringBuilder sb) {
		sb.append(AbstractPrivilege.upperOrLower(isCreateAllowed(), 'c'));
		sb.append(AbstractPrivilege.upperOrLower(isReadAllowed(), 'r'));
		sb.append(AbstractPrivilege.upperOrLower(isUpdateAllowed(), 'u'));
		sb.append(AbstractPrivilege.upperOrLower(isDeleteAllowed(), 'd'));
	}

	private Object readResolve() throws ObjectStreamException {
		return create(create, read, update, delete);
	}
}
