using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Merge.Model;
using System.Runtime.Serialization;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Typeinfo;
using System.Reflection;

namespace De.Osthus.Ambeth.Merge.Transfer
{
    [DataContract(Name = "EntityMetaDataTransfer")]
    public class EntityMetaDataTransfer
    {
        private static readonly short[] EmptyShortArray = new short[0];

        private static readonly ITypeInfoItem[] emptyTypeInfoItems = new ITypeInfoItem[0];

        [DataMember]
        public Type EntityType { get; set; }

        [DataMember]
        public String IdMemberName { get; set; }

        [DataMember]
        public String VersionMemberName { get; set; }

        [DataMember]
        public String CreatedByMemberName { get; set; }

        [DataMember]
        public String CreatedOnMemberName { get; set; }

        [DataMember]
        public String UpdatedByMemberName { get; set; }

        [DataMember]
        public String UpdatedOnMemberName { get; set; }

        [DataMember]
        public String[] AlternateIdMemberNames { get; set; }

        [DataMember]
        public String[] PrimitiveMemberNames { get; set; }

        [DataMember]
        public String[] RelationMemberNames { get; set; }

        [DataMember]
        public Type[] TypesRelatingToThis { get; set; }

        [DataMember]
        public Type[] TypesToCascadeDelete { get; set; }

        [DataMember]
        public int[][] AlternateIdMemberIndicesInPrimitives { get; set; }

        [DataMember]
        public String[] MergeRelevantNames { get; set; }

        public ITypeInfoItem IdMember
        {
            get
            {
                throw new NotSupportedException();
            }
        }

        public ITypeInfoItem GetIdMemberByIdIndex(byte idIndex)
        {
            throw new NotSupportedException();
        }

        public ITypeInfoItem VersionMember
        {
            get
            {
                throw new NotSupportedException();
            }
        }

        public ITypeInfoItem[] AlternateIdMembers
        {
            get
            {
                throw new NotSupportedException();
            }
        }

        public ITypeInfoItem CreatedOnMember
        {
            get
            {
                throw new NotSupportedException();
            }
        }

        public ITypeInfoItem CreatedByMember
        {
            get
            {
                throw new NotSupportedException();
            }
        }

        public ITypeInfoItem UpdatedOnMember
        {
            get
            {
                throw new NotSupportedException();
            }
        }

        public ITypeInfoItem UpdatedByMember
        {
            get
            {
                throw new NotSupportedException();
            }
        }

        public ITypeInfoItem[] PrimitiveMembers
        {
            get
            {
                throw new NotSupportedException();
            }
        }

        public ITypeInfoItem[] RelationMembers
        {
            get
            {
                throw new NotSupportedException();
            }
        }

        public bool IsMergeRelevant(ITypeInfoItem primitiveMember)
        {
            throw new NotSupportedException();
        }

        public ITypeInfoItem GetMemberByName(String memberName)
        {
            throw new NotSupportedException();
        }

        public int GetIndexByName(String memberName)
        {
            throw new NotSupportedException();
        }

        public bool IsCascadeDelete(Type other)
        {
            throw new NotSupportedException();
        }
    }
}