package com.koch.ambeth.util.collections;

/*-
 * #%L
 * jambeth-util
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import java.util.List;

public class InterfaceFastList<V> {
	public static class InterfaceFastListAnchor<V> implements IListElem<V> {
		protected IListElem<V> next;

		@Override
		public Object getListHandle() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setListHandle(Object listHandle) {
			throw new UnsupportedOperationException();
		}

		@Override
		public IListElem<V> getPrev() {
			return null;
		}

		@Override
		public void setPrev(IListElem<V> prev) {
			throw new UnsupportedOperationException();
		}

		@Override
		public IListElem<V> getNext() {
			return next;
		}

		@Override
		public void setNext(IListElem<V> next) {
			this.next = next;
		}

		@Override
		public V getElemValue() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setElemValue(V value) {
			throw new UnsupportedOperationException();
		}
	}

	private final IListElem<V> anchor = new InterfaceFastListAnchor<>();

	private IListElem<V> last;

	private int size = 0;

	public final boolean isEmpty() {
		return size == 0;
	}

	public final void pushAllFrom(InterfaceFastList<V> list) {
		while (true) {
			IListElem<V> firstElem = list.popFirst();
			if (firstElem == null) {
				return;
			}
			pushLast(firstElem);
		}
	}

	public final void pushAllFrom(FastList<V> list) {
		while (true) {
			ListElem<V> firstElem = list.popFirst();
			if (firstElem == null) {
				return;
			}
			pushLast(firstElem);
		}
	}

	public final void pushAllFrom(List<V> list) {
		for (int a = 0, size = list.size(); a < size; a++) {
			pushLast(new InterfaceListElem<>(list.get(a)));
		}
	}

	public final void pushAllFrom(IList<IListElem<V>> list) {
		for (int a = 0, size = list.size(); a < size; a++) {
			pushLast(list.get(a));
		}
	}

	public final void pushLast(final IListElem<V> elem) {
		if (validateListHandle(elem)) {
			IListElem<V> elemPrev = elem.getPrev();
			IListElem<V> elemNext = elem.getNext();

			if (elemPrev != null) {
				elemPrev.setNext(elemNext);
			}
			else {
				anchor.setNext(elemNext);
			}
			if (elemNext != null) {
				elemNext.setPrev(elemPrev);
			}
			else {
				last = elemPrev;
			}
			elem.setNext(null);
			if (size > 0) {
				elem.setPrev(last);
				last.setNext(elem);
			}
			else {
				elem.setPrev(null);
				anchor.setNext(elem);
			}
			last = elem;
			return;
		}
		elem.setListHandle(this);
		elem.setNext(null);
		if (size > 0) {
			elem.setPrev(last);
			last.setNext(elem);
		}
		else {
			elem.setPrev(null);
			anchor.setNext(elem);
		}
		last = elem;
		size++;
	}

	public final void pushFirst(final IListElem<V> elem) {
		if (validateListHandle(elem)) {
			if (size == 1) {
				return;
			}
			IListElem<V> elemPrev = elem.getPrev();
			IListElem<V> elemNext = elem.getNext();

			if (elemPrev != null) {
				elemPrev.setNext(elemNext);
			}
			else {
				anchor.setNext(elemNext);
			}
			if (elemNext != null) {
				elemNext.setPrev(elemPrev);
			}
			else {
				last = elemPrev;
			}
			IListElem<V> anchorNext = anchor.getNext();
			elem.setNext(anchorNext);
			elem.setPrev(null);
			anchorNext.setPrev(elem);
			anchor.setNext(elem);
			return;
		}
		if (size == 0) {
			pushLast(elem);
		}
		else {
			elem.setListHandle(this);
			IListElem<V> anchorNext = anchor.getNext();
			elem.setNext(anchorNext);
			elem.setPrev(null);
			anchorNext.setPrev(elem);
			anchor.setNext(elem);
			size++;
		}
	}

	public final void insertAfter(final IListElem<V> insertElem, final IListElem<V> afterElem) {
		if (!validateListHandle(afterElem)) {
			throw new IllegalArgumentException("'afterElem' is not a member of this list");
		}
		if (validateListHandle(insertElem)) {
			remove(insertElem);
		}
		insertElem.setListHandle(this);
		insertElem.setPrev(afterElem);
		IListElem<V> afterElemNext = afterElem.getNext();
		insertElem.setNext(afterElemNext);
		if (afterElemNext != null) {
			afterElemNext.setPrev(insertElem);
		}
		else {
			last = insertElem;
		}
		afterElem.setNext(insertElem);
		size++;
	}

	public final void insertBefore(final IListElem<V> insertElem, final IListElem<V> beforeElem) {
		if (!validateListHandle(beforeElem)) {
			throw new IllegalArgumentException("'beforeElem' is not a member of this list");
		}
		if (validateListHandle(insertElem)) {
			remove(insertElem);
		}
		insertElem.setListHandle(this);
		insertElem.setNext(beforeElem);
		IListElem<V> beforeElemPrev = beforeElem.getPrev();
		insertElem.setPrev(beforeElemPrev);
		if (beforeElemPrev != null) {
			beforeElemPrev.setNext(insertElem);
		}
		else {
			anchor.setNext(insertElem);
		}
		beforeElem.setPrev(insertElem);
		size++;
	}

	public final IListElem<V> popFirst() {
		IListElem<V> anchorNext = anchor.getNext();
		if (anchorNext != null) {
			IListElem<V> anchorNextNext = anchorNext.getNext();
			anchor.setNext(anchorNextNext);
			if (anchorNextNext != null) {
				anchorNextNext.setPrev(anchor);
			}
			else {
				last = null;
			}
			size--;
			cleanRelationToList(anchorNext);
			return anchorNext;
		}
		return null;
	}

	public final IListElem<V> popLast() {
		if (size > 0) {
			IListElem<V> elem = last;
			IListElem<V> lastPrev = elem.getPrev();
			if (lastPrev != null) {
				lastPrev.setNext(null);
				last = lastPrev;
			}
			else {
				anchor.setNext(null);
			}
			size--;
			cleanRelationToList(elem);
			return elem;
		}
		return null;
	}

	public final IListElem<V> first() {
		return anchor.getNext();
	}

	public final IListElem<V> last() {
		return last;
	}

	public final int size() {
		return size;
	}

	public final void clear() {
		IListElem<V> pointer = anchor.getNext();
		anchor.setNext(null);
		while (pointer != null) {
			IListElem<V> nextPointer = pointer.getNext();
			cleanRelationToList(pointer);
			pointer = nextPointer;
		}
		size = 0;
		last = null;
	}

	public static final <V> void switchElems(final IListElem<V> elem1, final IListElem<V> elem2) {
		V o = elem1.getElemValue();
		elem1.setElemValue(elem2.getElemValue());
		elem2.setElemValue(o);
	}

	public static final <V extends Comparable<V>> void insertOrdered(InterfaceFastList<V> list,
			IListElem<V> elemToInsert) {
		V value = elemToInsert.getElemValue();
		IListElem<V> pointer = list.first();
		while (pointer != null) {
			V existingDefEntry = pointer.getElemValue();
			if (existingDefEntry.compareTo(value) < 0) {
				// DefEntry is of higher priority
				list.insertBefore(elemToInsert, pointer);
				return;
			}
			pointer = pointer.getNext();
		}
		// DefEntry is of the least priority
		list.pushLast(elemToInsert);
	}

	protected final boolean validateListHandle(final IListElem<V> elem) {
		Object listHandle = elem.getListHandle();
		if (listHandle == null) {
			return false;
		}
		if (listHandle != this) {
			throw new IllegalArgumentException("'elem' is not a member of this list");
		}
		return true;
	}

	public final void remove(final IListElem<V> elem) {
		if (!validateListHandle(elem)) {
			return;
		}
		IListElem<V> elemPrev = elem.getPrev();
		IListElem<V> elemNext = elem.getNext();

		if (elemPrev != null) {
			elemPrev.setNext(elemNext);
		}
		else {
			anchor.setNext(elemNext);
		}
		if (elemNext != null) {
			elemNext.setPrev(elemPrev);
		}
		else {
			last = elemPrev;
		}
		size--;
		cleanRelationToList(elem);
	}

	public final boolean hasListElem(final IListElem<V> listElem) {
		return listElem.getListHandle() == this;
	}

	protected void cleanRelationToList(final IListElem<V> listElem) {
		listElem.setListHandle(null);
		listElem.setPrev(null);
		listElem.setNext(null);
	}
}
