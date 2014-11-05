using System;

// This is NOT the original Ambeth AutowiredAttribute. It was modified for Unit testing.
namespace De.Osthus.Ambeth.Ioc.Annotation
{
    [AttributeUsage(AttributeTargets.Field | AttributeTargets.Property, Inherited = false, AllowMultiple = false)]
    public class AutowiredAttribute : Attribute
    {
        public Type Value { get; set; }

        public String Name { get; set; }

        public bool Optional { get; set; }

        public AutowiredAttribute()
            : this(typeof(Object))
        {
            // Intended blank
        }

        public AutowiredAttribute(Type value)
        {
            Value = value;
            Name = "";// Default value
            Optional = false; // Default value
        }
    }
}
