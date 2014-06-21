package de.osthus.ambeth.reflectasm;

import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import de.osthus.ambeth.repackaged.com.esotericsoftware.reflectasm.MethodAccess;
import de.osthus.ambeth.testutil.category.PerformanceTests;

@Category(PerformanceTests.class)
public class ReflectAsmPerformanceTest
{
	public static class MyClass
	{
		protected int myField;

		public int getMyField()
		{
			return myField;
		}

		public void setMyField(int myField)
		{
			this.myField = myField;
		}
	}

	protected int iterationCount = 100000000;

	protected MyClass obj = new MyClass();

	protected Method method;

	protected MethodAccess methodAccess;

	protected int getIndex;

	@Before
	public void before() throws Exception
	{
		method = MyClass.class.getMethod("getMyField");
		methodAccess = MethodAccess.get(MyClass.class);
		getIndex = methodAccess.getIndex("getMyField");
	}

	@Test
	public void reflectionAccess() throws Exception
	{
		int iterationCount = this.iterationCount;
		Method method = this.method;
		MyClass obj = this.obj;

		for (int a = iterationCount; a-- > 0;)
		{
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
	public void asmAccess()
	{
		int iterationCount = this.iterationCount;
		MethodAccess methodAccess = this.methodAccess;
		int getIndex = this.getIndex;
		MyClass obj = this.obj;

		for (int a = iterationCount; a-- > 0;)
		{
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
	public void asmNoGarbageAccess()
	{
		int iterationCount = this.iterationCount;
		MethodAccess methodAccess = this.methodAccess;
		int getIndex = this.getIndex;
		MyClass obj = this.obj;
		Object[] emptyArgs = new Object[0];

		for (int a = iterationCount; a-- > 0;)
		{
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
