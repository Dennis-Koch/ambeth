using System;
using De.Osthus.Ambeth.Annotation;
using System.Runtime.Serialization;
using System.Collections.Generic;
using System.Reflection;

namespace De.Osthus.Ambeth.Filter.Model
{
    [DataContract(Name = "CompositeFilterDescriptor", Namespace = "http://schemas.osthus.de/Ambeth")]
    public class CompositeFilterDescriptor : IFilterDescriptor
    {
        [DataMember]
        public LogicalOperator LogicalOperator { get; set; }

        [DataMember]
        public IList<IFilterDescriptor> ChildFilterDescriptors { get; set; }

        public String Member
        {
            get
            {
                return null;
            }
        }

        public bool? IsCaseSensitive
        {
            get
            {
                return null;
            }
        }

        public FilterOperator Operator
        {
            get
            {
                return default(FilterOperator);
            }
        }

        public IList<String> Value
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
            if (!(obj is CompositeFilterDescriptor))
            {
                return false;
            }
            CompositeFilterDescriptor compared = (CompositeFilterDescriptor)obj;
            if (!Object.Equals(LogicalOperator, compared.LogicalOperator))
            {
                return false;
            }
            if (ChildFilterDescriptors == null || compared.ChildFilterDescriptors == null)
            {
                return (ChildFilterDescriptors == compared.ChildFilterDescriptors);
            }
            if (ChildFilterDescriptors.Count != compared.ChildFilterDescriptors.Count)
            {
                return false;
            }
            // ToDo in FilterDescriptorConverter: Ensure that two lists with equal descriptors do not have different orders
            for (int i = 0, length = ChildFilterDescriptors.Count; i < length; i++)
            {
                if (!Object.Equals(ChildFilterDescriptors[i],compared.ChildFilterDescriptors[i]))
                {
                    return false;
                }
            }
            return true;
        }

        public override int GetHashCode()
        {
	        if (ChildFilterDescriptors == null)
	        {
		        return LogicalOperator.GetHashCode();
	        }
			return LogicalOperator.GetHashCode() ^ ChildFilterDescriptors.Count;
        }
    }
}