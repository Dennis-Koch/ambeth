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
import javax.xml.bind.annotation.XmlRootElement;

import com.koch.ambeth.security.privilege.model.impl.TypePropertyPrivilegeImpl;
import com.koch.ambeth.util.IPrintable;

@XmlRootElement(name = "TypePropertyPrivilegeOfService",
		namespace = "http://schema.kochdev.com/Ambeth")
@XmlAccessorType(XmlAccessType.FIELD)
public final class TypePropertyPrivilegeOfService
		implements ITypePropertyPrivilegeOfService, IPrintable {
	private static final TypePropertyPrivilegeOfService[] array =
			new TypePropertyPrivilegeOfService[TypePropertyPrivilegeImpl.arraySizeForIndex()];

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

	private static void put(Boolean create, Boolean read, Boolean update, Boolean delete) {
		int index = TypePropertyPrivilegeImpl.calcIndex(create, read, update, delete);
		array[index] = new TypePropertyPrivilegeOfService(create, read, update, delete);
	}

	public static ITypePropertyPrivilegeOfService create(Boolean create, Boolean read, Boolean update,
			Boolean delete) {
		int index = TypePropertyPrivilegeImpl.calcIndex(create, read, update, delete);
		return array[index];
	}

	private final Boolean create;
	private final Boolean read;
	private final Boolean update;
	private final Boolean delete;

	private TypePropertyPrivilegeOfService(Boolean create, Boolean read, Boolean update,
			Boolean delete) {
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
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof TypePropertyPrivilegeOfService)) {
			return false;
		}
		TypePropertyPrivilegeOfService other = (TypePropertyPrivilegeOfService) obj;
		int index = TypePropertyPrivilegeImpl.calcIndex(create, read, update, delete);
		int otherIndex =
				TypePropertyPrivilegeImpl.calcIndex(other.create, other.read, other.update, other.delete);
		return index == otherIndex;
	}

	@Override
	public int hashCode() {
		return TypePropertyPrivilegeImpl.calcIndex(create, read, update, delete);
	}

	@Override
	public final String toString() {
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}

	@Override
	public void toString(StringBuilder sb) {
		sb.append(isReadAllowed() != null ? isReadAllowed() ? "+R" : "-R" : "nR");
		sb.append(isCreateAllowed() != null ? isCreateAllowed() ? "+C" : "-C" : "nC");
		sb.append(isUpdateAllowed() != null ? isUpdateAllowed() ? "+U" : "-U" : "nU");
		sb.append(isDeleteAllowed() != null ? isDeleteAllowed() ? "+D" : "-D" : "nD");
	}
}
