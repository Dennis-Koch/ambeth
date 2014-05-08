package de.osthus.ambeth.objectcollector;

public interface IObjectCollector
{
	<T> T create(Class<T> type);

	void dispose(Object object);

	<T> void dispose(Class<T> type, T object);

	void cleanUp();

	/**
	 * Only use the result of this method within a controlled code block only.
	 * 
	 * - Do not give it as an argument to other methods - Do never save the result on a static or object member - In other words: Use it on the stack and never
	 * on the heap
	 * 
	 * @return
	 */
	IThreadLocalObjectCollector getCurrent();
}