package de.osthus.ambeth.collections;

public interface IListElem<V>
{
	Object getListHandle();

	void setListHandle(Object listHandle);

	IListElem<V> getPrev();

	void setPrev(IListElem<V> prev);

	IListElem<V> getNext();

	void setNext(IListElem<V> next);

	V getElemValue();

	void setElemValue(V elemValue);
}
