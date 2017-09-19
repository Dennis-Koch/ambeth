package com.koch.ambeth.util;

import org.junit.Assert;
import org.junit.Test;

import com.koch.ambeth.ioc.util.ImmutableTypeSet;

public class ImmutableTypeSetTest {
	public interface MyType {
		// intended blank
	}

	public class MyClass implements MyType {
		// intended blank
	}

	@Test
	public void test() {
		ImmutableTypeSet immutableTypeSet = new ImmutableTypeSet();

		Assert.assertFalse(immutableTypeSet.isImmutableType(MyClass.class));

		immutableTypeSet.registerImmutableType(MyType.class);

		Assert.assertTrue(immutableTypeSet.isImmutableType(MyType.class));
		Assert.assertTrue(immutableTypeSet.isImmutableType(MyClass.class));
	}
}
