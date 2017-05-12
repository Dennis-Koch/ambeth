using System;

namespace De.Osthus.Ambeth.Collections
{
    public class GenericFastList<V> where V : AbstractListElem<V>
    {
        private readonly V anchor = Activator.CreateInstance<V>();

        private V last;

        private int size = 0;

        public void PushLast(V pointer)
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

        public void PushFirst(V pointer)
        {
            if (size == 0)
            {
                PushLast(pointer);
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

        public void InsertAfter(V insertElem, V afterElem)
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

        public void InsertBefore(V insertElem, V beforeElem)
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

        public V First()
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

        public V Last()
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

        public V GetFirstElem()
        {
            return anchor.next;
        }

        public V GetLastElem()
        {
            return last != anchor ? last : null;
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
            anchor.next = null;
            size = 0;
            last = anchor;
        }

        public static void SwitchElems(ListElem<V> elem1, ListElem<V> elem2)
        {
            V o = elem1.ElemValue;
            elem1.ElemValue = elem2.ElemValue;
            elem2.ElemValue = o;
        }

        public void Remove(V elem)
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

        public bool HasListElem(V listElem)
        {
            V pointer = GetFirstElem();
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
}