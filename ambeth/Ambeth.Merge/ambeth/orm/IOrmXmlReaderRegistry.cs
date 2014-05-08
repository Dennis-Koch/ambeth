using System;

namespace De.Osthus.Ambeth.Orm
{
    public interface IOrmXmlReaderRegistry
    {
        IOrmXmlReader GetOrmXmlReader(String version);
    }
}
