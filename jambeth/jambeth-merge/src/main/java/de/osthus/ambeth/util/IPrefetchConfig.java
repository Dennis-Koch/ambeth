package de.osthus.ambeth.util;

public interface IPrefetchConfig
{
	/**
	 * Returns a stub looking like the requested entity type. All relations on this stub can be accessed (to-one/to-many) in a cascaded manner. The traversal is
	 * internally tracked as a prefetch path configuration. For to-many relations there is always exactly one entity stub in the collection available for valid
	 * traversal.<br>
	 * For example:<br>
	 * <code>
	 * IPrefetchConfig pc = prefetchHelper.createPrefetch();<br>
	 * pc.plan(MyEntity.class).getMyToOne().getMyToMany().get(0).getFunnyRel();<br>
	 * IPrefetchHandle ph = pc.build();<br>
	 * </code><br>
	 * Does exactly the same as:<br>
	 * <code>
	 * IPrefetchConfig pc = prefetchHelper.createPrefetch();<br>
	 * pc.add(MyEntity.class, "MyToOne.MyToMany.FunnyRel");<br>
	 * IPrefetchHandle ph = pc.build();<br>
	 * </code><br>
	 * The major difference is that the stub traversal is supported via code completion of our chosen IDE and eagerly compiled to detect typos immediately. The
	 * latter one could be loaded e.g. from a configuration file or generic string concatenation with ease.
	 * 
	 * @param entityType
	 *            The requested entity type to create a stub of
	 * @return A stub of the requested entity type
	 */
	<T> T plan(Class<T> entityType);

	IPrefetchConfig add(Class<?> entityType, String propertyPath);

	IPrefetchConfig add(Class<?> entityType, String... propertyPaths);

	IPrefetchHandle build();
}
