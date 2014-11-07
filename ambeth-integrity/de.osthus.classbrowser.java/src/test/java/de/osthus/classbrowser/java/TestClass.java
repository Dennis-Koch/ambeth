package de.osthus.classbrowser.java;

import org.junit.After;

import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.LogInstance;

public class TestClass extends AbstractTestClass implements TestInterface1, TestInterface2
{
	public static final String CONST = "test constant";

	@SuppressWarnings("unused")
	private static final int DEFAULT = 42;

	@LogInstance
	private Object log;

	@Autowired
	protected Object service;

	@Autowired(TestClass.class)
	protected Object service2;

	@Autowired(name = "test", optional = true)
	protected Object service3;

	protected Object internal;

	@Override
	@After
	public void setInternal(Object internal)
	{
		this.internal = internal;
	}
}
