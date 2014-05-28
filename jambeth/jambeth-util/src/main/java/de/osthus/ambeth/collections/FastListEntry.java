package de.osthus.ambeth.collections;

import de.osthus.ambeth.collections.LinkedIntKeyMap.LinkedIntKeyMapEntry;

public class FastListEntry<V> extends LinkedIntKeyMapEntry<FastListEntry<V>>
{
	private final ListElem<V> anchor = new ListElem<V>();

	private ListElem<V> last;

	private int size = 0;

	public final void pushLast(final ListElem<V> pointer)
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

	public final void pushFirst(final ListElem<V> pointer)
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

	public final void insertAfter(final ListElem<V> insertElem, final ListElem<V> afterElem)
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

	public final void insertBefore(final ListElem<V> insertElem, final ListElem<V> beforeElem)
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

	public final Object popFirst()
	{
		if (anchor.next != null)
		{
			ListElem<V> elem = anchor.next;
			anchor.next = anchor.next.next;
			if (anchor.next != null)
			{
				anchor.next.prev = anchor;
			}
			size--;
			Object o = elem.value;
			return o;
		}
		return null;
	}

	public final ListElem<V> popFirstElem()
	{
		if (anchor.next != null)
		{
			ListElem<V> elem = anchor.next;
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

	public final Object popFirstAndMoveTo(final FastListEntry<V> list)
	{
		if (anchor.next != null)
		{
			ListElem<V> elem = anchor.next;
			anchor.next = anchor.next.next;
			if (anchor.next != null)
			{
				anchor.next.prev = anchor;
			}
			size--;
			V o = elem.value;
			list.pushLast(elem);
			return o;
		}
		return null;
	}

	public final Object popLast()
	{
		if (size > 0)
		{
			ListElem<V> elem = last;
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
			Object o = elem.value;
			return o;
		}
		return null;
	}

	public final ListElem<V> popLastElem()
	{
		if (size > 0)
		{
			ListElem<V> elem = last;
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

	public final Object popLastAndMoveTo(final FastListEntry<V> list)
	{
		if (size > 0)
		{
			ListElem<V> elem = last;
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
			V o = elem.value;
			list.pushLast(elem);
			return o;
		}
		return null;
	}

	public final ListElem<V> getFirstElem()
	{
		return anchor.next;
	}

	public final ListElem<V> getLastElem()
	{
		return (last != anchor ? last : null);
	}

	public final int getSize()
	{
		return size;
	}

	public final void clear()
	{
		anchor.next = null;
		size = 0;
		last = anchor;
	}

	public final void clearAndDispose()
	{
		ListElem<V> pointer = anchor.next, buff;
		while (pointer != null)
		{
			buff = pointer.next;
			pointer = buff;
		}
		clear();
	}

	public static final <V> void switchElems(final ListElem<V> elem1, final ListElem<V> elem2)
	{
		V o = elem1.value;
		elem1.value = elem2.value;
		elem2.value = o;
	}

	public final boolean remove(final Object value)
	{
		ListElem<V> elem = anchor.next;
		while (elem != null)
		{
			if (elem.value == value)
			{
				remove(elem);
				return true;
			}
			elem = elem.next;
		}
		return false;
	}

	public final void remove(final ListElem<V> elem)
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
		size--;
	}

	public final void removeNoDispose(final ListElem<V> elem)
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
		size--;
	}

	public final ListElem<V> getElemAt(int index)
	{
		ListElem<V> pointer = getFirstElem();
		while (pointer != null)
		{
			if (index-- == 0)
			{
				return pointer;
			}
			pointer = pointer.next;
		}
		return null;
	}

	public final ListElem<V> findOneObject(final ListCompare listCompare)
	{
		ListElem<V> pointer = getFirstElem();
		while (pointer != null)
		{
			if (listCompare.isObject(pointer.value))
			{
				return pointer;
			}
			pointer = pointer.next;
		}
		return null;
	}

	public final void handleAllObjects(final ListCompare listCompare)
	{
		ListElem<V> pointer = getFirstElem(), buff;
		while (pointer != null)
		{
			buff = pointer.next;
			listCompare.handleObject(pointer.value);
			pointer = buff;
		}
	}

	public final boolean hasListElem(final ListElem<V> listElem)
	{
		ListElem<V> pointer = getFirstElem();
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

	public final boolean hasValue(final Object object)
	{
		ListElem<V> pointer = getFirstElem();
		while (pointer != null)
		{
			if (pointer.value == object)
			{
				return true;
			}
			pointer = pointer.next;
		}
		return false;
	}

	public final ListElem<V> hasValueGetListElem(final Object object)
	{
		ListElem<V> pointer = getFirstElem();
		while (pointer != null)
		{
			if (pointer.value == object)
			{
				return pointer;
			}
			pointer = pointer.next;
		}
		return null;
	}

	public final void pushAllFrom(final FastListEntry<V> otherList)
	{
		ListElem<V> pointer = otherList.getFirstElem(), buff;
		while (pointer != null)
		{
			buff = pointer.next;
			pushLast(pointer);
			pointer = buff;
		}
		otherList.clear();
	}
}