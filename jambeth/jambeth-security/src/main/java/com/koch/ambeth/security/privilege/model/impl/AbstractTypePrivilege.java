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

import com.koch.ambeth.security.privilege.model.ITypePrivilege;
import com.koch.ambeth.security.privilege.model.ITypePropertyPrivilege;
import com.koch.ambeth.util.IImmutableType;
import com.koch.ambeth.util.IPrintable;

public abstract class AbstractTypePrivilege implements ITypePrivilege, IPrintable, IImmutableType {
	public static int arraySizeForIndex() {
		return 81 * 3;
	}

	public static int calcIndex(Boolean create, Boolean read, Boolean update, Boolean delete,
			Boolean execute) {
		return toBitValue(create, 1, 1 * 2) + toBitValue(read, 3, 3 * 2) + toBitValue(update, 9, 9 * 2)
				+ toBitValue(delete, 27, 27 * 2) + toBitValue(execute, 81, 81 * 2);
	}

	public static int toBitValue(Boolean value, int valueIfTrue, int valueIfFalse) {
		if (value == null) {
			return 0;
		}
		return value.booleanValue() ? valueIfTrue : valueIfFalse;
	}

	public AbstractTypePrivilege(Boolean create, Boolean read, Boolean update, Boolean delete,
			Boolean execute, ITypePropertyPrivilege[] primitivePropertyPrivileges,
			ITypePropertyPrivilege[] relationPropertyPrivileges) {
		// intended blank
	}

	@Override
	public abstract ITypePropertyPrivilege getPrimitivePropertyPrivilege(int primitiveIndex);

	@Override
	public abstract ITypePropertyPrivilege getRelationPropertyPrivilege(int relationIndex);

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
		sb.append(AbstractPrivilege.upperOrLower(isExecuteAllowed(), 'e'));
	}
}
