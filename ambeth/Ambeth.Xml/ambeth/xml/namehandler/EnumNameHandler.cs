using System;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Xml.Typehandler;

namespace De.Osthus.Ambeth.Xml.Namehandler
{
    public class EnumNameHandler : AbstractHandler, INameBasedHandler
    {
	    [LogInstance]
		public new ILogger Log { private get; set; }

	    public virtual bool WritesCustom(Object obj, Type type, IWriter writer)
	    {
		    if (!type.IsEnum)
		    {
			    return false;
		    }
		    writer.WriteStartElement(XmlDictionary.EnumElement);
		    int id = writer.AcquireIdForObject(obj);
		    writer.WriteAttribute(XmlDictionary.IdAttribute, id.ToString());
		    ClassElementHandler.WriteAsAttribute(type, writer);
		    writer.WriteAttribute(XmlDictionary.ValueAttribute, obj.ToString());
		    writer.WriteEndElement();
		    return true;
	    }

        public virtual Object ReadObject(Type returnType, String elementName, int id, IReader reader)
	    {
		    if (!XmlDictionary.EnumElement.Equals(elementName))
		    {
			    throw new Exception("Element '" + elementName + "' not supported");
		    }
		    Type enumType = ClassElementHandler.ReadFromAttribute(reader);

            String enumValue = reader.GetAttributeValue(XmlDictionary.ValueAttribute);
            if (enumValue == null)
            {
                throw new Exception("Element '" + elementName + "' invalid");
            }
            return ConversionHelper.ConvertValueToType(enumType, enumValue);
	    }
    }
}