package com.koch.ambeth.ioc.injection;

public class InjectionTestBean
{
	private String name;

	private InjectionTestBean previous;

	private InjectionTestBean counterpart;

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public InjectionTestBean getPrevious()
	{
		return previous;
	}

	public void setPrevious(InjectionTestBean previous)
	{
		this.previous = previous;
	}

	public InjectionTestBean getCounterpart()
	{
		return counterpart;
	}

	public void setCounterpart(InjectionTestBean counterpart)
	{
		this.counterpart = counterpart;
	}
}
