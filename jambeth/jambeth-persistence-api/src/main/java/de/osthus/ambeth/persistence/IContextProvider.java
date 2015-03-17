package de.osthus.ambeth.persistence;

public interface IContextProvider
{
	Long getCurrentTime();

	void setCurrentTime(Long currentTime);

	String getCurrentUser();

	void setCurrentUser(String currentUser);

	void acquired();

	void clear();

	void clearAfterMerge();
}
