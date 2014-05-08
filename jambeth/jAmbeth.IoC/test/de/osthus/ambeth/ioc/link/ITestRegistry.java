package de.osthus.ambeth.ioc.link;

import java.beans.PropertyChangeListener;

public interface ITestRegistry
{
	PropertyChangeListener[] getPceListeners();

	ITestListener[] getTestListeners();
}
