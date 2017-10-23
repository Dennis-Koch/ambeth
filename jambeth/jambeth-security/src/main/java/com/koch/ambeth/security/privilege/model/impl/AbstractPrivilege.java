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

import java.io.Serializable;

import com.koch.ambeth.security.privilege.model.IPrivilege;
import com.koch.ambeth.security.privilege.model.IPropertyPrivilege;
import com.koch.ambeth.util.IImmutableType;
import com.koch.ambeth.util.IPrintable;

public abstract class AbstractPrivilege
		implements IPrivilege, IPrintable, IImmutableType, Serializable {
	private static final long serialVersionUID = 1549203069449950797L;

	public static int arraySizeForIndex() {
		return 1 << 8;
	}

	public static int calcIndex(boolean create, boolean read, boolean update, boolean delete,
			boolean execute) {
		return toBitValue(create, 0) + toBitValue(read, 1) + toBitValue(update, 2)
				+ toBitValue(delete, 3) + toBitValue(execute, 4);
	}

	public static int toBitValue(boolean value, int startingBit) {
		return value ? 1 << startingBit : 0;
	}

	public static Boolean parse3ValueFlag(char value, char trueChar, char falseChar) {
		if (value == trueChar) {
			return Boolean.TRUE;
		}
		if (value == falseChar) {
			return Boolean.FALSE;
		}
		return null;
	}

	public static boolean parseFlag(char value, char trueChar, char falseChar) {
		if (value == trueChar) {
			return true;
		}
		return false;
	}

	public static char upperOrLower(boolean flag, char oneChar) {
		if (flag) {
			return Character.toUpperCase(oneChar);
		}
		return Character.toLowerCase(oneChar);
	}

	public static char upperOrLower(Boolean flag, char oneChar) {
		if (flag == null) {
			return '_';
		}
		if (flag.booleanValue()) {
			return Character.toUpperCase(oneChar);
		}
		return Character.toLowerCase(oneChar);
	}

	public AbstractPrivilege(boolean create, boolean read, boolean update, boolean delete,
			boolean execute, IPropertyPrivilege[] primitivePropertyPrivileges,
			IPropertyPrivilege[] relationPropertyPrivileges) {
		// intended blank
	}

	@Override
	public abstract IPropertyPrivilege getPrimitivePropertyPrivilege(int primitiveIndex);

	@Override
	public abstract IPropertyPrivilege getRelationPropertyPrivilege(int relationIndex);

	@Override
	public final String toString() {
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}

	@Override
	public void toString(StringBuilder sb) {
		sb.append(upperOrLower(isCreateAllowed(), 'c'));
		sb.append(upperOrLower(isReadAllowed(), 'r'));
		sb.append(upperOrLower(isUpdateAllowed(), 'u'));
		sb.append(upperOrLower(isDeleteAllowed(), 'd'));
		sb.append(upperOrLower(isExecuteAllowed(), 'e'));
	}
}
