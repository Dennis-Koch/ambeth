package de.osthus.ambeth.cache.rootcachevalue;

import de.osthus.ambeth.cache.AbstractCacheValue;
import de.osthus.ambeth.cache.ValueHolderState;
import de.osthus.ambeth.collections.IListElem;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.proxy.IObjRefContainer;

public abstract class RootCacheValue extends AbstractCacheValue implements IListElem<RootCacheValue>, IObjRefContainer
{
	protected Object listHandle;

	protected IListElem<RootCacheValue> next, prev;

	public RootCacheValue(IEntityMetaData metaData)
	{
		// Intended blank
	}

	public abstract void setPrimitives(Object[] primitives);

	public abstract IObjRef[][] getRelations();

	public abstract void setRelations(IObjRef[][] relations);

	public abstract IObjRef[] getRelation(int relationIndex);

	public abstract void setRelation(int relationIndex, IObjRef[] relationsOfMember);

	@Override
	public Object getListHandle()
	{
		return this.listHandle;
	}

	@Override
	public void setListHandle(Object listHandle)
	{
		if (this.listHandle != null && listHandle != null)
		{
			throw new UnsupportedOperationException();
		}
		this.listHandle = listHandle;
	}

	@Override
	public IListElem<RootCacheValue> getNext()
	{
		return next;
	}

	@Override
	public void setNext(IListElem<RootCacheValue> next)
	{
		this.next = next;
	}

	@Override
	public IListElem<RootCacheValue> getPrev()
	{
		return prev;
	}

	@Override
	public void setPrev(IListElem<RootCacheValue> prev)
	{
		this.prev = prev;
	}

	@Override
	public RootCacheValue getElemValue()
	{
		return this;
	}

	@Override
	public void setElemValue(RootCacheValue value)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public ValueHolderState get__State(int relationIndex)
	{
		return ValueHolderState.LAZY;
	}

	@Override
	public boolean is__Initialized(int relationIndex)
	{
		return false;
	}

	@Override
	public IObjRef[] get__ObjRefs(int relationIndex)
	{
		return getRelation(relationIndex);
	}

	@Override
	public void set__ObjRefs(int relationIndex, IObjRef[] objRefs)
	{
		setRelation(relationIndex, objRefs);
	}

	@Override
	public void set__Uninitialized(int relationIndex, IObjRef[] objRefs)
	{
		throw new UnsupportedOperationException();
	}
}
