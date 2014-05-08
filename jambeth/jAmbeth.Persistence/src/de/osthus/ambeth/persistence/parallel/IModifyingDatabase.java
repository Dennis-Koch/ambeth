package de.osthus.ambeth.persistence.parallel;

public interface IModifyingDatabase
{
	boolean isModifyingAllowed();

	void setModifyingAllowed(boolean isModifyingAllowed);

	boolean isModifyingDatabase();

	void setModifyingDatabase(boolean modifying);
}
