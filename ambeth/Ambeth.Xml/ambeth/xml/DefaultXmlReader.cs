using System;
using System.Collections.Generic;
using System.Xml;
using De.Osthus.Ambeth.Ioc.Extendable;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Xml.Pending;
using De.Osthus.Ambeth.Xml.PostProcess;

namespace De.Osthus.Ambeth.Xml
{
    public class DefaultXmlReader : IReader, IPostProcessReader, ICommandTypeRegistry, ICommandTypeExtendable
    {
        protected readonly XmlReader xmlReader;

        protected readonly ICyclicXmlController xmlController;

        protected readonly IObjectFutureHandlerRegistry objectFutureHandlerRegistry;

        protected readonly IDictionary<int, Object> idToObjectMap = new Dictionary<int, Object>();

        protected readonly IDictionary<Type, ITypeInfoItem[]> typeToMemberMap = new Dictionary<Type, ITypeInfoItem[]>();

        protected readonly IList<IObjectCommand> objectCommands = new List<IObjectCommand>();

        protected readonly IMapExtendableContainer<Type, Type> commandTypeExtendable = new MapExtendableContainer<Type, Type>("Overriding command type", "Original command type");

        public DefaultXmlReader(XmlReader xmlReader, ICyclicXmlController xmlController, IObjectFutureHandlerRegistry objectFutureHandlerRegistry)
        {
            this.xmlReader = xmlReader;
            this.xmlController = xmlController;
            this.objectFutureHandlerRegistry = objectFutureHandlerRegistry;
        }

        public String GetAttributeValue(String attributeName)
        {
            return xmlReader.GetAttribute(attributeName, null);
        }

        public Object ReadObject()
        {
            Object obj = xmlController.ReadObject(this);
            return obj;
        }

        public Object ReadObject(Type returnType)
        {
            Object obj = xmlController.ReadObject(returnType, this);
            return obj;
        }

        public String GetElementName()
        {
            return xmlReader.Name;
        }

        public void NextToken()
        {
            xmlReader.Read();
        }

        public String GetElementValue()
        {
            return xmlReader.Value;
        }

        public bool IsEmptyElement()
        {
            return xmlReader.IsEmptyElement;
        }

        public void MoveOverElementEnd()
        {
            xmlReader.Read();
        }

        public void NextTag()
        {
            try
            {
                xmlReader.Read();
            }
            catch (Exception)
            {
                throw;
            }
        }

        public bool IsStartTag()
        {
            return xmlReader.IsStartElement();
        }

        public Object GetObjectById(int id)
        {
            return GetObjectById(id, true);
        }

        public Object GetObjectById(int id, bool checkExistence)
        {
            Object obj = DictionaryExtension.ValueOrDefault(idToObjectMap, id);
            if (obj == null && checkExistence)
            {
                throw new Exception("No object found in xml with id " + id);
            }
            return obj;
        }

        public void PutObjectWithId(Object obj, int id)
        {
            Object existingObj = DictionaryExtension.ValueOrDefault(idToObjectMap, id);
            if (existingObj != null)
            {
                if (existingObj != obj)
                {
                    throw new Exception("Already mapped object to id " + id + " found");
                }
                return;
            }
            idToObjectMap.Add(id, obj);
        }

        public void PutMembersOfType(Type type, ITypeInfoItem[] members)
        {
            typeToMemberMap.Add(type, members);
        }

        public ITypeInfoItem[] GetMembersOfType(Type type)
        {
            return DictionaryExtension.ValueOrDefault(typeToMemberMap, type);
        }

        public void AddObjectCommand(IObjectCommand objectCommand)
        {
            objectCommands.Add(objectCommand);
        }

        public ICommandTypeRegistry CommandTypeRegistry
        {
            get { return this; }
        }

        public ICommandTypeExtendable CommandTypeExtendable
        {
            get { return this; }
        }

        public Type GetOverridingCommandType(Type commandType)
        {
            return commandTypeExtendable.GetExtension(commandType);
        }

        public void RegisterOverridingCommandType(Type overridingCommandType, Type commandType)
        {
            commandTypeExtendable.Register(overridingCommandType, commandType);
        }

        public void UnregisterOverridingCommandType(Type overridingCommandType, Type commandType)
        {
            commandTypeExtendable.Unregister(overridingCommandType, commandType);
        }

        public void ExecuteObjectCommands()
        {
            while (objectCommands.Count != 0)
            {
                IObjectCommand[] commandSnapShot = ListUtil.ToArray(objectCommands);
                objectCommands.Clear();

                ResolveObjectFutures(commandSnapShot);

                // Commands have to be executed in-order (e.g. for CollectionSetterCommands)
                // except for MergeCommand which have to be last
                List<IObjectCommand> mergeCommands = new List<IObjectCommand>();
                foreach (IObjectCommand objectCommand in commandSnapShot)
                {
                    if (objectCommand is MergeCommand)
                    {
                        mergeCommands.Add(objectCommand);
                        continue;
                    }
                    objectCommand.Execute(this);
                }
                for (int i = 0, size = mergeCommands.Count; i < size; i++)
                {
                    IObjectCommand objectCommand = mergeCommands[i];
                    objectCommand.Execute(this);
                }
            }
        }

        protected void ResolveObjectFutures(IList<IObjectCommand> objectCommands)
        {
            IObjectFutureHandlerRegistry objectFutureHandlerRegistry = this.objectFutureHandlerRegistry;
            IDictionary<Type, ISet<IObjectFuture>> sortedObjectFutures = BucketSortObjectFutures(objectCommands);
            foreach (KeyValuePair<Type, ISet<IObjectFuture>> entry in sortedObjectFutures)
            {
                Type type = entry.Key;
                ISet<IObjectFuture> objectFutures = entry.Value;
                IObjectFutureHandler objectFutureHandler = objectFutureHandlerRegistry.GetObjectFutureHandler(type);
                if (objectFutureHandler == null)
                {
                    throw new Exception("No handler found for IObjectFutures of type '" + type.Name + "'");
                }
                IList<IObjectFuture> objectFutureList = new List<IObjectFuture>(objectFutures);
                objectFutureHandler.Handle(objectFutureList);
            }
        }

        protected IDictionary<Type, ISet<IObjectFuture>> BucketSortObjectFutures(IList<IObjectCommand> objectCommands)
        {
            IDictionary<Type, ISet<IObjectFuture>> sortedObjectFutures = new Dictionary<Type, ISet<IObjectFuture>>((int)(objectCommands.Count / 0.75));
            for (int i = 0, size = objectCommands.Count; i < size; i++)
            {
                IObjectCommand objectCommand = objectCommands[i];
                IObjectFuture objectFuture = objectCommand.ObjectFuture;
                if (objectFuture != null)
                {
                    Type type = objectFuture.GetType();
                    ISet<IObjectFuture> objectFutures = DictionaryExtension.ValueOrDefault(sortedObjectFutures, type);
                    if (objectFutures == null)
                    {
                        objectFutures = new HashSet<IObjectFuture>();
                        sortedObjectFutures.Add(type, objectFutures);
                    }
                    objectFutures.Add(objectFuture);
                }
            }

            return sortedObjectFutures;
        }

        public override String ToString()
        {
            if (IsStartTag())
            {
                if (IsEmptyElement())
                {
                    return GetElementName() + "|EMPTY";
                }
                return GetElementName() + "|START";
            }
            return GetElementName() + "|END";
        }
    }
}