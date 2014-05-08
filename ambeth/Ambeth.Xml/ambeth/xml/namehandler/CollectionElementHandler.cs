using System;
using System.Collections;
using System.Collections.Generic;
using System.Reflection;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Xml.Pending;
using De.Osthus.Ambeth.Xml.Typehandler;

namespace De.Osthus.Ambeth.Xml.Namehandler
{
    public class CollectionElementHandler : AbstractHandler, INameBasedHandler
    {
        [LogInstance]
        public new ILogger Log { private get; set; }

        public virtual ICommandBuilder CommandBuilder { protected get; set; }

        public virtual IXmlTypeRegistry XmlTypeRegistry { protected get; set; }

        public override void AfterPropertiesSet()
        {
            base.AfterPropertiesSet();

            ParamChecker.AssertNotNull(CommandBuilder, "CommandBuilder");
            ParamChecker.AssertNotNull(XmlTypeRegistry, "XmlTypeRegistry");
        }

        protected virtual Type GetComponentTypeOfCollection(Object obj)
        {
            Type[] genericArguments = obj.GetType().GetGenericArguments();
            if (genericArguments == null || genericArguments.Length != 1)
            {
                return typeof(Object);
            }
            else
            {
                return genericArguments[0];
            }
        }

        public virtual bool WritesCustom(Object obj, Type type, IWriter writer)
        {
            if (type.IsArray || !typeof(ICollection).IsAssignableFrom(type))
            {
                return false;
            }
            ICollection coll = (ICollection)obj;
            String collElement;
            int length = coll.Count;

            if (obj is IList)
            {
                collElement = XmlDictionary.ListElement;
            }
            else if (obj is ICollection)
            {
                collElement = XmlDictionary.SetElement;
            }
            else
            {
                throw new Exception("Collection of type '" + type.FullName + "' not supported");
            }
            writer.WriteStartElement(collElement);
            int id = writer.AcquireIdForObject(obj);
            writer.WriteAttribute(XmlDictionary.IdAttribute, id.ToString());
            writer.WriteAttribute(XmlDictionary.SizeAttribute, length.ToString());
            Type componentType = GetComponentTypeOfCollection(obj);
            ClassElementHandler.WriteAsAttribute(componentType, writer);
            writer.WriteStartElementEnd();
            if (obj is IList)
            {
                IList list = (IList)obj;
                for (int a = 0, size = list.Count; a < size; a++)
                {
                    Object item = list[a];
                    writer.WriteObject(item);
                }
            }
            else if (obj is IEnumerable)
            {
                IEnumerator enumerator = ((IEnumerable)obj).GetEnumerator();
                while (enumerator.MoveNext())
                {
                    Object item = enumerator.Current;
                    writer.WriteObject(item);
                }
            }
            writer.WriteCloseElement(collElement);
            return true;
        }

        public virtual Object ReadObject(Type returnType, String elementName, int id, IReader reader)
        {
            if (!XmlDictionary.SetElement.Equals(elementName) && !XmlDictionary.ListElement.Equals(elementName))
            {
                throw new Exception("Element '" + elementName + "' not supported");
            }
            String lengthValue = reader.GetAttributeValue(XmlDictionary.SizeAttribute);
            int length = lengthValue != null && lengthValue.Length > 0 ? Int32.Parse(lengthValue) : 0;
            // Do not remove although in Java it is not necessary to extract the generic type information of a collection.
            // This code is important for environments like C#
            Type componentType = ClassElementHandler.ReadFromAttribute(reader);

            if (returnType.IsGenericType)
            {
                componentType = returnType.GetGenericArguments()[0];
            }
            MethodInfo addMethod = null;
            Object[] parameters = new Object[1];
            IEnumerable coll;
            if (XmlDictionary.SetElement.Equals(elementName))
            {
                Type setType = typeof(HashSet<>).MakeGenericType(componentType);
                coll = (IEnumerable)Activator.CreateInstance(setType);
                addMethod = setType.GetMethod("Add");
            }
            else
            {
                Type listType = typeof(List<>).MakeGenericType(componentType);
                coll = (IEnumerable)(length > 0 ? Activator.CreateInstance(listType, length) : Activator.CreateInstance(listType));
                addMethod = listType.GetMethod("Add");
            }
            reader.PutObjectWithId(coll, id);
            reader.NextTag();
            bool useObjectFuture = false;
            ICommandBuilder commandBuilder = CommandBuilder;
            ICommandTypeRegistry commandTypeRegistry = reader.CommandTypeRegistry;
            while (reader.IsStartTag())
            {
                Object item = reader.ReadObject(componentType);
                if (item is IObjectFuture)
                {
                    IObjectFuture objectFuture = (IObjectFuture)item;
                    IObjectCommand command = commandBuilder.Build(commandTypeRegistry, objectFuture, coll, addMethod);
                    reader.AddObjectCommand(command);
                    useObjectFuture = true;
                }
                else if (useObjectFuture)
                {
                    IObjectCommand command = commandBuilder.Build(commandTypeRegistry, null, coll, addMethod, item);
                    reader.AddObjectCommand(command);
                }
                else
                {
                    parameters[0] = item;
                    addMethod.Invoke(coll, parameters);
                }
            }
            return coll;
        }
    }
}