using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Xml.PostProcess;

namespace De.Osthus.Ambeth.Xml
{
    public class DefaultXmlWriter : IWriter, IPostProcessWriter
    {
        protected readonly IAppendable appendable;

        protected readonly ICyclicXmlController xmlController;

        protected readonly IDictionary<Object, int> mutableToIdMap = new IdentityDictionary<Object, int>();
        protected readonly IDictionary<Object, int> immutableToIdMap = new Dictionary<Object, int>();
        protected int nextIdMapIndex = 1;

        protected readonly IISet<Object> substitutedEntities = new IdentityHashSet<Object>();

        protected readonly IDictionary<Type, ITypeInfoItem[]> typeToMemberMap = new Dictionary<Type, ITypeInfoItem[]>();

        protected bool isInAttributeState = false;

        public IDictionary<Object, int> MutableToIdMap
        {
            get
            {
                return mutableToIdMap;
            }
        }

        public IDictionary<Object, int> ImmutableToIdMap
        {
            get
            {
                return immutableToIdMap;
            }
        }

        public IISet<Object> SubstitutedEntities
        {
            get
            {
                return substitutedEntities;
            }
        }

        public DefaultXmlWriter(IAppendable appendable, ICyclicXmlController xmlController)
        {
            this.appendable = appendable;
            this.xmlController = xmlController;
        }

        public void WriteEscapedXml(String unescapedString)
        {
            for (int a = 0, size = unescapedString.Length; a < size; a++)
            {
                char oneChar = unescapedString[a];
                switch (oneChar)
                {
                    case '&':
                        appendable.Append("&amp;");
                        break;
                    case '\"':
                        appendable.Append("&quot;");
                        break;
                    case '\'':
                        appendable.Append("&apos;");
                        break;
                    case '<':
                        appendable.Append("&lt;");
                        break;
                    case '>':
                        appendable.Append("&gt;");
                        break;
                    default:
                        appendable.Append(oneChar);
                        break;
                }
            }
        }

        public void WriteAttribute(String attributeName, Object attributeValue)
        {
            if (attributeValue == null)
            {
                return;
            }
            WriteAttribute(attributeName, attributeValue.ToString());
        }

        public void WriteAttribute(String attributeName, String attributeValue)
        {
            if (attributeValue == null || attributeValue.Length == 0)
            {
                return;
            }
            CheckIfInAttributeState();
            appendable.Append(' ').Append(attributeName).Append("=\"");
            WriteEscapedXml(attributeValue);
            appendable.Append('\"');
        }

        public void WriteEndElement()
        {
            CheckIfInAttributeState();
            appendable.Append("/>");
            isInAttributeState = false;
        }

        public void WriteCloseElement(String elementName)
        {
            if (isInAttributeState)
            {
                WriteEndElement();
                isInAttributeState = false;
            }
            else
            {
                appendable.Append("</").Append(elementName).Append('>');
            }
        }

        public void Write(String s)
        {
            appendable.Append(s);
        }

        public void Write(char s)
        {
            appendable.Append(s);
        }

        public void WriteOpenElement(String elementName)
        {
            EndTagIfInAttributeState();
            appendable.Append('<').Append(elementName).Append('>');
        }

        public void WriteStartElement(String elementName)
        {
            EndTagIfInAttributeState();
            appendable.Append('<').Append(elementName);
            isInAttributeState = true;
        }

        public void WriteStartElementEnd()
        {
            if (!isInAttributeState)
            {
                return;
            }
            CheckIfInAttributeState();
            appendable.Append('>');
            isInAttributeState = false;
        }

        public void WriteObject(Object obj)
        {
            xmlController.WriteObject(obj, this);
        }

        public void WriteEmptyElement(String elementName)
        {
            EndTagIfInAttributeState();
            appendable.Append('<').Append(elementName).Append("/>");
        }

        public int AcquireIdForObject(Object obj)
        {
            bool isImmutableType = ImmutableTypeSet.IsImmutableType(obj.GetType());
            IDictionary<Object, int> objectToIdMap = isImmutableType ? immutableToIdMap : mutableToIdMap;

            if (objectToIdMap.ContainsKey(obj))
            {
                throw new Exception("There is already a id mapped given object (" + obj + ")");
            }
            int id = nextIdMapIndex++;
            objectToIdMap.Add(obj, id);

            return id;
        }

        public int GetIdOfObject(Object obj)
        {
            bool isImmutableType = ImmutableTypeSet.IsImmutableType(obj.GetType());
            IDictionary<Object, int> objectToIdMap = isImmutableType ? immutableToIdMap : mutableToIdMap;

            return DictionaryExtension.ValueOrDefault(objectToIdMap, obj);
        }

        public void PutMembersOfType(Type type, ITypeInfoItem[] members)
        {
            typeToMemberMap.Add(type, members);
        }

        public ITypeInfoItem[] GetMembersOfType(Type type)
        {
            return DictionaryExtension.ValueOrDefault(typeToMemberMap, type);
        }

        public bool IsInAttributeState()
        {
            return isInAttributeState;
        }

        protected void CheckIfInAttributeState()
        {
            if (!IsInAttributeState())
            {
                throw new Exception("There is currently no pending open tag to attribute");
            }
        }

        protected void EndTagIfInAttributeState()
        {
            if (IsInAttributeState())
            {
                WriteStartElementEnd();
            }
        }

        public void AddSubstitutedEntity(Object entity)
        {
            substitutedEntities.Add(entity);
        }
    }
}