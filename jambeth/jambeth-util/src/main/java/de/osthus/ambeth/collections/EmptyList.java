package de.osthus.ambeth.collections;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Hochperformante Implementierung einer Liste, welche leer ist und nicht modifiziert werden kann. Der Sinn hierbei ist, dass bei R�ckgabewerten von Methoden,
 * bei dem man �blicherweise eine neue Instanz einer leeren Liste verwenden w�rde, fallspezifisch der Singleton dieser Klasse verwenden werden k�nnte.
 * 
 * Hierbei w�rde keinerlei Garbage entstehen und der einzige Funktionalit�tsverlust w�re die fehlende Modifizierbarkeit des R�ckgabewertes f�r den
 * Aufrufer. Analysen der h�ufigsten Patterns haben jedoch gezeigt, dass dieser theoretische Verlust sehr selten praktische Relevanz besitzt.
 * 
 * Die auf dieser Klasse aufrufbaren Methoden sind:
 * 
 * clear(), iterator(), isEmpty(), lastIndexOf(), contains(), containsAll(), listIterator(), size(), toArray()
 * 
 * Alle anderen f�hren zu einer UnsupportedOperationException
 * 
 * @author kochd
 * 
 * @param <V>
 *            Typ der Liste
 */
public class EmptyList<V> implements List<V>, IList<V>
{
	private static final ListIterator<Object> emptyIter = new ListIterator<Object>()
	{
		@Override
		public boolean hasNext()
		{
			return false;
		}

		@Override
		public Object next()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void add(Object e)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean hasPrevious()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public int nextIndex()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public Object previous()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public int previousIndex()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void set(Object e)
		{
			throw new UnsupportedOperationException();
		}
	};

	private static final Object[] emptyArray = new Object[0];

	public static final EmptyList<?> instance = new EmptyList<Object>();

	@SuppressWarnings("unchecked")
	public static final <V> EmptyList<V> getInstance()
	{
		return (EmptyList<V>) instance;
	}

	@SuppressWarnings("unchecked")
	public static final <V> EmptyList<V> createTypedEmptyList(Class<V> referenceClass)
	{
		return (EmptyList<V>) instance;
	}

	public EmptyList()
	{
	}

	@Override
	public boolean add(V e)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(int index, V element)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <T extends V> boolean addAll(T[] array)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection<? extends V> c)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(int index, Collection<? extends V> c)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear()
	{
	}

	@Override
	public boolean contains(Object o)
	{
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c)
	{
		return false;
	}

	@Override
	public V get(int index)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public int indexOf(Object o)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isEmpty()
	{
		return true;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Iterator<V> iterator()
	{
		return (Iterator<V>) emptyIter;
	}

	@Override
	public int lastIndexOf(Object o)
	{
		return -1;
	}

	@Override
	@SuppressWarnings("unchecked")
	public ListIterator<V> listIterator()
	{
		return (ListIterator<V>) emptyIter;
	}

	@Override
	public ListIterator<V> listIterator(int index)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(Object o)
	{
		return false;
	}

	@Override
	public V remove(int index)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c)
	{
		return false;
	}

	@Override
	public boolean retainAll(Collection<?> c)
	{
		return false;
	}

	@Override
	public V set(int index, V element)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public int size()
	{
		return 0;
	}

	@Override
	public IList<V> subList(int fromIndex, int toIndex)
	{
		return this;
	}

	@Override
	public Object[] toArray()
	{
		return emptyArray;
	}

	@Override
	public <T> T[] toArray(T[] a)
	{
		return a;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(final Class<T> componentType)
	{
		return (T[]) Array.newInstance(componentType, 0);
	}
}
