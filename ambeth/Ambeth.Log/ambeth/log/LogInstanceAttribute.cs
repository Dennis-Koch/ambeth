using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace De.Osthus.Ambeth.Log
{
    [AttributeUsage(AttributeTargets.Field | AttributeTargets.Property, Inherited = false, AllowMultiple = false)]
    public class LogInstanceAttribute : Attribute
    {
	    public Type Value { get; protected set; }

        public LogInstanceAttribute()
        {
            // Intended blank
        }

        public LogInstanceAttribute(Type value)
        {
            this.Value = value;
        }
    }
}
