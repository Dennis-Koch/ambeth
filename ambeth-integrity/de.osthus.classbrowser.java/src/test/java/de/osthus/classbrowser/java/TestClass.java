package de.osthus.classbrowser.java;

import org.junit.After;

import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.LogInstance;

public class TestClass
{
	@LogInstance
	private Object log;

	@Autowired
	protected Object service;

	@Autowired(TestClass.class)
	protected Object service2;

	@Autowired(name = "test", optional = true)
	protected Object service3;

	protected Object internal;

	@After
	public void setInternal(Object internal)
	{
		this.internal = internal;
	}
}
