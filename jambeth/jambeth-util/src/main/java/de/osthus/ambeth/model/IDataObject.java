package de.osthus.ambeth.model;

public interface IDataObject
{
	boolean isToBeDeleted();

	boolean isToBeUpdated();

	boolean isToBeCreated();

	boolean hasPendingChanges();

	void setToBeDeleted(boolean toBeDeleted);

	void setToBeUpdated(boolean toBeUpdated);
}