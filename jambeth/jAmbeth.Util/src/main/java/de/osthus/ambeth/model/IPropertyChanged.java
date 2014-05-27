package de.osthus.ambeth.model;

import java.beans.PropertyChangeListener;

/**
 * A "PropertyChange" event gets fired whenever a bean changes a "bound" property. You can register a PropertyChangeListener with a source bean so as to be
 * notified of any bound property updates.
 */
public interface IPropertyChanged
{
	/**
	 * Registers a PropertyChangeListener to be notified of any bound property updates.
	 * 
	 * @param listener
	 *            The Listener to be registered
	 */
	void addPropertyChangeListener(PropertyChangeListener listener);

	/**
	 * Unregisters a PropertyChangeListener that has been registered before to be notified of any bound property updates.
	 * 
	 * @param listener
	 *            The Listener to be unregistered
	 */
	void removePropertyChangeListener(PropertyChangeListener listener);
}
