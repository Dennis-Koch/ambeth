package de.osthus.ambeth.demo;

public abstract class AbstractObject
{
	public void method1()
	{
		System.out.println(getClass().toString());
	}

	public abstract void method2();
}
