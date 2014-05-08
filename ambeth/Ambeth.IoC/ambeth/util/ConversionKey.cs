using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Log;
using System;
using System.Text;

namespace De.Osthus.Ambeth.Util
{
    public class ConversionKey : IPrintable
    {
        public Type SourceType { get; set; }

        public Type TargetType { get; set; }

        public ConversionKey()
        {
        }

        public ConversionKey(Type sourceType, Type targetType)
        {
            this.SourceType = sourceType;
            this.TargetType = targetType;
        }

        public override int GetHashCode()
        {
            return (SourceType.GetHashCode() << 1) ^ TargetType.GetHashCode();
        }

        public override bool Equals(object obj)
        {
            if (obj == this)
            {
                return true;
            }
            if (obj == null || !GetType().Equals(obj.GetType()))
            {
                return false;
            }
            ConversionKey other = (ConversionKey)obj;
            return Object.Equals(this.SourceType, other.SourceType) && Object.Equals(this.TargetType, other.TargetType);
        }

        public override String ToString()
        {
            StringBuilder sb = new StringBuilder();
            ToString(sb);
            return sb.ToString();
        }

        public void ToString(StringBuilder sb)
        {
            sb.Append(GetType().Name).Append(": ").Append(SourceType.Name).Append("->").Append(TargetType.Name);
        }
    }
}