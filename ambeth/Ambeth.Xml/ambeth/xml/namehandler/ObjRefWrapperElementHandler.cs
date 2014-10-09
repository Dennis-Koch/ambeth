using System;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Merge.Transfer;
using De.Osthus.Ambeth.Service;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Xml.Pending;
using De.Osthus.Ambeth.Xml.Typehandler;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Metadata;

namespace De.Osthus.Ambeth.Xml.Namehandler
{
    public class ObjRefWrapperElementHandler : AbstractHandler, INameBasedHandler
    {
        [LogInstance]
        public new ILogger Log { private get; set; }

        protected const String idNameIndex = "ix";

        [Autowired]
        public IEntityMetaDataProvider EntityMetaDataProvider { protected get; set; }

        [Autowired]
        public IObjRefFactory ObjRefFactory { protected get; set; }

        [Autowired]
        public IObjRefHelper ObjRefHelper { protected get; set; }
        
        public bool WritesCustom(Object obj, Type type, IWriter writer)
        {
            if (SyncToAsyncUtil.IsLowLevelSerializationType(type))
            {
                // Those types can never be an entity
                return false;
            }
            IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(type, true);
            if (metaData == null)
            {
                return false;
            }

            int idValue = writer.GetIdOfObject(obj);
            if (idValue != 0)
            {
                writer.WriteStartElement(XmlDictionary.RefElement);
                writer.WriteAttribute(XmlDictionary.IdAttribute, idValue);
                writer.WriteEndElement();
            }
            else
            {
                writer.AddSubstitutedEntity(obj);
                IObjRef ori = ObjRefHelper.EntityToObjRef(obj, true);
                WriteOpenElement(ori, obj, writer);
                writer.WriteObject(ori.RealType);
                writer.WriteObject(ori.Id);
                writer.WriteObject(ori.Version);
                writer.WriteCloseElement(XmlDictionary.OriWrapperElement);
            }

            return true;
        }

        public Object ReadObject(Type returnType, String elementName, int id, IReader reader)
        {
            if (!XmlDictionary.OriWrapperElement.Equals(elementName))
            {
                throw new Exception("Element '" + elementName + "' not supported");
            }

            String idIndexValue = reader.GetAttributeValue(idNameIndex);
            sbyte idIndex = idIndexValue != null ? SByte.Parse(idIndexValue) : ObjRef.PRIMARY_KEY_INDEX;
            reader.NextTag();
            Type realType = (Type)reader.ReadObject();
            Object objId = reader.ReadObject();
            Object version = reader.ReadObject();

            IObjRef ori = ObjRefFactory.CreateObjRef(realType, idIndex, objId, version);

            Object obj = new ObjRefFuture(ori);

            return obj;
        }

        protected void WriteOpenElement(IObjRef ori, Object obj, IWriter writer)
        {
            writer.WriteStartElement(XmlDictionary.OriWrapperElement);
            int id = writer.AcquireIdForObject(obj);
            writer.WriteAttribute(XmlDictionary.IdAttribute, id);
            sbyte idIndex = ori.IdNameIndex;
            if (idIndex != ObjRef.PRIMARY_KEY_INDEX)
            {
                writer.WriteAttribute(idNameIndex, idIndex.ToString());
            }
            writer.WriteStartElementEnd();
        }
    }
}
