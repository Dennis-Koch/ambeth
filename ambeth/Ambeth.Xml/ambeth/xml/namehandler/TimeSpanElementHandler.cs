using System;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Xml.Typehandler;

namespace De.Osthus.Ambeth.Xml.Namehandler
{
    public class TimeSpanElementHandler : AbstractHandler, INameBasedHandler
    {
        [LogInstance]
		public new ILogger Log { private get; set; }

	    public virtual bool WritesCustom(Object obj, Type type, IWriter writer)
	    {
		    if (!typeof(TimeSpan).Equals(type))
		    {
			    return false;
		    }
            long millis = (long)((TimeSpan)obj).TotalMilliseconds;
		    writer.WriteStartElement(XmlDictionary.ObjectElement);
            ClassElementHandler.WriteAsAttribute(typeof(Int64?), writer);
		    writer.WriteAttribute(XmlDictionary.ValueAttribute, millis.ToString());
		    writer.WriteEndElement();
		    return true;
	    }

        public virtual Object ReadObject(Type returnType, String elementName, int id, IReader reader)
	    {
		    String spanString = reader.GetAttributeValue(XmlDictionary.ValueAttribute);
            long time = ConversionHelper.ConvertValueToType<long>(spanString);
            return TimeSpan.FromMilliseconds(time);
	    }
    }
}