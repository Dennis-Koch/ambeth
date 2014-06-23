using System;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge.Transfer;
using De.Osthus.Ambeth.Xml.Typehandler;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Merge;

namespace De.Osthus.Ambeth.Xml.Namehandler
{
    public class ObjRefElementHandler : AbstractHandler, INameBasedHandler
    {
        protected static readonly String idNameIndex = "ix";

        [LogInstance]
		public new ILogger Log { private get; set; }

        [Autowired]
        public IEntityMetaDataProvider EntityMetaDataProvider { protected get; set; }

        public virtual bool WritesCustom(Object obj, Type type, IWriter writer)
        {
            if (!typeof(ObjRef).Equals(type))
            {
                return false;
            }
            ObjRef ori = (ObjRef)obj;
            WriteOpenElement(ori, writer);
            writer.WriteObject(ori.RealType);
            writer.WriteObject(ori.Id);
            writer.WriteObject(ori.Version);
            writer.WriteCloseElement(XmlDictionary.EntityRefElement);
            return true;
        }

        public virtual Object ReadObject(Type returnType, String elementName, int id, IReader reader)
        {
            if (!XmlDictionary.EntityRefElement.Equals(elementName))
            {
                throw new Exception("Element '" + elementName + "' not supported");
            }

            String idIndexValue = reader.GetAttributeValue(idNameIndex);
		    sbyte idIndex = idIndexValue != null ? SByte.Parse(idIndexValue) : ObjRef.PRIMARY_KEY_INDEX;
		    reader.NextTag();
		    Type realType = (Type) reader.ReadObject();
		    Object objId = reader.ReadObject();
            Object version = reader.ReadObject();

            if (objId != null || version != null)
            {
                IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(realType, true);
                if (metaData != null)
                {
                    if (objId != null)
                    {
                        ITypeInfoItem idMember = metaData.GetIdMemberByIdIndex(idIndex);
                        if (objId.Equals(idMember.NullEquivalentValue))
                        {
                            objId = null;
                        }
                    }
                    if (version != null)
                    {
                        ITypeInfoItem versionMember = metaData.VersionMember;
                        if (versionMember != null)
                        {
                            if (version.Equals(versionMember.NullEquivalentValue))
                            {
                                version = null;
                            }
                        }
                    }
                }
            }

            ObjRef obj = new ObjRef(realType, idIndex, objId, version);

		    return obj;
        }

        protected void WriteOpenElement(ObjRef ori, IWriter writer)
        {
            writer.WriteStartElement(XmlDictionary.EntityRefElement);
            int id = writer.AcquireIdForObject(ori);
            writer.WriteAttribute(XmlDictionary.IdAttribute, id.ToString());
            sbyte idIndex = ori.IdNameIndex;
            if (idIndex != ObjRef.PRIMARY_KEY_INDEX)
            {
                writer.WriteAttribute(idNameIndex, idIndex.ToString());
            }
            writer.WriteStartElementEnd();
        }
    }
}