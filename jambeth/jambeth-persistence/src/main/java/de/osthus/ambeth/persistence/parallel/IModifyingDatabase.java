package de.osthus.ambeth.persistence.parallel;

public interface IModifyingDatabase
{
	boolean isModifyingAllowed();

	void setModifyingAllowed(boolean modifyingAllowed);

	boolean isModifyingDatabase();

	void setModifyingDatabase(boolean modifyingDatabase);
}
