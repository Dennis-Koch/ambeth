using System;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Util;
using System.Text;

namespace De.Osthus.Ambeth.Xml
{
    public class XmlTypeKey : IPrintable, IXmlTypeKey
    {
        public String Name { get; set; }

        public String Namespace { get; set; }

        public override int GetHashCode()
        {
            if (this.Namespace == null)
            {
                return this.Name.GetHashCode();
            }
            return this.Name.GetHashCode() ^ this.Namespace.GetHashCode();
        }

        public override bool Equals(object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (!(obj is XmlTypeKey))
            {
                return false;
            }
            IXmlTypeKey other = (IXmlTypeKey)obj;
            return Object.Equals(this.Name, other.Name) && Object.Equals(this.Namespace, other.Namespace);
        }

        public override String ToString()
        {
            StringBuilder sb = new StringBuilder();
            ToString(sb);
            return sb.ToString();
        }

        public void ToString(StringBuilder sb)
        {
            sb.Append("XmlTypeKey: ").Append(this.Name);
            if (this.Namespace != null)
            {
                sb.Append(" ").Append(this.Namespace);
            }
        }
    }
}