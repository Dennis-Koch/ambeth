using System;

namespace De.Osthus.Ambeth.Collections
{
    public class InterfaceFastListAnchor<V> : IListElem<V>
    {
        protected IListElem<V> next;

        public Object ListHandle
        {
            get
            {
                throw new NotSupportedException();
            }
            set
            {
                throw new NotSupportedException();
            }
        }

        public IListElem<V> Prev
        {
            get
            {
                return null;
            }
            set
            {
                throw new NotSupportedException();
            }
        }

        public IListElem<V> Next { get; set; }

        public V ElemValue
        {
            get
            {
                throw new NotSupportedException();
            }
            set
            {
                throw new NotSupportedException();
            }
        }
    }

    public class InterfaceFastList<V>
    {
        public static void InsertOrdered<T>(InterfaceFastList<T> list, IListElem<T> elemToInsert) where T : IComparable<T>
	    {
		    T value = elemToInsert.ElemValue;
		    IListElem<T> pointer = list.First;
		    while (pointer != null)
		    {
			    T existingDefEntry = pointer.ElemValue;
			    if (existingDefEntry.CompareTo(value) < 0)
			    {
				    // DefEntry is of higher priority
				    list.InsertBefore(elemToInsert, pointer);
				    return;
			    }
			    pointer = pointer.Next;
		    }
		    // DefEntry is of the least priority
		    list.PushLast(elemToInsert);
	    }

        private readonly IListElem<V> anchor = new InterfaceFastListAnchor<V>();

        private IListElem<V> last;

        private int size = 0;

        public void PushLast(IListElem<V> elem)
	    {
            if (ValidateListHandle(elem))
		    {
                IListElem<V> elemPrev = elem.Prev;
                IListElem<V> elemNext = elem.Next;

                if (elemPrev != null)
                {
                    elemPrev.Next = elemNext;
                }
                else
                {
                    anchor.Next = elemNext;
                }
                if (elemNext != null)
                {
                    elemNext.Prev = elemPrev;
                }
                else
                {
                    last = elemPrev;
                }
                elem.Next = null;
                if (size > 0)
                {
                    elem.Prev = last;
                    last.Next = elem;
                }
                else
                {
                    elem.Prev = null;
                    anchor.Next = elem;
                }
                last = elem;
                return;
		    }
            elem.ListHandle = this;
            elem.Next = null;
		    if (size > 0)
		    {
                elem.Prev = last;
                last.Next = elem;
		    }
		    else
		    {
                elem.Prev = null;
                anchor.Next = elem;
		    }
            last = elem;
		    size++;
	    }

        public void PushFirst(IListElem<V> elem)
	    {
            if (ValidateListHandle(elem))
		    {
                if (size == 1)
                {
                    return;
                }
                IListElem<V> elemPrev = elem.Prev;
                IListElem<V> elemNext = elem.Next;

                if (elemPrev != null)
                {
                    elemPrev.Next = elemNext;
                }
                else
                {
                    anchor.Next = elemNext;
                }
                if (elemNext != null)
                {
                    elemNext.Prev = elemPrev;
                }
                else
                {
                    last = elemPrev;
                }
                IListElem<V> anchorNext = anchor.Next;
                elem.Next = anchorNext;
                elem.Prev = null;
                anchorNext.Prev = elem;
                anchor.Next = elem;
                return;
		    }
		    if (size == 0)
		    {
                PushLast(elem);
		    }
		    else
		    {
                elem.ListHandle = this;
			    IListElem<V> anchorNext = anchor.Next;
                elem.Next = anchorNext;
                elem.Prev = null;
                anchorNext.Prev = elem;
                anchor.Next = elem;
			    size++;
		    }
	    }

	    public void InsertAfter(IListElem<V> insertElem, IListElem<V> afterElem)
	    {
		    if (!ValidateListHandle(afterElem))
		    {
			    throw new Exception("'afterElem' is not a member of this list");
		    }
		    if (ValidateListHandle(insertElem))
		    {
			    Remove(insertElem);
		    }
		    insertElem.ListHandle = this;
		    insertElem.Prev = afterElem;
		    IListElem<V> afterElemNext = afterElem.Next;
		    insertElem.Next = afterElemNext;
		    if (afterElemNext != null)
		    {
			    afterElemNext.Prev = insertElem;
		    }
		    else
		    {
			    last = insertElem;
		    }
		    afterElem.Next = insertElem;
		    size++;
	    }

	    public void InsertBefore(IListElem<V> insertElem, IListElem<V> beforeElem)
	    {
		    if (!ValidateListHandle(beforeElem))
		    {
			    throw new Exception("'beforeElem' is not a member of this list");
		    }
		    if (ValidateListHandle(insertElem))
		    {
			    Remove(insertElem);
		    }
		    insertElem.ListHandle = this;
		    insertElem.Next = beforeElem;
		    IListElem<V> beforeElemPrev = beforeElem.Prev;
		    insertElem.Prev = beforeElemPrev;
		    if (beforeElemPrev != null)
		    {
			    beforeElemPrev.Next = insertElem;
		    }
		    else
		    {
			    anchor.Next = insertElem;
		    }
		    beforeElem.Prev = insertElem;
		    size++;
	    }

	    public IListElem<V> PopFirst()
	    {
		    IListElem<V> anchorNext = anchor.Next;
		    if (anchorNext != null)
		    {
			    IListElem<V> anchorNextNext = anchorNext.Next;
			    anchor.Next = anchorNextNext;
			    if (anchorNextNext != null)
			    {
				    anchorNextNext.Prev = anchor;
			    }
                else
                {
                    last = null;
                }
			    size--;
                CleanRelationToList(anchorNext);
			    return anchorNext;
		    }
		    return null;
	    }

	    public IListElem<V> PopLast()
	    {
		    if (size > 0)
		    {
			    IListElem<V> elem = last;
			    IListElem<V> lastPrev = elem.Prev;
			    if (lastPrev != null)
			    {
				    lastPrev.Next = null;
				    last = lastPrev;
			    }
			    else
			    {
				    anchor.Next = null;
			    }
			    size--;
                CleanRelationToList(elem);
			    return elem;
		    }
		    return null;
	    }

	    public IListElem<V> First
	    {
            get
            {
                return anchor.Next;
            }
	    }

	    public IListElem<V> Last
	    {
            get
            {
                return last;
            }
	    }

	    public int Count
	    {
            get
            {
		        return size;
            }
	    }

	    public void Clear()
	    {
            IListElem<V> pointer = anchor.Next;
            anchor.Next = null;
            while (pointer != null)
            {
                IListElem<V> nextPointer = pointer.Next;
                CleanRelationToList(pointer);
                pointer = nextPointer;
            }
		    size = 0;
		    last = null;
	    }

	    public static void SwitchElems(IListElem<V> elem1, IListElem<V> elem2)
	    {
		    V o = elem1.ElemValue;
		    elem1.ElemValue = elem2.ElemValue;
		    elem2.ElemValue = o;
	    }

	    protected bool ValidateListHandle(IListElem<V> elem)
	    {
		    Object listHandle = elem.ListHandle;
		    if (listHandle == null)
		    {
			    return false;
		    }
		    if (listHandle != this)
		    {
			    throw new Exception("'elem' is not a member of this list");
		    }
		    return true;
	    }

	    public void Remove(IListElem<V> elem)
	    {
		    if (!ValidateListHandle(elem))
		    {
			    return;
		    }
		    IListElem<V> elemPrev = elem.Prev;
            IListElem<V> elemNext = elem.Next;

		    if (elemPrev != null)
		    {
			    elemPrev.Next = elemNext;
		    }
		    else
		    {
			    anchor.Next = elemNext;
		    }
		    if (elemNext != null)
		    {
			    elemNext.Prev = elemPrev;
		    }
		    else
		    {
			    last = elemPrev;
		    }
		    size--;
            CleanRelationToList(elem);
        }

	    public bool HasListElem(IListElem<V> listElem)
	    {
		    return listElem.ListHandle == this;
	    }

        protected void CleanRelationToList(IListElem<V> listElem)
	    {
		    listElem.ListHandle = null;
		    listElem.Prev = null;
		    listElem.Next = null;
	    }
    }
}