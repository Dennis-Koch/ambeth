using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Ambeth.Typeinfo
{
    public class TypeInfo : ITypeInfo
    {
        protected readonly String simpleName;

        public ITypeInfoItem[] Members { get; private set; }

        public Type RealType { get; private set; }

        protected readonly Type[] genericInterfaces;

        protected IDictionary<String, ITypeInfoItem> nameToMemberDict = new Dictionary<String, ITypeInfoItem>();

        protected IDictionary<String, ITypeInfoItem> nameToXmlMemberDict = new Dictionary<String, ITypeInfoItem>();

        public TypeInfo(Type realType)
        {
            RealType = realType;
            simpleName = realType.Name;

            List<Type> allBaseTypes = new List<Type>();
            foreach (Type interf in realType.GetInterfaces())
            {
                if (interf.IsGenericType)
                {
                    allBaseTypes.Add(interf.GetGenericTypeDefinition());
                }
            }
            while (realType != null)
            {
                if (realType.IsGenericType)
                {
                    allBaseTypes.Add(realType.GetGenericTypeDefinition());
                }
                realType = realType.BaseType;
            }
            genericInterfaces = allBaseTypes.ToArray();
        }

        public String SimpleName
        {
            get
            {
                return simpleName;
            }
        }

        public void PostInit(ITypeInfoItem[] members)
        {
            this.Members = members;

            for (int a = this.Members.Length; a-- > 0; )
            {
                ITypeInfoItem member = this.Members[a];
                nameToMemberDict.Add(member.Name, member);
                nameToXmlMemberDict.Add(member.XMLName, member);
            }
        }

        public ITypeInfoItem GetMemberByName(String memberName)
        {
            return DictionaryExtension.ValueOrDefault(nameToMemberDict, memberName);
        }

        public ITypeInfoItem GetMemberByXmlName(String xmlMemberName)
        {
            return DictionaryExtension.ValueOrDefault(nameToXmlMemberDict, xmlMemberName);
        }

        public bool DoesImplement(Type interfaceArgument)
        {
            if (interfaceArgument.IsAssignableFrom(RealType))
            {
                return true;
            }
            return genericInterfaces.Any(x =>
                x.Equals(interfaceArgument));
        }
    }
}
