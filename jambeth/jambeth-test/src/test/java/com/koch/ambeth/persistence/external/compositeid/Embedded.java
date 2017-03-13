package com.koch.ambeth.persistence.external.compositeid;

public class Embedded
{
	protected String sid;

	protected Embedded()
	{
		// Intended blank
	}

	public void setSid(String sid)
	{
		this.sid = sid;
	}

	public String getSid()
	{
		return sid;
	}
}
