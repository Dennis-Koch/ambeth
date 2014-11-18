package demo.codeanalyzer.common.model;

import de.osthus.ambeth.collections.IList;

/**
 * Stores common attributes of a java class
 * 
 * @author Deepa Sobhana, Seema Richard
 */
public interface BaseJavaClassModel
{

	String getName();

	IList<Annotation> getAnnotations();

	boolean isPublic();

	boolean isProtected();

	boolean isFinal();

	boolean isNative();

	boolean isStatic();

	boolean isPrivate();

	boolean isAbstract();

	Location getLocationInfo();
}
