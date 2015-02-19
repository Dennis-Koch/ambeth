package de.osthus.ambeth.util.converter;

import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.util.IDedicatedConverter;

public class BooleanArrayConverter implements IDedicatedConverter
{
	protected IThreadLocalObjectCollector objectCollector;

	public void setObjectCollector(IThreadLocalObjectCollector objectCollector)
	{
		this.objectCollector = objectCollector;
	}

	@Override
	public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value, Object additionalInformation)
	{
		if (boolean[].class.equals(sourceType) && (CharSequence.class.equals(expectedType) || String.class.equals(expectedType)))
		{
			boolean[] source = (boolean[]) value;
			IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
			StringBuilder sb = tlObjectCollector.create(StringBuilder.class);
			try
			{
				for (int a = 0, size = source.length; a < size; a++)
				{
					if (source[a])
					{
						sb.append('1');
					}
					else
					{
						sb.append('0');
					}
				}
				return sb.toString();
			}
			finally
			{
				tlObjectCollector.dispose(sb);
			}
		}
		else if (CharSequence.class.isAssignableFrom(sourceType) && boolean[].class.equals(expectedType))
		{
			CharSequence sValue = (CharSequence) value;
			boolean[] target = new boolean[sValue.length()];
			for (int a = 0, size = sValue.length(); a < size; a++)
			{
				char oneChar = sValue.charAt(a);
				switch (oneChar)
				{
					case '1':
					case 'T':
					case 't':
						target[a] = true;
						break;
					case '0':
					case 'F':
					case 'f':
						target[a] = false;
						break;
					default:
						throw new IllegalStateException("Character '" + oneChar + "' not supported");
				}
			}
			return target;
		}
		throw new IllegalStateException("Conversion " + sourceType.getName() + "->" + expectedType.getName() + " not supported");
	}
}
