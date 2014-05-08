package de.osthus.ambeth.collections;

public class GenericFastList<V extends AbstractListElem<V>>
{

	private final V anchor;

	private V last;

	private int size = 0;

	public GenericFastList(final V anchor)
	{
		this.anchor = anchor;
	}

	public GenericFastList(final Class<V> anchorClass)
	{
		try
		{
			anchor = anchorClass.newInstance();
		}
		catch (InstantiationException e)
		{
			throw new RuntimeException(e);
		}
		catch (IllegalAccessException e)
		{
			throw new RuntimeException(e);
		}
	}

	public final void pushLast(final V pointer)
	{
		pointer.next = null;
		if (size > 0)
		{
			pointer.prev = last;
			last.next = pointer;
		}
		else
		{
			pointer.prev = null;
			anchor.next = pointer;
		}
		last = pointer;
		size++;
	}

	public final void pushFirst(final V pointer)
	{
		if (size == 0)
		{
			pushLast(pointer);
		}
		else
		{
			pointer.next = anchor.next;
			pointer.prev = null;
			anchor.next.prev = pointer;
			anchor.next = pointer;
			size++;
		}
	}

	public final void insertAfter(final V insertElem, final V afterElem)
	{
		insertElem.prev = afterElem;
		insertElem.next = afterElem.next;
		if (afterElem.next != null)
		{
			afterElem.next.prev = insertElem;
		}
		else
		{
			last = insertElem;
		}
		afterElem.next = insertElem;
		size++;
	}

	public final void insertBefore(final V insertElem, final V beforeElem)
	{
		insertElem.next = beforeElem;
		insertElem.prev = beforeElem.prev;
		if (beforeElem.prev != null)
		{
			beforeElem.prev.next = insertElem;
		}
		else
		{
			anchor.next = insertElem;
		}
		beforeElem.prev = insertElem;
		size++;
	}

	public final V popFirst()
	{
		if (anchor.next != null)
		{
			V elem = anchor.next;
			anchor.next = anchor.next.next;
			if (anchor.next != null)
			{
				anchor.next.prev = anchor;
			}
			size--;
			return elem;
		}
		return null;
	}

	public final V popLast()
	{
		if (size > 0)
		{
			V elem = last;
			if (last.prev != null)
			{
				last.prev.next = null;
				last = last.prev;
			}
			else
			{
				anchor.next = null;
				last = anchor;
			}
			size--;
			return elem;
		}
		return null;
	}

	public final V getFirstElem()
	{
		return anchor.next;
	}

	public final V getLastElem()
	{
		return last != anchor ? last : null;
	}

	public final int size()
	{
		return size;
	}

	public final void clear()
	{
		anchor.next = null;
		size = 0;
		last = anchor;
	}

	public static final <V> void switchElems(final ListElem<V> elem1, final ListElem<V> elem2)
	{
		V o = elem1.value;
		elem1.value = elem2.value;
		elem2.value = o;
	}

	public final void remove(final V elem)
	{
		if (elem.prev != null)
		{
			elem.prev.next = elem.next;
		}
		else
		{
			anchor.next = elem.next;
		}
		if (elem.next != null)
		{
			elem.next.prev = elem.prev;
		}
		else
		{
			last = elem.prev;
		}
		elem.prev = null;
		elem.next = null;
		size--;
	}

	public final boolean hasListElem(final V listElem)
	{
		V pointer = getFirstElem();
		while (pointer != null)
		{
			if (pointer == listElem)
			{
				return true;
			}
			pointer = pointer.next;
		}
		return false;
	}
}