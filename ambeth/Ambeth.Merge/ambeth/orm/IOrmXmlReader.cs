using System.Collections.Generic;
using System.Xml.Linq;

namespace De.Osthus.Ambeth.Orm
{
    public interface IOrmXmlReader
    {
		ISet<IEntityConfig> LoadFromDocument(XDocument doc);

		void LoadFromDocument(XDocument doc, ISet<IEntityConfig> localEntities, ISet<IEntityConfig> externalEntities);
    }
}
