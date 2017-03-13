package com.koch.ambeth.persistence.streaming;

import com.koch.ambeth.stream.bool.IBooleanInputSource;
import com.koch.ambeth.stream.float32.IFloatInputSource;
import com.koch.ambeth.stream.float64.IDoubleInputSource;
import com.koch.ambeth.stream.int32.IIntInputSource;
import com.koch.ambeth.stream.int64.ILongInputSource;
import com.koch.ambeth.stream.strings.IStringInputSource;

public abstract class StreamingEntity
{
	protected int id;

	protected IBooleanInputSource booleanStreamed;

	protected IDoubleInputSource doubleStreamed;

	protected IFloatInputSource floatStreamed;

	protected IIntInputSource intStreamed;

	protected ILongInputSource longStreamed;

	protected IStringInputSource stringStreamed;

	protected StreamingEntity()
	{
		// Intended blank
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public IBooleanInputSource getBooleanStreamed()
	{
		return booleanStreamed;
	}

	public void setBooleanStreamed(IBooleanInputSource booleanStreamed)
	{
		this.booleanStreamed = booleanStreamed;
	}

	public IDoubleInputSource getDoubleStreamed()
	{
		return doubleStreamed;
	}

	public void setDoubleStreamed(IDoubleInputSource doubleStreamed)
	{
		this.doubleStreamed = doubleStreamed;
	}

	public IFloatInputSource getFloatStreamed()
	{
		return floatStreamed;
	}

	public void setFloatStreamed(IFloatInputSource floatStreamed)
	{
		this.floatStreamed = floatStreamed;
	}

	public IIntInputSource getIntStreamed()
	{
		return intStreamed;
	}

	public void setIntStreamed(IIntInputSource intStreamed)
	{
		this.intStreamed = intStreamed;
	}

	public ILongInputSource getLongStreamed()
	{
		return longStreamed;
	}

	public void setLongStreamed(ILongInputSource longStreamed)
	{
		this.longStreamed = longStreamed;
	}

	public IStringInputSource getStringStreamed()
	{
		return stringStreamed;
	}

	public void setStringStreamed(IStringInputSource stringStreamed)
	{
		this.stringStreamed = stringStreamed;
	}
}
