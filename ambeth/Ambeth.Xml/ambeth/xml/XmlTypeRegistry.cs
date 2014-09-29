using System;
using System.Collections;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Ioc.Annotation;

namespace De.Osthus.Ambeth.Xml
{
    public class XmlTypeRegistry : IXmlTypeExtendable, IInitializingBean, IXmlTypeRegistry
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        [Autowired]
        public ILoggerHistory LoggerHistory { protected get; set; }

        protected Tuple2KeyHashMap<String, String, Type> xmlTypeToClassMap = new Tuple2KeyHashMap<String, String, Type>(0.5f);

        protected Dictionary<Type, IList<XmlTypeKey>> classToXmlTypeMap = new Dictionary<Type, IList<XmlTypeKey>>();

        protected readonly Lock readLock, writeLock;

        public XmlTypeRegistry()
        {
            ReadWriteLock rwLock = new ReadWriteLock();
            readLock = rwLock.ReadLock;
            writeLock = rwLock.WriteLock;
        }

        public virtual void AfterPropertiesSet()
        {
            RegisterXmlType(typeof(Boolean?), "BoolN", null);
            RegisterXmlType(typeof(Boolean), "Bool", null);
            RegisterXmlType(typeof(Char?), "CharN", null);
            RegisterXmlType(typeof(Char), "Char", null);
            RegisterXmlType(typeof(Byte?), "UByteN", null);
            RegisterXmlType(typeof(Byte), "UByte", null);
            RegisterXmlType(typeof(SByte?), "ByteN", null);
            RegisterXmlType(typeof(SByte), "Byte", null);
            RegisterXmlType(typeof(Int64?), "Int64N", null);
            RegisterXmlType(typeof(Int64), "Int64", null);
            RegisterXmlType(typeof(Int32?), "Int32N", null);
            RegisterXmlType(typeof(Int32), "Int32", null);
            RegisterXmlType(typeof(Int16?), "Int16N", null);
            RegisterXmlType(typeof(Int16), "Int16", null);
            RegisterXmlType(typeof(UInt64?), "UInt64N", null);
            RegisterXmlType(typeof(UInt64), "UInt64", null);
            RegisterXmlType(typeof(UInt32?), "UInt32N", null);
            RegisterXmlType(typeof(UInt32), "UInt32", null);
            RegisterXmlType(typeof(UInt16?), "UInt16N", null);
            RegisterXmlType(typeof(UInt16), "UInt16", null);
            RegisterXmlType(typeof(Single?), "Float32N", null);
            RegisterXmlType(typeof(Single), "Float32", null);
            RegisterXmlType(typeof(Double?), "Float64N", null);
            RegisterXmlType(typeof(Double), "Float64", null);
            RegisterXmlType(typeof(String), "String", null);
            RegisterXmlType(typeof(Object), "Object", null);
            RegisterXmlType(typeof(Type), "Class", null);
            RegisterXmlType(typeof(IList), "List", null);
            RegisterXmlType(typeof(IList<>), "ListG", null);
            RegisterXmlType(typeof(ObservableCollection<>), "ListG", null);
            RegisterXmlType(typeof(ISet<>), "SetG", null);
            RegisterXmlType(typeof(DateTime), "Date", null);
        }

        public Type GetType(String name, String namespaceString)
        {
            ParamChecker.AssertParamNotNull(name, "name");
            if (namespaceString == null)
            {
                namespaceString = String.Empty;
            }

            XmlTypeKey xmlTypeKey = new XmlTypeKey();
            xmlTypeKey.Name = name;
            xmlTypeKey.Namespace = namespaceString;

            readLock.Lock();
            try
            {
                Type type = xmlTypeToClassMap.Get(name, namespaceString);
                if (type == null)
                {
                    if (Log.DebugEnabled)
                    {
                        LoggerHistory.DebugOnce(Log, this, "XmlTypeNotFound: name=" + name + ", namespace=" + namespaceString);
                    }
                    return null;
                }
                return type;
            }
            finally
            {
                readLock.Unlock();
            }
        }

        public IXmlTypeKey GetXmlType(Type type)
        {
            return GetXmlType(type, true);
        }

        public IXmlTypeKey GetXmlType(Type type, bool expectExisting)
        {
            ParamChecker.AssertParamNotNull(type, "type");

            readLock.Lock();
            try
            {
                IList<XmlTypeKey> xmlTypeKeys = DictionaryExtension.ValueOrDefault(classToXmlTypeMap, type);
                if ((xmlTypeKeys == null || xmlTypeKeys.Count == 0) && expectExisting)
                {
                    throw new Exception("No xml type found: Type=" + type);
                }
                return xmlTypeKeys[0];
            }
            finally
            {
                readLock.Unlock();
            }
        }

        public void RegisterXmlType(Type type, String name, String namespaceString)
        {
            ParamChecker.AssertParamNotNull(type, "type");
            ParamChecker.AssertParamNotNull(name, "name");
            if (namespaceString == null)
            {
                namespaceString = String.Empty;
            }

            XmlTypeKey xmlTypeKey = new XmlTypeKey();
            xmlTypeKey.Name = name;
            xmlTypeKey.Namespace = namespaceString;

            writeLock.Lock();
            try
            {
                Type typeToSet = type;
                Type existingType = xmlTypeToClassMap.Get(name, namespaceString);
                if (existingType != null)
                {
                    if (type.IsAssignableFrom(existingType))
                    {
                        // Nothing else to to
                    }
                    else if (existingType.IsAssignableFrom(type))
                    {
                        typeToSet = existingType;
                    }
                    else if (typeof(IList<>).Equals(existingType) && typeof(ObservableCollection<>).Equals(type))
                    {
                        // Workaround for C# since the two collections are not assignable to eachother.
                        typeToSet = existingType;
                    }
                    else
                    {
                        throw new Exception("Error while registering '" + type.FullName + "': Unassignable type '" + existingType.FullName
                                + "' already registered for: Name='" + name + "' Namespace='" + namespaceString + "'");
                    }
                }
                xmlTypeToClassMap.Put(name, namespaceString, typeToSet);

                IList<XmlTypeKey> xmlTypeKeys = DictionaryExtension.ValueOrDefault(classToXmlTypeMap, type);
                if (xmlTypeKeys == null)
                {
                    xmlTypeKeys = new List<XmlTypeKey>();
                    classToXmlTypeMap.Add(type, xmlTypeKeys);
                }
                xmlTypeKeys.Add(xmlTypeKey);
            }
            finally
            {
                writeLock.Unlock();
            }
        }

        public void UnregisterXmlType(Type type, String name, String namespaceString)
        {
            ParamChecker.AssertParamNotNull(type, "type");
            ParamChecker.AssertParamNotNull(name, "name");
            if (namespaceString == null)
            {
                namespaceString = String.Empty;
            }

            XmlTypeKey xmlTypeKey = new XmlTypeKey();
            xmlTypeKey.Name = name;
            xmlTypeKey.Namespace = namespaceString;

            writeLock.Lock();
            try
            {
                xmlTypeToClassMap.Remove(xmlTypeKey.Name, xmlTypeKey.Namespace);

                IList<XmlTypeKey> xmlTypeKeys = classToXmlTypeMap[type];
                xmlTypeKeys.Remove(xmlTypeKey);
                if (xmlTypeKeys.Count == 0)
                {
                    classToXmlTypeMap.Remove(type);
                }
            }
            finally
            {
                writeLock.Unlock();
            }
        }
    }
}