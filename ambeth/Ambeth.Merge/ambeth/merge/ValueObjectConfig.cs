using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Ambeth.Merge
{
    public class ValueObjectConfig : IValueObjectConfig
    {
        public Type EntityType { get; set; }

        public Type ValueType { get; set; }

        protected ISet<String> ignoredMembers = new HashSet<String>();

        protected ISet<String> listTypeMembers = new HashSet<String>();

        protected IDictionary<String, ValueObjectMemberType?> valueObjectMemberTypes = new Dictionary<String, ValueObjectMemberType?>();

        protected IDictionary<String, Type> memberTypes = new Dictionary<String, Type>();

        protected IDictionary<String, String> boToVoMemberNameMap = new Dictionary<String, String>();

        public String GetValueObjectMemberName(String businessObjectMemberName)
        {
            String voMemberName = DictionaryExtension.ValueOrDefault(boToVoMemberNameMap, businessObjectMemberName);
            if (voMemberName == null)
            {
                return businessObjectMemberName;
            }
            else
            {
                return voMemberName;
            }
        }

        public void PutValueObjectMemberName(String businessObjectMemberName, String valueObjectMemberName)
        {
            if (!boToVoMemberNameMap.ContainsKey(businessObjectMemberName))
            {
                boToVoMemberNameMap.Add(businessObjectMemberName, valueObjectMemberName);
            }
            else
            {
                throw new InvalidOperationException("Mapping for member '" + businessObjectMemberName + "' already defined");
            }
        }

        public bool HoldsListType(String memberName)
        {
            return listTypeMembers.Contains(memberName);
        }

        public void AddListTypeMember(String memberName)
        {
            listTypeMembers.Add(memberName);
        }

        public ValueObjectMemberType GetValueObjectMemberType(String valueObjectMemberName)
        {
            ValueObjectMemberType? memberType = DictionaryExtension.ValueOrDefault(valueObjectMemberTypes, valueObjectMemberName);
            if (memberType == null)
            {
                memberType = ValueObjectMemberType.UNDEFINED;
            }
            return memberType.Value;
        }

        public void SetValueObjectMemberType(String valueObjectMemberName, ValueObjectMemberType memberType)
        {
            if (valueObjectMemberTypes.ContainsKey(valueObjectMemberName))
            {
                throw new Exception("Type entry for member '" + valueObjectMemberName + "' already exists");
            }
            valueObjectMemberTypes.Add(valueObjectMemberName, memberType);
        }

        public bool IsIgnoredMember(String valueObjectMemberName)
        {
            ValueObjectMemberType memberType = GetValueObjectMemberType(valueObjectMemberName);
            return memberType == ValueObjectMemberType.IGNORE;
        }

        public Type GetMemberType(String memberName)
        {
            return DictionaryExtension.ValueOrDefault(memberTypes, memberName);
        }

        public void PutMemberType(String memberName, Type elementType)
        {
            if (!memberTypes.ContainsKey(memberName))
            {
                memberTypes.Add(memberName, elementType);
            }
            else
            {
                throw new InvalidOperationException("Type for member '" + memberName + "' already defined");
            }
        }
    }
}
