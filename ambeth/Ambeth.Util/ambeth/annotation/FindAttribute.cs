using System;

namespace De.Osthus.Ambeth.Annotation
{
    [AttributeUsage(AttributeTargets.Method, Inherited = false, AllowMultiple = false)]
    public class FindAttribute : Attribute
    {
        public Type EntityType { get; set; }

        public String QueryName { get; set; }

        public QueryResultType ResultType { get; set; }

        public FindAttribute()
        {
            EntityType = typeof(Object); // Default value
            QueryName = ""; // Default value
            ResultType = QueryResultType.REFERENCES; // Default value
        }        
    }
}
