using System.Xml.Linq;

namespace De.Osthus.Ambeth.Util.Xml
{
    public interface IXmlValidator
    {
        void Validate(XDocument doc);
    }
}
