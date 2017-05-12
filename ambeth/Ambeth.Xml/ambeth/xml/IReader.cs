using System;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Xml.Pending;

namespace De.Osthus.Ambeth.Xml
{
    public interface IReader
    {
        bool IsEmptyElement();

        String GetAttributeValue(String attributeName);

        Object ReadObject();

        Object ReadObject(Type returnType);

        String GetElementName();

        String GetElementValue();

        void NextTag();

        void NextToken();

        bool IsStartTag();

        void MoveOverElementEnd();

        Object GetObjectById(int id);

        Object GetObjectById(int id, bool checkExistence);

        void PutObjectWithId(Object obj, int id);

        void PutMembersOfType(Type type, ITypeInfoItem[] member);

        ITypeInfoItem[] GetMembersOfType(Type type);

        void AddObjectCommand(IObjectCommand pendingSetter);

        // IReader contains the registry because the reader in fact is the deserialization state.
        ICommandTypeRegistry CommandTypeRegistry { get; }

        ICommandTypeExtendable CommandTypeExtendable { get; }
    }
}
