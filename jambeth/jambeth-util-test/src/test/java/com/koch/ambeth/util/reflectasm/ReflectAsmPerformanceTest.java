package com.koch.ambeth.util.reflectasm;

/*-
 * #%L
 * jambeth-util-test
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

import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.koch.ambeth.repackaged.com.esotericsoftware.reflectasm.MethodAccess;
import com.koch.ambeth.testutil.category.PerformanceTests;

@Category(PerformanceTests.class)
public class ReflectAsmPerformanceTest {
	public static class MyClass {
		protected int myField;

		public int getMyField() {
			return myField;
		}

		public void setMyField(int myField) {
			this.myField = myField;
		}
	}

	protected int iterationCount = 100000000;

	protected MyClass obj = new MyClass();

	protected Method method;

	protected MethodAccess methodAccess;

	protected int getIndex;

	@Before
	public void before() throws Exception {
		method = MyClass.class.getMethod("getMyField");
		methodAccess = MethodAccess.get(MyClass.class);
		getIndex = methodAccess.getIndex("getMyField");
	}

	@Test
	public void reflectionAccess() throws Exception {
		int iterationCount = this.iterationCount;
		Method method = this.method;
		MyClass obj = this.obj;

		for (int a = iterationCount; a-- > 0;) {
			@SuppressWarnings("unused")
			Object result = method.invoke(obj);
			result = method.invoke(obj);
			result = method.invoke(obj);
			result = method.invoke(obj);
			result = method.invoke(obj);
			result = method.invoke(obj);
			result = method.invoke(obj);
			result = method.invoke(obj);
			result = method.invoke(obj);
			result = method.invoke(obj);
			result = method.invoke(obj);
			result = method.invoke(obj);
			result = method.invoke(obj);
			result = method.invoke(obj);
			result = method.invoke(obj);
			result = method.invoke(obj);
			result = method.invoke(obj);
			result = method.invoke(obj);
			result = method.invoke(obj);
			result = method.invoke(obj);
		}
	}

	@Test
	public void asmAccess() {
		int iterationCount = this.iterationCount;
		MethodAccess methodAccess = this.methodAccess;
		int getIndex = this.getIndex;
		MyClass obj = this.obj;

		for (int a = iterationCount; a-- > 0;) {
			@SuppressWarnings("unused")
			Object result = methodAccess.invoke(obj, getIndex);
			result = methodAccess.invoke(obj, getIndex);
			result = methodAccess.invoke(obj, getIndex);
			result = methodAccess.invoke(obj, getIndex);
			result = methodAccess.invoke(obj, getIndex);
			result = methodAccess.invoke(obj, getIndex);
			result = methodAccess.invoke(obj, getIndex);
			result = methodAccess.invoke(obj, getIndex);
			result = methodAccess.invoke(obj, getIndex);
			result = methodAccess.invoke(obj, getIndex);
			result = methodAccess.invoke(obj, getIndex);
			result = methodAccess.invoke(obj, getIndex);
			result = methodAccess.invoke(obj, getIndex);
			result = methodAccess.invoke(obj, getIndex);
			result = methodAccess.invoke(obj, getIndex);
			result = methodAccess.invoke(obj, getIndex);
			result = methodAccess.invoke(obj, getIndex);
			result = methodAccess.invoke(obj, getIndex);
			result = methodAccess.invoke(obj, getIndex);
			result = methodAccess.invoke(obj, getIndex);
		}
	}

	@Test
	public void asmNoGarbageAccess() {
		int iterationCount = this.iterationCount;
		MethodAccess methodAccess = this.methodAccess;
		int getIndex = this.getIndex;
		MyClass obj = this.obj;
		Object[] emptyArgs = new Object[0];

		for (int a = iterationCount; a-- > 0;) {
			@SuppressWarnings("unused")
			Object result = methodAccess.invoke(obj, getIndex, emptyArgs);
			result = methodAccess.invoke(obj, getIndex, emptyArgs);
			result = methodAccess.invoke(obj, getIndex, emptyArgs);
			result = methodAccess.invoke(obj, getIndex, emptyArgs);
			result = methodAccess.invoke(obj, getIndex, emptyArgs);
			result = methodAccess.invoke(obj, getIndex, emptyArgs);
			result = methodAccess.invoke(obj, getIndex, emptyArgs);
			result = methodAccess.invoke(obj, getIndex, emptyArgs);
			result = methodAccess.invoke(obj, getIndex, emptyArgs);
			result = methodAccess.invoke(obj, getIndex, emptyArgs);
			result = methodAccess.invoke(obj, getIndex, emptyArgs);
			result = methodAccess.invoke(obj, getIndex, emptyArgs);
			result = methodAccess.invoke(obj, getIndex, emptyArgs);
			result = methodAccess.invoke(obj, getIndex, emptyArgs);
			result = methodAccess.invoke(obj, getIndex, emptyArgs);
			result = methodAccess.invoke(obj, getIndex, emptyArgs);
			result = methodAccess.invoke(obj, getIndex, emptyArgs);
			result = methodAccess.invoke(obj, getIndex, emptyArgs);
			result = methodAccess.invoke(obj, getIndex, emptyArgs);
			result = methodAccess.invoke(obj, getIndex, emptyArgs);
		}
	}
}
