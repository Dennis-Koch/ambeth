package com.koch.ambeth.persistence.jdbc.lob;

/*-
 * #%L
 * jambeth-persistence-jdbc
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

import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;

import com.koch.ambeth.query.persistence.IDataCursor;
import com.koch.ambeth.stream.IInputStream;
import com.koch.ambeth.stream.binary.IBinaryInputStream;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class BlobInputStream implements IBinaryInputStream, IInputStream {
	protected final Blob blob;

	protected final InputStream is;

	protected final IDataCursor cursor;

	public BlobInputStream(IDataCursor cursor, Blob blob) {
		this.cursor = cursor;
		this.blob = blob;
		try {
			is = blob.getBinaryStream();
		}
		catch (SQLException e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	protected void finalize() throws Throwable {
		close();
	}

	@Override
	public void close() throws IOException {
		try {
			is.close();
			blob.free();
		}
		catch (SQLException e) {
			throw RuntimeExceptionUtil.mask(e);
		}
		finally {
			cursor.dispose();
		}
	}

	@Override
	public int readByte() {
		try {
			return is.read();
		}
		catch (IOException e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
