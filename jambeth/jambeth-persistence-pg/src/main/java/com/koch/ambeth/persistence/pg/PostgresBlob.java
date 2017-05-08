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

import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.SQLException;

import org.postgresql.PGConnection;
import org.postgresql.largeobject.LargeObject;
import org.postgresql.largeobject.LargeObjectManager;

public class PostgresBlob implements Blob {
	private long oid;

	private LargeObjectManager largeObjectManager;

	public PostgresBlob(PGConnection connection, long oid) throws SQLException {
		this.oid = oid;
		largeObjectManager = connection.getLargeObjectAPI();
	}

	@Override
	public void free() throws SQLException {
		largeObjectManager = null;
	}

	@Override
	public long length() throws SQLException {
		LargeObject lo = largeObjectManager.open(oid, LargeObjectManager.READ);
		try {
			return lo.size64();
		}
		finally {
			lo.close();
		}
	}

	@Override
	public byte[] getBytes(long pos, int length) throws SQLException {
		LargeObject lo = largeObjectManager.open(oid, LargeObjectManager.READ);
		try {
			lo.seek64(pos - 1, LargeObject.SEEK_SET);
			return lo.read(length);
		}
		finally {
			lo.close();
		}
	}

	@Override
	public InputStream getBinaryStream() throws SQLException {
		LargeObject lo = largeObjectManager.open(oid, LargeObjectManager.READ);
		return lo.getInputStream();
	}

	@Override
	public InputStream getBinaryStream(long pos, long length) throws SQLException {
		LargeObject lo = largeObjectManager.open(oid, LargeObjectManager.READ);
		lo.seek64(pos - 1, LargeObject.SEEK_SET);
		return lo.getInputStream(length);
	}

	@Override
	public long position(byte[] pattern, long start) throws SQLException {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public long position(Blob pattern, long start) throws SQLException {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public int setBytes(long pos, byte[] bytes) throws SQLException {
		LargeObject lo = largeObjectManager.open(oid, LargeObjectManager.WRITE);
		try {
			lo.seek64(pos - 1, LargeObject.SEEK_SET);
			lo.write(bytes);
			return bytes.length;
		}
		finally {
			lo.close();
		}
	}

	@Override
	public int setBytes(long pos, byte[] bytes, int offset, int len) throws SQLException {
		LargeObject lo = largeObjectManager.open(oid, LargeObjectManager.WRITE);
		try {
			lo.seek64(pos - 1, LargeObject.SEEK_SET);
			lo.write(bytes, offset, len);
			return len;
		}
		finally {
			lo.close();
		}
	}

	@Override
	public OutputStream setBinaryStream(long pos) throws SQLException {
		LargeObject lo = largeObjectManager.open(oid, LargeObjectManager.WRITE);
		lo.seek64(pos - 1, LargeObject.SEEK_SET);
		return lo.getOutputStream();
	}

	@Override
	public void truncate(long len) throws SQLException {
		LargeObject lo = largeObjectManager.open(oid, LargeObjectManager.WRITE);
		try {
			lo.truncate64(len);
		}
		finally {
			lo.close();
		}
	}
}
