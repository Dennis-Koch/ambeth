package de.osthus.classbrowser.java;

import org.junit.After;

import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.LogInstance;

public class TestClass {

	@LogInstance
	private Object log;

	@Autowired
	protected Object service;

	protected Object internal;

	@After
	public void setInternal(Object internal) {
		this.internal = internal;
	}

}
