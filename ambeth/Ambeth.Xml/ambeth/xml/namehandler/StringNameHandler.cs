using System;
using System.Text;
using System.Text.RegularExpressions;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Xml.Typehandler;

namespace De.Osthus.Ambeth.Xml.Namehandler
{
    public class StringNameHandler : AbstractHandler, INameBasedHandler
    {
	    [LogInstance]
		public new ILogger Log { private get; set; }

        private static readonly String cdataStartSeq = "<![CDATA[";

    	private static readonly String cdataEndSeq = "]]>";

        protected static readonly Regex cdataPattern = new Regex("([\\s\\S]*?\\])(\\][\\s\\S]*)");

	    public virtual bool WritesCustom(Object obj, Type type, IWriter writer)
	    {
		    if (!typeof(String).Equals(type))
		    {
			    return false;
		    }
            String value = (String)obj;
            String stringElement = XmlDictionary.StringElement;
            writer.WriteStartElement(stringElement);
            int id = writer.AcquireIdForObject(obj);
            writer.WriteAttribute(XmlDictionary.IdAttribute, id.ToString());

            if (value.Length == 0)
            {
                writer.WriteEndElement();
                return true;
            }
            writer.WriteStartElementEnd();

            bool firstCdataElement = true;
            while (true)
            {
                Match matcher = cdataPattern.Match(value);
                if (!matcher.Success)
                {
                    if (!firstCdataElement)
                    {
                        writer.WriteStartElement("s");
                        writer.WriteStartElementEnd();
                    }
                    writer.Write(cdataStartSeq);
                    writer.Write(value);
                    writer.Write(cdataEndSeq);
                    if (!firstCdataElement)
                    {
                        writer.WriteCloseElement("s");
                    }
                    break;
                }
                firstCdataElement = false;

                String leftSeq = matcher.Groups[1].Value;
                String rightSeq = matcher.Groups[2].Value;
                writer.WriteStartElement("s");
                writer.WriteStartElementEnd();
                writer.Write(cdataStartSeq);
                writer.Write(leftSeq);
                writer.Write(cdataEndSeq);
                writer.WriteCloseElement("s");
                value = rightSeq;
            }
            writer.WriteCloseElement(stringElement);
            return true;
	    }

        public virtual Object ReadObject(Type returnType, String elementName, int id, IReader reader)
	    {
		    if (!XmlDictionary.StringElement.Equals(elementName))
		    {
			    throw new Exception("Element '" + elementName + "' not supported");
		    }
            if (reader.IsEmptyElement())
            {
                return String.Empty;
            }
            reader.NextToken();
    		StringBuilder sb = null;
			if (!reader.IsStartTag())
			{
				String value = reader.GetElementValue();
				reader.NextTag();
				if (value == null)
				{
					value = String.Empty;
				}
				return value;
			}
			while (reader.IsStartTag())
			{
				if (!"s".Equals(reader.GetElementName()))
				{
					throw new Exception("Element '" + elementName + "' not supported");
				}
				if (sb == null)
				{
					sb = new StringBuilder();
				}
				reader.NextToken();
				String textPart = reader.GetElementValue();
				sb.Append(textPart);
				reader.NextToken();
				reader.MoveOverElementEnd();
			}
			return sb.ToString();
	    }
    }
}