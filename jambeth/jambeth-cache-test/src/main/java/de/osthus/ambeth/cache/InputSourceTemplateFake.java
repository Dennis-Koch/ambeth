package de.osthus.ambeth.cache;

import de.osthus.ambeth.stream.IInputSourceTemplate;
import de.osthus.ambeth.util.IImmutableType;

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
