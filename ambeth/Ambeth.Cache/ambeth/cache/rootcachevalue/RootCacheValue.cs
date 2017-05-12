using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Proxy;
using System;

namespace De.Osthus.Ambeth.Cache.Rootcachevalue
{
    public abstract class RootCacheValue : AbstractCacheValue, IListElem<RootCacheValue>, IObjRefContainer
    {
	    protected Object listHandle;

	    public IListElem<RootCacheValue> Prev { get; set; }

        public IListElem<RootCacheValue> Next { get; set; }

	    protected RootCacheValue(IEntityMetaData metaData)
	    {
		    // Intended blank
	    }

        public ICache Get__Cache()
        {
            return null;
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

	    public ValueHolderState Get__State(int relationIndex)
	    {
		    return ValueHolderState.LAZY;
	    }

        public bool Is__Initialized(int relationIndex)
        {
            return false;
        }

	    public IObjRef[] Get__ObjRefs(int relationIndex)
	    {
		    return GetRelation(relationIndex);
	    }

	    public void Set__ObjRefs(int relationIndex, IObjRef[] objRefs)
	    {
		    SetRelation(relationIndex, objRefs);
	    }

        public abstract IEntityMetaData Get__EntityMetaData();

		public void Detach()
		{
			// intended blank
		}
    }
}