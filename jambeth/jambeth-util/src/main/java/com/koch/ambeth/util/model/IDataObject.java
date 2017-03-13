package com.koch.ambeth.util.model;

public interface IDataObject
{
	boolean isToBeDeleted();

	boolean isToBeUpdated();

	boolean isToBeCreated();

	boolean hasPendingChanges();

	void setToBeDeleted(boolean toBeDeleted);

	void setToBeUpdated(boolean toBeUpdated);
}