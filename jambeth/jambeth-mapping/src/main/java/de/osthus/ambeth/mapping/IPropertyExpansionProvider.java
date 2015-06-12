package de.osthus.ambeth.mapping;

public interface IPropertyExpansionProvider
{
	/**
	 * load the propertyExpansion for a given class and path
	 * 
	 * @param entityType
	 * @param propertyPath
	 *            starting at the entityType, separated with . (dots) (e.g. someRelation.someOtherRelation.someProperty
	 * @return
	 */
	PropertyExpansion getPropertyExpansion(Class<?> entityType, String propertyPath);
}
