using De.Osthus.Ambeth.Debug;
using System.Collections.Generic;
using System.Diagnostics;

namespace De.Osthus.Ambeth.Cache.Valueholdercontainer
{
    //[DebuggerTypeProxy(typeof(FlattenHierarchyProxy))]
    //[DebuggerTypeProxy(typeof(FlattenHierarchyProxy))]
    public class AbstractMaterial
    {
      //  [DebuggerBrowsable(DebuggerBrowsableState.Never)]
        private IList<MaterialType> types;

       // [DebuggerDisplay("Types1")]
       // [DebuggerBrowsable(DebuggerBrowsableState.Never)]
        public virtual IList<MaterialType> Types
        {
            get
            {
                return types;
            }
            set
            {
                types = value;
            }
        }
        
        protected AbstractMaterial()
        {
            // Intended blank
        }
    }
}