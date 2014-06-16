using System;

namespace De.Osthus.Ambeth.Config
{
    [AttributeUsage(AttributeTargets.Property)]
    public class PropertyAttribute : Attribute
    {
        public const String DEFAULT_VALUE = "##unspecified##";

        public PropertyAttribute() : this(DEFAULT_VALUE)
        {
            // intended blank
        }

        public PropertyAttribute(String name)
        {
            Name = name;
            Mandatory = true;
            DefaultValue = DEFAULT_VALUE;
        }

        public String Name { get; private set; }

        public bool Mandatory { get; set; }

        public String DefaultValue { get; set; }
    }
}
