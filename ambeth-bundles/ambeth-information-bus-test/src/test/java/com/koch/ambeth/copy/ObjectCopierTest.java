package com.koch.ambeth.copy;

/*-
 * #%L
 * jambeth-merge-test
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

import java.util.Arrays;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import com.koch.ambeth.merge.copy.IObjectCopier;
import com.koch.ambeth.merge.ioc.ObjectCopierModule;
import com.koch.ambeth.testutil.AbstractIocTest;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.util.ParamChecker;

/**
 * Tests the ObjectCopier-Bean functionality There is only the ObjectCopierModule needed to allow
 * isolated tests
 */
@TestModule(ObjectCopierModule.class)
public class ObjectCopierTest extends AbstractIocTest {
	protected IObjectCopier fixture;

	@Override
	public void afterPropertiesSet() throws Throwable {
		super.afterPropertiesSet();

		ParamChecker.assertNotNull(fixture, "fixture");
	}

	/**
	 * Injected fixture to test
	 *
	 * @param fixture
	 */
	public void setFixture(IObjectCopier fixture) {
		this.fixture = fixture;
	}

	@Test
	public void cloneInteger() {
		Integer original = new Integer(5);
		Integer clone = fixture.clone(original);
		Assert.assertSame(original, clone);
	}

	@Test
	public void cloneLong() {
		Long original = new Long(5);
		Long clone = fixture.clone(original);
		Assert.assertSame(original, clone);
	}

	@Test
	public void cloneDouble() {
		Double original = new Double(5);
		Double clone = fixture.clone(original);
		Assert.assertSame(original, clone);
	}

	@Test
	public void cloneFloat() {
		Float original = new Float(5);
		Float clone = fixture.clone(original);
		Assert.assertSame(original, clone);
	}

	@Test
	public void cloneByte() {
		Byte original = new Byte((byte) 5);
		Byte clone = fixture.clone(original);
		Assert.assertSame(original, clone);
	}

	@Test
	public void cloneCharacter() {
		Character original = new Character((char) 5);
		Character clone = fixture.clone(original);
		Assert.assertSame(original, clone);
	}

	@Test
	public void cloneBoolean() {
		Boolean original = new Boolean(true);
		Boolean clone = fixture.clone(original);
		Assert.assertSame(original, clone);
	}

	@Test
	public void cloneDate() {
		Date original = new Date(System.currentTimeMillis() - 1000);
		Date clone = fixture.clone(original);
		Assert.assertNotSame(original, clone);
		Assert.assertEquals(original, clone);
	}

	@Test
	public void cloneMaterial() {
		StringBuilder original = new StringBuilder("abc");
		StringBuilder clone = fixture.clone(original);
		Assert.assertNotSame(original, clone);
		Assert.assertEquals(original.toString(), clone.toString());
	}

	@Test
	public void cloneByteArrayNative() {
		byte[] original = new byte[] {5, 4, 3, 2, 1};
		byte[] clone = fixture.clone(original);
		Assert.assertNotSame(original, clone);
		Arrays.equals(original, clone);
	}

	@Test
	public void cloneByteArray() {
		Byte[] original = new Byte[] {5, 4, 3, 2, 1};
		Byte[] clone = fixture.clone(original);
		Assert.assertNotSame(original, clone);
		Arrays.deepEquals(original, clone);
	}

	@Test
	public void cloneArrayOfArrays() {
		Object[][] original =
				new Object[][] {new Object[] {new Integer(5)}, new Object[] {new Long(6), new Double(7)}};
		Object[][] clone = fixture.clone(original);
		Assert.assertNotSame(original, clone);
		Arrays.deepEquals(original, clone);
	}
}
