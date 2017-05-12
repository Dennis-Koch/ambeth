using System;
using De.Osthus.Ambeth.Log;

namespace De.Osthus.Ambeth.Xml.Typehandler
{
    public class NumberTypeHandler : AbstractHandler, ITypeBasedHandler
    {
	    [LogInstance]
		public new ILogger Log { private get; set; }

	    public virtual void WriteObject(Object obj, Type type, IWriter writer)
	    {
            if (type.IsPrimitive)
		    {
                String convertedValue = ConversionHelper.ConvertValueToType<String>(obj);
                writer.WriteAttribute(XmlDictionary.ValueAttribute, convertedValue);
                return;
		    }
		    writer.WriteAttribute(XmlDictionary.ValueAttribute, obj.ToString());
	    }

        public virtual Object ReadObject(Type returnType, Type objType, int id, IReader reader)
	    {
		    String value = reader.GetAttributeValue(XmlDictionary.ValueAttribute);
            if (objType.IsPrimitive)
            {
                return ConversionHelper.ConvertValueToType(objType, value);
            }
		    if (typeof(Double?).Equals(objType))
		    {
			    return new Nullable<Double>(ConversionHelper.ConvertValueToType<Double>(value));
		    }
		    else if (typeof(Int64?).Equals(objType))
		    {
			    return new Nullable<Int64>(ConversionHelper.ConvertValueToType<Int64>(value));
		    }
		    else if (typeof(Single?).Equals(objType))
		    {
			    return new Nullable<Single>(ConversionHelper.ConvertValueToType<Single>(value));
		    }
		    else if (typeof(Int32?).Equals(objType))
		    {
			    return new Nullable<Int32>(ConversionHelper.ConvertValueToType<Int32>(value));
		    }
		    else if (typeof(Int16?).Equals(objType))
		    {
			    return new Nullable<Int16>(ConversionHelper.ConvertValueToType<Int16>(value));
		    }
		    else if (typeof(Byte?).Equals(objType))
		    {
			    return new Nullable<Byte>(ConversionHelper.ConvertValueToType<Byte>(value));
		    }
            else if (typeof(SByte?).Equals(objType))
            {
                return new Nullable<SByte>(ConversionHelper.ConvertValueToType<SByte>(value));
            }
            else if (typeof(Boolean?).Equals(objType))
		    {
			    return new Nullable<Boolean>(ConversionHelper.ConvertValueToType<Boolean>(value));
		    }
		    else if (typeof(Char?).Equals(objType))
		    {
			    if (value.Length == 0)
			    {
                    return new Nullable<Char>('\0');
			    }
			    return new Nullable<Char>(value[0]);
		    }
		    throw new Exception("Type not supported: " + objType);
	    }
    }
}