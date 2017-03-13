package com.koch.ambeth.cache;

import com.koch.ambeth.stream.IInputSourceTemplate;
import com.koch.ambeth.util.IImmutableType;

public class InputSourceTemplateFake implements IInputSourceTemplate, IImmutableType
{
	private final Object values;

	public InputSourceTemplateFake(Object values)
	{
		super();
		this.values = values;
	}

	public Object getValues()
	{
		return values;
	}
}
