using System;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Xml.Typehandler;

namespace De.Osthus.Ambeth.Xml.Namehandler
{
    public class DateElementHandler : AbstractHandler, INameBasedHandler
    {
	    [LogInstance]
		public new ILogger Log { private get; set; }

	    public virtual bool WritesCustom(Object obj, Type type, IWriter writer)
	    {
		    if (!typeof(DateTime).Equals(type))
		    {
			    return false;
		    }

            int id = writer.AcquireIdForObject(obj);
            DateTime dateTime = (DateTime)obj;
            long utcTime = ConversionHelper.ConvertValueToType<long>(dateTime);
            String utcTimeString = utcTime.ToString();

		    writer.WriteStartElement("d");
            writer.WriteAttribute(XmlDictionary.IdAttribute, id.ToString());
            writer.WriteAttribute(XmlDictionary.ValueAttribute, utcTimeString);
		    writer.WriteEndElement();

		    return true;
	    }

        public virtual Object ReadObject(Type returnType, String elementName, int id, IReader reader)
	    {
            String utcTimeString = reader.GetAttributeValue(XmlDictionary.ValueAttribute);
            long utcTime = ConversionHelper.ConvertValueToType<long>(utcTimeString);
            DateTime utcDateTime = ConversionHelper.ConvertValueToType<DateTime>(utcTime);
            return utcDateTime;
	    }
    }
}