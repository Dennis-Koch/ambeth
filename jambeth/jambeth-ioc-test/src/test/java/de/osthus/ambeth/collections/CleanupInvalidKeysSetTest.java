package de.osthus.ambeth.collections;

import java.lang.ref.WeakReference;

import org.junit.Assert;
import org.junit.Test;

import de.osthus.ambeth.collections.CleanupInvalidKeysSetTest.TestKey;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.testutil.AbstractIocTest;

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
