package com.koch.ambeth.collections;

/*-
 * #%L
 * jambeth-ioc-test
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

import java.lang.ref.WeakReference;

import org.junit.Assert;
import org.junit.Test;

import com.koch.ambeth.collections.CleanupInvalidKeysSetTest.TestKey;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.testutil.AbstractIocTest;
import com.koch.ambeth.util.collections.CleanupInvalidKeysSet;
import com.koch.ambeth.util.collections.IInvalidKeyChecker;

public class CleanupInvalidKeysSetTest extends AbstractIocTest implements IInvalidKeyChecker<TestKey>
{
	public static class TestKey extends WeakReference<String>
	{
		protected final int hash;

		public TestKey(String referent)
		{
			super(referent);
			this.hash = referent.hashCode();
		}

		@Override
		public boolean equals(Object obj)
		{
			if (obj == this)
			{
				return true;
			}
			if (!(obj instanceof TestKey))
			{
				return false;
			}
			String value = get();
			if (value == null)
			{
				return false;
			}
			return value.equals(((TestKey) obj).get());
		}

		@Override
		public int hashCode()
		{
			return hash;
		}

		@Override
		public String toString()
		{
			return get();
		}
	}

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public boolean isKeyValid(TestKey key)
	{
		return key.get() != null;
	}

	@Test
	public void invalidKeys()
	{
		CleanupInvalidKeysSet<TestKey> set = new CleanupInvalidKeysSet<TestKey>(this);
		int count = 1000000;
		for (int a = count; a-- > 0;)
		{
			set.add(new TestKey("key" + a));
			if (a % (count / 100) == 0)
			{
				System.gc();
			}
		}
		Assert.assertNotEquals(set.size(), count);
	}
}
