using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Util.Xml;
using System;
using System.Xml.Linq;

namespace De.Osthus.Ambeth.Orm
{
	public class OrmConfigGroupProvider : IOrmConfigGroupProvider
	{
		public static readonly String handleClearAllCachesEvent = "handleClearAllCachesEvent";

		[LogInstance]
		public ILogger Log { private get; set; }

		[Autowired]
		public IOrmXmlReaderRegistry ormXmlReaderRegistry { protected get; set; }

		[Autowired]
		public IXmlConfigUtil xmlConfigUtil { protected get; set; }

		protected readonly HashMap<String, IOrmConfigGroup> xmlFileNamesConfigGroupMap = new HashMap<String, IOrmConfigGroup>(0.5f);

		protected readonly Object writeLock = new Object();

		public void HandleClearAllCachesEvent(ClearAllCachesEvent evnt)
		{
			lock (writeLock)
			{
				xmlFileNamesConfigGroupMap.Clear();
			}
		}

		public IOrmConfigGroup GetOrmConfigGroup(String xmlFileNames)
		{
			lock (writeLock)
			{
				IOrmConfigGroup ormConfigGroup = xmlFileNamesConfigGroupMap.Get(xmlFileNames);
				if (ormConfigGroup != null)
				{
					return ormConfigGroup;
				}
			}
			LinkedHashSet<IEntityConfig> localEntities = new LinkedHashSet<IEntityConfig>();
			LinkedHashSet<IEntityConfig> externalEntities = new LinkedHashSet<IEntityConfig>();

			XDocument[] docs = xmlConfigUtil.ReadXmlFiles(xmlFileNames);
			foreach (XDocument doc in docs)
			{
				//doc.normalizeDocument();
				String documentNamespace = xmlConfigUtil.ReadDocumentNamespace(doc);
				IOrmXmlReader ormXmlReader = ormXmlReaderRegistry.GetOrmXmlReader(documentNamespace);
				ormXmlReader.LoadFromDocument(doc, localEntities, externalEntities);
			}
			lock (writeLock)
			{
				IOrmConfigGroup ormConfigGroup = xmlFileNamesConfigGroupMap.Get(xmlFileNames);
				if (ormConfigGroup != null)
				{
					return ormConfigGroup;
				}
				ormConfigGroup = new OrmConfigGroup(new LinkedHashSet<IEntityConfig>(localEntities), new LinkedHashSet<IEntityConfig>(externalEntities));
				xmlFileNamesConfigGroupMap.Put(xmlFileNames, ormConfigGroup);
				return ormConfigGroup;
			}
		}
	}
}