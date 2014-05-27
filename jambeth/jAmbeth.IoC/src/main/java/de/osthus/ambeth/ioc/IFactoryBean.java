package de.osthus.ambeth.ioc;

/**
 * Interface for a factory bean. The jAmbeth context does not return the bean itself if asked for it by name or autowiring but calls the
 * <code>getObject()</code> method.
 * <p>
 * For details please refer to the wiki article about the {@wiki osthus Ambeth_Bean#IFactoryBean IFactoryBean}.
 */
public interface IFactoryBean
{
	/**
	 * Getter for the real bean instance to be used for injections or as lookup result.
	 * 
	 * @return Real bean instance.
	 * @throws Throwable
	 */
	Object getObject() throws Throwable;
}
