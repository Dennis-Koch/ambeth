using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Merge.Model;
using System;

namespace De.Osthus.Ambeth.Cache.Rootcachevalue
{
    public abstract class RootCacheValue : AbstractCacheValue, IListElem<RootCacheValue>
    {
	    protected Object listHandle;

	    public IListElem<RootCacheValue> Prev { get; set; }

        public IListElem<RootCacheValue> Next { get; set; }

	    protected RootCacheValue(Type entityType)
	    {
		    // Intended blank
	    }

	    public abstract void SetPrimitives(Object[] primitives);

	    public abstract IObjRef[][] GetRelations();

	    public abstract void SetRelations(IObjRef[][] relations);

        public abstract IObjRef[] GetRelation(int relationIndex);

	    public abstract void SetRelation(int relationIndex, IObjRef[] relationsOfMember);

	    public Object ListHandle
	    {
            get
            {
		        return this.listHandle;
            }
            set
            {
                if (this.listHandle != null && value != null)
		        {
			        throw new NotSupportedException();
		        }
		        this.listHandle = value;
            }
	    }

	    public RootCacheValue ElemValue
	    {
            get
            {
		        return this;
            }
            set
            {
                throw new NotSupportedException();
            }
	    }
    }
}