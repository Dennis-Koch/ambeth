using System;
using System.Collections.Generic;
using System.Text;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Xml.Typehandler;

namespace De.Osthus.Ambeth.Xml.Namehandler
{
    public class ClassNameHandler : AbstractHandler, INameBasedHandler
    {
        [ThreadStatic]
        private static StringBuilder sb;

        protected static StringBuilder StringBuilder
        {
            get
            {
                if (sb == null)
                {
                    sb = new StringBuilder();
                }
                return sb;
            }
        }

	    [LogInstance]
		public new ILogger Log { private get; set; }

	    public IProxyHelper ProxyHelper { protected get; set; }

        public ITypeInfoProvider TypeInfoProvider { protected get; set; }

        public IXmlTypeRegistry XmlTypeRegistry { protected get; set; }

	    public override void AfterPropertiesSet()
	    {
		    base.AfterPropertiesSet();

		    ParamChecker.AssertNotNull(ProxyHelper, "ProxyHelper");
            ParamChecker.AssertNotNull(TypeInfoProvider, "TypeInfoProvider");
            ParamChecker.AssertNotNull(XmlTypeRegistry, "XmlTypeRegistry");
	    }

        public void WriteAsAttribute(Type type, IWriter writer)
        {
            WriteAsAttribute(type, XmlDictionary.ClassIdAttribute, XmlDictionary.ClassNameAttribute, XmlDictionary.ClassNamespaceAttribute, XmlDictionary.ClassMemberAttribute, writer);
        }

        public void WriteAsAttribute(Type type, String classIdAttribute, String classNameAttribute, String classNamespaceAttribute, String classMemberAttribute, IWriter writer)
        {
            Type realType = ProxyHelper.GetRealType(type);
            int typeId = writer.GetIdOfObject(type);
            if (typeId > 0)
            {
                writer.WriteAttribute(classIdAttribute, typeId.ToString());
                return;
            }
            typeId = writer.AcquireIdForObject(realType);
            writer.WriteAttribute(classIdAttribute, typeId.ToString());

            StringBuilder sb = StringBuilder;
            try
            {
                while (type.IsArray)
                {
                    sb.Append("[]");
                    type = type.GetElementType();
                }
                String xmlName, xmlNamespace;
                IXmlTypeKey xmlType;
                Type genericArgumentType = null;
                if (type.IsGenericType && !typeof(Nullable<>).Equals(type.GetGenericTypeDefinition()))
                {
                    xmlType = XmlTypeRegistry.GetXmlType(type.GetGenericTypeDefinition());
                    genericArgumentType = type.GetGenericArguments()[0];
                }
                else
                {
                    xmlType = XmlTypeRegistry.GetXmlType(type);
                }
                xmlName = xmlType.Name;
                xmlNamespace = xmlType.Namespace;
                if (sb.Length > 0)
                {
                    sb.Insert(0, xmlName);
                    writer.WriteAttribute(classNameAttribute, sb.ToString());
                    sb.Length = 0;
                }
                else
                {
                    writer.WriteAttribute(classNameAttribute, xmlName);
                }
                writer.WriteAttribute(classNamespaceAttribute, xmlNamespace);
                if (genericArgumentType != null)
                {
                    WriteAsAttribute(genericArgumentType, "gti", "gn", "gns", "gm", writer);
                }
                if (!realType.IsArray && !realType.IsEnum && !realType.IsInterface && !typeof(Type).Equals(realType))
                {
                    ITypeInfo typeInfo = TypeInfoProvider.GetTypeInfo(type);
                    ITypeInfoItem[] members = typeInfo.Members;
                    List<ITypeInfoItem> membersToWrite = new List<ITypeInfoItem>(members.Length);
                    for (int a = 0, size = members.Length; a < size; a++)
                    {
                        ITypeInfoItem member = members[a];
                        if (member.IsXMLIgnore)
                        {
                            continue;
                        }
                        if (sb.Length > 0)
                        {
                            sb.Append(' ');
                        }
                        sb.Append(member.Name);
                        membersToWrite.Add(member);
                    }
                    if (sb.Length > 0)
                    {
                        writer.WriteAttribute(classMemberAttribute, sb.ToString());
                        sb.Length = 0;
                    }
                    writer.PutMembersOfType(type, ListUtil.ToArray(membersToWrite));
                }
            }
            finally
            {
                sb.Length = 0;
            }
        }

        public Type ReadFromAttribute(IReader reader)
        {
            return ReadFromAttribute(XmlDictionary.ClassIdAttribute, XmlDictionary.ClassNameAttribute, XmlDictionary.ClassNamespaceAttribute, XmlDictionary.ClassMemberAttribute, reader);
        }

	    public Type ReadFromAttribute(String classIdAttribute, String classNameAttribute, String classNamespaceAttribute, String classMemberAttribute, IReader reader)
	    {
            String classIdValue = reader.GetAttributeValue(classIdAttribute);
            int classId = 0;
            if (classIdValue != null && classIdValue.Length > 0)
            {
                classId = Int32.Parse(classIdValue);
            }
            Type typeObj = (Type)reader.GetObjectById(classId, false);
            if (typeObj != null)
            {
                return typeObj;
            }
            String name = reader.GetAttributeValue(classNameAttribute);
            String namespaceString = reader.GetAttributeValue(classNamespaceAttribute);

            int dimensionCount = 0;
            while (name.EndsWith("[]"))
            {
                dimensionCount++;
                name = name.Substring(0, name.Length - 2);
            }
            typeObj = XmlTypeRegistry.GetType(name, namespaceString);

            String classMemberValue = reader.GetAttributeValue(classMemberAttribute);
		    if (classMemberValue != null)
		    {
			    ITypeInfo typeInfo = TypeInfoProvider.GetTypeInfo(typeObj);
                String[] memberNames = classMemberValue.Split(' ');
			    ITypeInfoItem[] members = new ITypeInfoItem[memberNames.Length];

			    for (int a = memberNames.Length; a-- > 0;)
			    {
				    String memberName = memberNames[a];
				    ITypeInfoItem member = typeInfo.GetMemberByXmlName(memberName);
				    if (member == null)
				    {
					    throw new Exception("No member found with xml name '" + memberName + "' on entity '" + typeObj.FullName + "'");
				    }
                    members[a] = member;
			    }
			    reader.PutMembersOfType(typeObj, members);
		    }
            String gtIdValue = classIdAttribute.Equals("gti") ? null : reader.GetAttributeValue("gti");
            Type genericArgumentType = null;
            if (gtIdValue != null && gtIdValue.Length > 0)
            {
                genericArgumentType = ReadFromAttribute("gti", "gn", "gns", "gm", reader);
                typeObj = typeObj.MakeGenericType(genericArgumentType);
            }
            while (dimensionCount-- > 0)
            {
                typeObj = typeObj.MakeArrayType();
            }
            reader.PutObjectWithId(typeObj, classId);
            return typeObj;
	    }

        public virtual bool WritesCustom(Object obj, Type type, IWriter writer)
	    {
            if (!typeof(Type).IsAssignableFrom(type))
            {
                return false;
            }
		    Type typeObj = (Type) obj;
		    StringBuilder sb = null;
			while (typeObj.IsArray)
			{
                if (sb == null)
                {
                    sb = new StringBuilder();
                }
				sb.Append("[]");
				typeObj = typeObj.GetElementType();
			}
			String xmlName, xmlNamespace;
			IXmlTypeKey xmlType = XmlTypeRegistry.GetXmlType(typeObj);
			xmlName = xmlType.Name;
			xmlNamespace = xmlType.Namespace;

            int id = writer.AcquireIdForObject(obj);
            writer.WriteStartElement(XmlDictionary.ClassElement);
            writer.WriteAttribute(XmlDictionary.IdAttribute, id.ToString());
			if (sb != null && sb.Length > 0)
			{
				sb.Insert(0, xmlName);
				writer.WriteAttribute(XmlDictionary.ClassNameAttribute, sb.ToString());
			}
			else
			{
				writer.WriteAttribute(XmlDictionary.ClassNameAttribute, xmlName);
			}
			writer.WriteAttribute(XmlDictionary.ClassNamespaceAttribute, xmlNamespace);
            writer.WriteEndElement();
            return true;
	    }

        public virtual Object ReadObject(Type returnType, String elementName, int id, IReader reader)
	    {
            if (!XmlDictionary.ClassElement.Equals(elementName))
            {
                throw new Exception("Element '" + elementName + "' not supported");
            }
		    String name = reader.GetAttributeValue(XmlDictionary.ClassNameAttribute);
		    String namespaceString = reader.GetAttributeValue(XmlDictionary.ClassNamespaceAttribute);

		    int dimensionCount = 0;
		    while (name.EndsWith("[]"))
		    {
			    dimensionCount++;
			    name = name.Substring(0, name.Length - 2);
		    }
            Type typeObj = XmlTypeRegistry.GetType(name, namespaceString);
		    while (dimensionCount-- > 0)
		    {
                typeObj = typeObj.MakeArrayType();
		    }
		    return typeObj;
	    }
    }
}