package com.koch.ambeth.xml.pending;

import com.koch.ambeth.service.merge.model.IObjRef;

public class ObjRefFuture implements IObjectFuture
{
	private final IObjRef ori;

	private Object value;

	public ObjRefFuture(IObjRef ori)
	{
		this.ori = ori;
	}

	@Override
	public Object getValue()
	{
		return value;
	}

	public void setValue(Object value)
	{
		this.value = value;
	}

	public IObjRef getOri()
	{
		return ori;
	}
}
