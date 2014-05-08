using System;
using De.Osthus.Ambeth.Typeinfo;

namespace De.Osthus.Ambeth.Xml
{
    public interface IWriter
    {
        bool IsInAttributeState();

        void WriteEscapedXml(String unescapedString);

        void WriteAttribute(String attributeName, Object attributeValue);

        void WriteAttribute(String attributeName, String attributeValue);

        void WriteEndElement();

        void WriteCloseElement(String elementName);

        void Write(String s);

        void WriteOpenElement(String elementName);

        void WriteStartElement(String elementName);

        void WriteStartElementEnd();

        void WriteObject(Object obj);

        void WriteEmptyElement(String elementName);

        void Write(char s);

        int GetIdOfObject(Object obj);

        int AcquireIdForObject(Object obj);
                
	    void PutMembersOfType(Type type, ITypeInfoItem[] member);

	    ITypeInfoItem[] GetMembersOfType(Type type);

        void AddSubstitutedEntity(Object entity);
    }
}