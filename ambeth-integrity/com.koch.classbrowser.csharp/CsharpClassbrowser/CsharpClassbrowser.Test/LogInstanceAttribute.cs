using System;

// This is NOT the original Ambeth LogInstanceAttribute. It was modified for Unit testing.
namespace De.Osthus.Ambeth.Log
{
    [AttributeUsage(AttributeTargets.Field | AttributeTargets.Property, Inherited = false, AllowMultiple = false)]
    public class LogInstanceAttribute : Attribute
    {
        public LogInstanceAttribute()
        {
            // Intended blank
        }
    }
}
