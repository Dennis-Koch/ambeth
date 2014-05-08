using System;
using System.Text;

namespace De.Osthus.Ambeth.Util.Converter
{
    public class BooleanArrayConverter : IDedicatedConverter
    {
        public Object ConvertValueToType(Type expectedType, Type sourceType, Object value, Object additionalInformation)
	    {
		    if (typeof(bool[]).Equals(sourceType) && typeof(String).Equals(expectedType))
		    {
			    bool[] source = (bool[]) value;
			    StringBuilder sb = new StringBuilder(source.Length);
				for (int a = 0, size = source.Length; a < size; a++)
				{
					if (source[a])
					{
						sb.Append('1');
					}
					else
					{
						sb.Append('0');
					}
				}
				return sb.ToString();
		    }
		    else if (typeof(String).Equals(sourceType) && typeof(bool[]).Equals(expectedType))
		    {
			    String sValue = (String) value;
			    bool[] target = new bool[sValue.Length];
                for (int a = 0, size = sValue.Length; a < size; a++)
			    {
				    char oneChar = sValue[a];
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
						    throw new Exception("Character '" + oneChar + "' not supported");
				    }
			    }
			    return target;
		    }
		    throw new Exception("Conversion " + sourceType.FullName + "->" + expectedType.FullName + " not supported");
	    }
    }
}