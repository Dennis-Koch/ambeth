using De.Osthus.Ambeth.Annotation;
using System;
using System.Runtime.Serialization;

namespace De.Osthus.Ambeth.Xml.Test.Transfer
{
    [XmlType]
    public class TestXmlObject
    {
        public virtual String ValueString { get; set; }

        public virtual long ValueLong { get; set; }

        public virtual int ValueInteger { get; set; }

        public virtual double ValueDouble { get; set; }

        public virtual float ValueFloat { get; set; }

        public virtual byte ValueByte { get; set; }

        public virtual char ValueCharacter { get; set; }

        public virtual bool ValueBoolean { get; set; }

        public virtual Int64? ValueLongN { get; set; }

        public virtual Int32? ValueIntegerN { get; set; }

        public virtual Double? ValueDoubleN { get; set; }

        public virtual Single? ValueFloatN { get; set; }

        public virtual Byte? ValueByteN { get; set; }

        public virtual Char? ValueCharacterN { get; set; }

        public virtual Boolean? ValueBooleanN { get; set; }

        [IgnoreDataMember]
        public virtual Object TransientField1 { get; set; }

        [IgnoreDataMember]
        public virtual Object TransientField2 { get; set; }

	    public override int GetHashCode()
        {
		    return 42; // The answer to the sense of life, the universe and everything else
	    }

        public override bool Equals(object obj)
        {
		    if (this == obj)
		    {
			    return true;
		    }
		    if (obj == null)
		    {
			    return false;
		    }
		    if (GetType() != obj.GetType())
		    {
			    return false;
		    }
		    TestXmlObject other = (TestXmlObject) obj;
		    if (ValueBoolean != other.ValueBoolean)
		    {
			    return false;
		    }
		    if (!NullableEquals(ValueBooleanN, other.ValueBooleanN))
		    {
			    return false;
		    }
		    if (ValueByte != other.ValueByte)
		    {
			    return false;
		    }
		    if (!NullableEquals(ValueByteN, other.ValueByteN))
		    {
			    return false;
		    }
		    if (ValueCharacter != other.ValueCharacter)
		    {
			    return false;
		    }
		    if (!NullableEquals(ValueCharacterN, other.ValueCharacterN))
		    {
			    return false;
		    }
		    if (ValueDouble != other.ValueDouble)
		    {
			    return false;
		    }
		    if (!NullableEquals(ValueDoubleN, other.ValueDoubleN))
		    {
			    return false;
		    }
		    if (ValueFloat != other.ValueFloat)
		    {
			    return false;
		    }
		    if (!NullableEquals(ValueFloatN, other.ValueFloatN))
		    {
			    return false;
		    }
		    if (ValueInteger != other.ValueInteger)
		    {
			    return false;
		    }
            if (!NullableEquals(ValueIntegerN, other.ValueIntegerN))
            {
                return false;
            }
		    if (ValueLong != other.ValueLong)
		    {
			    return false;
		    }
            if (!NullableEquals(ValueLongN, other.ValueLongN))
            {
                return false;
            }
            if (!Object.Equals(ValueString, other.ValueString))
		    {
			    return false;
		    }
		    return true;
	    }


        protected bool NullableEquals<T>(Nullable<T> left, Nullable<T> right) where T : struct
        {
            if (left.HasValue)
            {
                if (right.HasValue)
                {
                    return left.Value.Equals(right.Value);
                }
                return false;
            }
            else if (right.HasValue)
            {
                return false;
            }
            return true;
        }
    }
}