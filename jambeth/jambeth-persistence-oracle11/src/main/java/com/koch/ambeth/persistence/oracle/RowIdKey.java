package com.koch.ambeth.persistence.oracle;

/*-
 * #%L
 * jambeth-persistence-oracle11
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

import java.nio.charset.Charset;
import java.util.Arrays;

public class RowIdKey
{
	private static final Charset utf8 = Charset.forName("UTF-8");

	protected final byte[] value;

	public RowIdKey(byte[] value)
	{
		this.value = value;
	}

	public byte[] getValue()
	{
		return value;
	}

	@Override
	public int hashCode()
	{
		return Arrays.hashCode(value);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		if (!(obj instanceof RowIdKey))
		{
			return false;
		}
		RowIdKey other = (RowIdKey) obj;
		return Arrays.equals(value, other.value);
	}

	@Override
	public String toString()
	{
		return new String(value, utf8);
	}
}
