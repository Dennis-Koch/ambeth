using System;
using De.Osthus.Ambeth.Annotation;
using System.Runtime.Serialization;
using System.Reflection;

namespace De.Osthus.Ambeth.Filter.Model
{
    [DataContract(Name = "SortDescriptor", Namespace = "http://schemas.osthus.de/Ambeth")]
    public class SortDescriptor : ISortDescriptor
    {
        [DataMember]
        public String Member { get; set; }

        [DataMember]
        public SortDirection SortDirection { get; set; }

        public SortDescriptor()
        {
            SortDirection = SortDirection.ASCENDING;
        }

        public SortDescriptor WithMember(String member)
        {
            Member = member;
            return this;
        }

        public SortDescriptor WithSortDirection(SortDirection sortDirection)
        {
            SortDirection = sortDirection;
            return this;
        }

        public override bool Equals(Object obj)
        {
            if (Object.ReferenceEquals(this, obj))
            {
                return true;
            }
            if (!(obj is SortDescriptor))
            {
                return false;
            }
            SortDescriptor compared = (SortDescriptor)obj;
            if (!Object.Equals(Member, compared.Member))
            {
                return false;
            }
            if (!Object.Equals(SortDirection, compared.SortDirection))
            {
                return false;
            }
            return true;
        }

        public override int GetHashCode()
        {
            return (Member.GetHashCode() ^ SortDirection.GetHashCode());
        }
    }
}