package com.koch.ambeth.persistence.pg;

/*-
 * #%L
 * jambeth-persistence-pg
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

import org.postgresql.PGConnection;

import com.koch.ambeth.stream.IInputStream;
import com.koch.ambeth.stream.IUnmodifiedInputSource;
import com.koch.ambeth.stream.chars.ICharacterInputSource;
import com.koch.ambeth.stream.chars.ICharacterInputStream;
import com.koch.ambeth.util.IImmutableType;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class PostgresCharacterInputSource
		implements ICharacterInputSource, IUnmodifiedInputSource, IImmutableType {
	protected long oid;

	protected PGConnection connection;

	public PostgresCharacterInputSource(long oid, PGConnection connection) {
		this.oid = oid;
		this.connection = connection;
	}

	@Override
	public IInputStream deriveInputStream() {
		try {
			System.out.println();
			return null;
			// return new PostgresCharacterInputStream(new PostgresClob(connection, oid));
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public ICharacterInputStream deriveCharacterInputStream() {
		try {
			System.out.println();
			return null;
			// return new PostgresCharacterInputStream(new PostgresClob(connection, oid));
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
