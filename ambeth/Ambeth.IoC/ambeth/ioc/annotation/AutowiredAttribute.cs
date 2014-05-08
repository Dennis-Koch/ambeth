using System;

namespace De.Osthus.Ambeth.Ioc.Annotation
{
    [AttributeUsage(AttributeTargets.Field | AttributeTargets.Property, Inherited = false, AllowMultiple = false)]
    public class AutowiredAttribute : Attribute
    {
        public String Value { get; set; }

        public bool Optional { get; set; }

        public AutowiredAttribute() : this("")
        {
            // Intended blank
        }

        public AutowiredAttribute(String value)
        {
            Value = value;
            Optional = false; // Default value
        }
    }
}
