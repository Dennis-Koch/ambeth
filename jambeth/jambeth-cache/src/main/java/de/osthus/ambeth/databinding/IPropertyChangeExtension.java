package de.osthus.ambeth.databinding;


public interface IPropertyChangeExtension
{
	void propertyChanged(Object obj, String propertyName, Object oldValue, Object currentValue);
}
