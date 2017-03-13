package com.koch.ambeth.util.collections;

import java.util.List;

public class FastList<V>
{
	private final ListElem<V> anchor = new ListElem<V>();

	private ListElem<V> last;

	private int size = 0;

	public final void pushAllFrom(FastList<V> list)
	{
		ListElem<V> firstElem = list.popFirst();
		pushLast(firstElem);
	}

	public final void pushAllFrom(List<V> list)
	{
		for (int a = 0, size = list.size(); a < size; a++)
		{
			pushLast(new ListElem<V>(list.get(a)));
		}
	}

	public final void pushLast(final ListElem<V> pointer)
	{
		if (validateListHandle(pointer))
		{
			remove(pointer);
		}
		pointer.setListHandle(this);
		pointer.setNext(null);
		if (size > 0)
		{
			pointer.setPrev(last);
			last.setNext(pointer);
		}
		else
		{
			pointer.setPrev(null);
			anchor.setNext(pointer);
		}
		last = pointer;
		size++;
	}

	public final void pushFirst(final ListElem<V> pointer)
	{
		if (validateListHandle(pointer))
		{
			remove(pointer);
		}
		if (size == 0)
		{
			pushLast(pointer);
		}
		else
		{
			pointer.setListHandle(this);
			IListElem<V> anchorNext = anchor.getNext();
			pointer.setNext(anchorNext);
			pointer.setPrev(null);
			anchorNext.setPrev(pointer);
			anchor.setNext(pointer);
			size++;
		}
	}

	public final void insertAfter(final ListElem<V> insertElem, final ListElem<V> afterElem)
	{
		if (!validateListHandle(afterElem))
		{
			throw new IllegalArgumentException("'afterElem' is not a member of this list");
		}
		if (validateListHandle(insertElem))
		{
			remove(insertElem);
		}
		insertElem.setListHandle(this);
		insertElem.setPrev(afterElem);
		IListElem<V> afterElemNext = afterElem.getNext();
		insertElem.setNext(afterElemNext);
		if (afterElemNext != null)
		{
			afterElemNext.setPrev(insertElem);
		}
		else
		{
			last = insertElem;
		}
		afterElem.setNext(insertElem);
		size++;
	}

	public final void insertBefore(final ListElem<V> insertElem, final ListElem<V> beforeElem)
	{
		if (!validateListHandle(beforeElem))
		{
			throw new IllegalArgumentException("'beforeElem' is not a member of this list");
		}
		if (validateListHandle(insertElem))
		{
			remove(insertElem);
		}
		insertElem.listHandle = this;
		insertElem.next = beforeElem;
		ListElem<V> beforeElemPrev = beforeElem.prev;
		insertElem.prev = beforeElem.prev;
		if (beforeElemPrev != null)
		{
			beforeElemPrev.next = insertElem;
		}
		else
		{
			anchor.next = insertElem;
		}
		beforeElem.prev = insertElem;
		size++;
	}

	public final ListElem<V> popFirst()
	{
		ListElem<V> anchorNext = anchor.next;
		if (anchorNext != null)
		{
			ListElem<V> anchorNextNext = anchorNext.next;
			anchor.next = anchorNextNext;
			if (anchorNextNext != null)
			{
				anchorNextNext.prev = anchor;
			}
			else
			{
				last = null;
			}
			size--;
			cleanRelationToList(anchorNext);
			return anchorNext;
		}
		return null;
	}

	public final V popFirstItem()
	{
		ListElem<V> elem = popFirst();
		if (elem == null)
		{
			return null;
		}
		V value = elem.getElemValue();
		return value;
	}

	public final V popLastItem()
	{
		ListElem<V> elem = popLast();
		if (elem == null)
		{
			return null;
		}
		V value = elem.getElemValue();
		return value;
	}

	public final ListElem<V> popLast()
	{
		if (size > 0)
		{
			ListElem<V> elem = last;
			ListElem<V> lastPrev = elem.prev;
			if (lastPrev != null)
			{
				lastPrev.next = null;
				last = lastPrev;
			}
			else
			{
				anchor.next = null;
			}
			size--;
			cleanRelationToList(elem);
			return elem;
		}
		return null;
	}

	public final boolean hasValue(V obj)
	{
		ListElem<V> pointer = first();
		while (pointer != null)
		{
			if (pointer.getElemValue() == obj)
			{
				return true;
			}
			pointer = pointer.next;
		}
		return false;
	}

	public final ListElem<V> first()
	{
		return anchor.next;
	}

	public final ListElem<V> last()
	{
		return last;
	}

	public final int size()
	{
		return size;
	}

	public final boolean isEmpty()
	{
		return size == 0;
	}

	public final void clear()
	{
		ListElem<V> pointer = anchor.next;
		anchor.next = null;
		while (pointer != null)
		{
			ListElem<V> nextPointer = pointer.next;
			cleanRelationToList(pointer);
			pointer = nextPointer;
		}
		size = 0;
		last = null;
	}

	public static final <V> void switchElems(final IListElem<V> elem1, final IListElem<V> elem2)
	{
		V o = elem1.getElemValue();
		elem1.setElemValue(elem2.getElemValue());
		elem2.setElemValue(o);
	}

	protected final boolean validateListHandle(final IListElem<V> elem)
	{
		Object listHandle = elem.getListHandle();
		if (listHandle == null)
		{
			return false;
		}
		if (listHandle != this)
		{
			throw new IllegalArgumentException("'elem' is not a member of this list");
		}
		return true;
	}

	public final void remove(final V obj)
	{
		ListElem<V> pointer = first();
		while (pointer != null)
		{
			ListElem<V> nextPointer = pointer.next;
			if (pointer.getElemValue() == obj)
			{
				remove(pointer);
				return;
			}
			pointer = nextPointer;
		}
	}

	public final void remove(final ListElem<V> elem)
	{
		if (!validateListHandle(elem))
		{
			return;
		}
		ListElem<V> elemPrev = elem.prev;
		ListElem<V> elemNext = elem.next;

		if (elemPrev != null)
		{
			elemPrev.next = elemNext;
		}
		else
		{
			anchor.next = elemNext;
		}
		if (elemNext != null)
		{
			elemNext.prev = elemPrev;
		}
		else
		{
			last = elemPrev;
		}
		size--;
		cleanRelationToList(elem);
	}

	public final boolean hasListElem(final ListElem<V> listElem)
	{
		return listElem.getListHandle() == this;
	}

	protected void cleanRelationToList(final ListElem<V> listElem)
	{
		listElem.listHandle = null;
		listElem.prev = null;
		listElem.next = null;
	}
}