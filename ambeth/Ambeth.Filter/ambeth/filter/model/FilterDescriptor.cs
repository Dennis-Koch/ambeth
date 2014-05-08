using System;
using De.Osthus.Ambeth.Annotation;
using System.Runtime.Serialization;
using System.Collections.Generic;
using System.Reflection;

namespace De.Osthus.Ambeth.Filter.Model
{
    /**
 * The FilterDescriptor is used for querying filtered results
 * 
 * <p>
 * Java class for FilterDescriptorType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 */
    [DataContract(Name = "FilterDescriptor", Namespace = "http://schemas.osthus.de/Ambeth")]
    public class FilterDescriptor : IFilterDescriptor
    {
        [DataMember]
        public String Member { get; set; }

        [DataMember]
        public IList<String> Value { get; set; }

        [DataMember]
        public bool? IsCaseSensitive { get; set; }

        [DataMember]
        public FilterOperator Operator { get; set; }

        public FilterDescriptor WithMember(String member)
        {
            Member = member;
            return this;
        }

        public FilterDescriptor WithValue(String value)
        {
            if (Value == null)
            {
                Value = new List<String>();
            }
            Value.Add(value);
            return this;
        }

        public FilterDescriptor WithCaseSensitive(bool? caseSensitive)
        {
            IsCaseSensitive = caseSensitive;
            return this;
        }

        public FilterDescriptor WithOperator(FilterOperator filterOperator)
        {
            Operator = filterOperator;
            return this;
        }

        [IgnoreDataMember]
        public LogicalOperator LogicalOperator
        {
            get
            {
                return default(LogicalOperator);
            }
        }

        [IgnoreDataMember]
        public IList<IFilterDescriptor> ChildFilterDescriptors
        {
            get
            {
                return null;
            }
        }

        public override bool Equals(Object obj)
        {
            if (Object.ReferenceEquals(this, obj))
            {
                return true;
            }
            if (!(obj is FilterDescriptor))
            {
                return false;
            }
            FilterDescriptor compared = (FilterDescriptor)obj;
            if (Value != null)
            {
                if (compared.Value == null)
                {
                    return false;
                }
                int count = Value.Count;
                if (count != compared.Value.Count)
                {
                    return false;
                }
                for (int i = 0; i < count; ++i)
                {
                    if (!Object.Equals(Value[i], compared.Value[i]))
                    {
                        return false;
                    }
                }
            }
            else if (compared.Value != null)
            {
                return false;
            }
            if (!Object.Equals(Operator, compared.Operator))
            {
                return false;
            }
            if (!Object.Equals(Member, compared.Member))
            {
                return false;
            }
            if (!Object.Equals(IsCaseSensitive, compared.IsCaseSensitive))
            {
                return false;
            }
            return true;
        }

        public override int GetHashCode()
        {
            int hashCode = GetType().GetHashCode();
            if (Member != null)
            {
                hashCode ^= Member.GetHashCode();
            }
            if (Value != null)
            {
                for (int i = 0, length = Value.Count; i < length; ++i)
                {
                    hashCode ^= Value[i].GetHashCode();
                }
            }
            return hashCode;
        }
    }
}