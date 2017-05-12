using System;
using System.Collections.Generic;
using System.Text;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge;

namespace De.Osthus.Ambeth.Xml
{
    public class XmlTypeHelper : IXmlTypeHelper
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        protected readonly HashMap<String, Type> xmlNameToType = new HashMap<String, Type>(0.5f);

        protected readonly Object writeLock = new Object();

        [Autowired]
        public IEntityMetaDataProvider EntityMetaDataProvider { protected get; set; }

        public String GetXmlName(Type valueObjectType)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append(GetXmlNamespace(valueObjectType));
            if (sb.Length > 0)
            {
                sb.Append('/');
            }
            sb.Append(GetXmlTypeName(valueObjectType));

            return sb.ToString();
        }

        public String GetXmlNamespace(Type valueObjectType)
        {
            //valueObjectType.get
            // TODO
            throw new NotSupportedException();
            //Package voPackage = valueObjectType.getPackage();
            //XmlSchema packageAnnotation = voPackage.getAnnotation(XmlSchema.class);
            //if (packageAnnotation != null)
            //{
            //    return packageAnnotation.namespace_();
            //}
            //else if (Log.WarnEnabled)
            //{
            //    log.Warn("No 'XmlSchema' annotation found on package '" + voPackage.getName() + "'");
            //}
            //return "";
        }

        public String GetXmlTypeName(Type valueObjectType)
        {
            // TODO
            throw new NotSupportedException();
            //XmlType typeAnnotation = valueObjectType.getAnnotation(XmlType.class);
            //if (typeAnnotation != null)
            //{
            //    return typeAnnotation.name();
            //}
            //else
            //{
            //    if (log.isWarnEnabled())
            //    {
            //        log.warn("No 'XmlType' annotation found on class '" + valueObjectType.getName() + "'");
            //    }
            //    return valueObjectType.getSimpleName();
            //}
        }

        public Type GetType(String xmlName)
        {
            Object writeLock = this.writeLock;
            lock (writeLock)
            {
                Type type = xmlNameToType.Get(xmlName);
                if (type != null)
                {
                    return type;
                }
                BuildXmlNamesToTypeMap();

                type = xmlNameToType.Get(xmlName);

                if (type != null)
                {
                    return type;
                }
                throw new ArgumentException("One or more of this xml type names are not mappable to an entity type: " + xmlName);
            }
        }


        public Type[] GetTypes(IList<String> xmlNames)
        {
            Type[] types;
            Object writeLock = this.writeLock;
            lock (writeLock)
            {
                types = GetTypesIntern(xmlNames);
                if (types != null)
                {
                    return types;
                }
                BuildXmlNamesToTypeMap();

                types = GetTypesIntern(xmlNames);

                if (types == null)
                {
                    String unmappables = FindUnmappables(xmlNames);
                    throw new ArgumentException("One or more of this xml type names are not mappable to an entity type: " + unmappables);
                }

                return types;
            }
        }

        protected Type[] GetTypesIntern(IList<String> xmlNames)
        {
            Type[] types = new Type[xmlNames.Count];

            for (int i = xmlNames.Count; i-- > 0; )
            {
                String xmlName = xmlNames[i];
                Type type = xmlNameToType.Get(xmlName);
                if (type == null)
                {
                    return null;
                }
                types[i] = type;
            }
            return types;
        }

        /**
         * Just for giving a helpful exception message.
         * 
         * @param xmlNames
         *            All xml type names given.
         * @return Listing of unresolvable names.
         */
        protected String FindUnmappables(IList<String> xmlNames)
        {
            StringBuilder sb = new StringBuilder();
            String separator = "";

            for (int i = xmlNames.Count; i-- > 0; )
            {
                String xmlName = xmlNames[i];
                Type type = xmlNameToType.Get(xmlName);
                if (type != null)
                {
                    continue;
                }
                sb.Append(separator).Append(xmlName);
                separator = ", ";
            }

            return sb.ToString();
        }

        protected void BuildXmlNamesToTypeMap()
        {
            IMap<String, Type> xmlNameToType = this.xmlNameToType;
            xmlNameToType.Clear();
            IList<Type> mappableEntityTypes = EntityMetaDataProvider.FindMappableEntityTypes();
            for (int i = mappableEntityTypes.Count; i-- > 0; )
            {
                Type entityType = mappableEntityTypes[i];
                Type valueObjectType = GetUniqueValueObjectType(entityType);
                String xmlName = GetXmlName(valueObjectType);
                xmlNameToType.Put(xmlName, valueObjectType);
            }
        }

        protected Type GetUniqueValueObjectType(Type entityType)
        {
            IList<Type> targetValueObjectTypes = EntityMetaDataProvider.GetValueObjectTypesByEntityType(entityType);
            if (targetValueObjectTypes.Count > 1)
            {
                throw new Exception("Entity type '" + entityType.FullName
                        + "' has more than 1 mapped value type. Autoresolving value type is not possible. Currently this feature is not supported");
            }
            return targetValueObjectTypes[0];
        }
    }
}