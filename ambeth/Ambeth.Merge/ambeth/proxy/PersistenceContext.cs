using System;

namespace De.Osthus.Ambeth.Annotation
{
    [AttributeUsage(AttributeTargets.Class | AttributeTargets.Method, Inherited = false, AllowMultiple = false)]
    public class PersistenceContext : Attribute
    {
		PersistenceContextType Value { get; set; }
        
        public PersistenceContext() : this(PersistenceContextType.REQUIRED)
        {
			// intended blank
        }

		public PersistenceContext(PersistenceContextType value)
        {
			Value = value;
        }
	}
}