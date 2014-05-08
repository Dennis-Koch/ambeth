using System.Collections.Generic;
using System.Xml.Linq;

namespace De.Osthus.Ambeth.Orm
{
    public interface IOrmXmlReader
    {
        ISet<EntityConfig> LoadFromDocument(XDocument doc);

        void LoadFromDocument(XDocument doc, ISet<EntityConfig> localEntities, ISet<EntityConfig> externalEntities);
    }
}
