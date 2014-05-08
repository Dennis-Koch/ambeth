using System;

namespace De.Osthus.Ambeth.Annotation
{
    public class CascadeAttribute : Attribute
    {
        public CascadeLoadMode Load { get; set; }

        public CascadeAttribute()
        {
            Load = CascadeLoadMode.DEFAULT; // Default value
        }
    }
}
