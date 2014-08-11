using System;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Util;
using System.Collections.Generic;
using System.Collections;
using System.Runtime.Serialization;
using De.Osthus.Ambeth.Annotation;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Threading;
using De.Osthus.Ambeth.Ioc.Annotation;

namespace De.Osthus.Ambeth.Xml
{

    public class XmlTransferScanner : IInitializingBean, IStartingBean, IDisposableBean
    {
        [LogInstance]
		public ILogger Log { private get; set; }

        public const String DefaultNamespace = "http://schemas.osthus.de/Ambeth";

        [Autowired(Optional = true)]
        public IClasspathScanner ClasspathScanner { protected get; set; }

        [Autowired]
        public IEntityMetaDataProvider EntityMetaDataProvider { protected get; set; }

        [Autowired(Optional = true)]
        public IThreadPool ThreadPool { protected get; set; }

        [Autowired]
        public IXmlTypeExtendable XmlTypeExtendable { protected get; set; }

        protected IList<Type> rootElementClasses;

        protected readonly List<IBackgroundWorkerDelegate> unregisterRunnables = new List<IBackgroundWorkerDelegate>();

        public virtual void AfterPropertiesSet()
        {
            if (ClasspathScanner == null)
		    {
			    if (Log.InfoEnabled)
			    {
				    Log.Info("Skipped scanning for XML transfer types. Reason: No instance of " + typeof(IClasspathScanner).FullName + " resolved");
			    }
			    return;
		    }
            IList<Type> rootElementClasses = ClasspathScanner.ScanClassesAnnotatedWith(typeof(DataContractAttribute), typeof(System.Xml.Serialization.XmlTypeAttribute), typeof(XmlTypeAttribute));
            if (Log.InfoEnabled)
            {
                Log.Info("Found " + rootElementClasses.Count + " classes annotated as XML transfer types");
            }
            if (Log.DebugEnabled)
            {
                List<Type> sorted = new List<Type>(rootElementClasses);
                sorted.Sort(delegate(Type left, Type right)
                {
                    return left.FullName.CompareTo(right.FullName);
                });
                for (int a = 0, size = sorted.Count; a < size; a++)
                {
                    Log.Debug("Xml entity found: " + sorted[a].Namespace + "." + sorted[a].Name);
                }
            }
            for (int a = rootElementClasses.Count; a-- > 0; )
            {
                Type rootElementClass = rootElementClasses[a];
                String name;
                String namespaceString;

                XmlTypeAttribute genericXmlType = AnnotationUtil.GetAnnotation<XmlTypeAttribute>(rootElementClass, false);
                if (genericXmlType != null)
                {
                    name = genericXmlType.Name;
                    namespaceString = genericXmlType.Namespace;
                }
                else
                {
                    DataContractAttribute dataContract = AnnotationUtil.GetAnnotation<DataContractAttribute>(rootElementClass, false);
                    if (dataContract != null)
                    {
                        name = dataContract.Name;
                        namespaceString = dataContract.Namespace;
                    }
                    else
                    {
                        System.Xml.Serialization.XmlTypeAttribute xmlTypeAttribute = AnnotationUtil.GetAnnotation<System.Xml.Serialization.XmlTypeAttribute>(rootElementClass, false);
                        name = xmlTypeAttribute.TypeName;
                        namespaceString = xmlTypeAttribute.Namespace;
                    }
                }
                if (DefaultNamespace.Equals(namespaceString))
                {
                    namespaceString = null;
                }
                if (name == null)
                {
                    name = rootElementClass.Name;
                }
                XmlTypeExtendable.RegisterXmlType(rootElementClass, name, namespaceString);
                unregisterRunnables.Add(delegate()
			    {
                    XmlTypeExtendable.UnregisterXmlType(rootElementClass, name, namespaceString);
			    });
            }
            this.rootElementClasses = rootElementClasses;
        }

        public virtual void AfterStarted()
        {
            // Eager fetch all meta data. Even if some of the classes are NOT an entity this is not a problem
            if (ThreadPool != null)
            {
                ThreadPool.Queue(FetchMetaData);
            }
            else
            {
                FetchMetaData();
            }
        }

        protected void FetchMetaData()
        {
            IList<Type> types = new List<Type>();
            foreach (Type type in rootElementClasses)
            {
                if (type.IsInterface || ImmutableTypeSet.IsImmutableType(type))
                {
                    continue;
                }
                types.Add(type);
            }
            EntityMetaDataProvider.GetMetaData(types);
        }

        public virtual void Destroy()
        {
            for (int a = unregisterRunnables.Count; a-- > 0; )
            {
                unregisterRunnables[a].Invoke();
            }
        }
    }
}