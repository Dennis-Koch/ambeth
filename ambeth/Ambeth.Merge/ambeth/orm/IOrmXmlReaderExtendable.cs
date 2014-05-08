using System;

namespace De.Osthus.Ambeth.Orm
{
    public interface IOrmXmlReaderExtendable
    {
        void RegisterOrmXmlReader(IOrmXmlReader reader, String version);

        void UnregisterOrmXmlReader(IOrmXmlReader reader, String version);
    }
}
