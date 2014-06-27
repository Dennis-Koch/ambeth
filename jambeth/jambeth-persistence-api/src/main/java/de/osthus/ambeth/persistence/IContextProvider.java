package de.osthus.ambeth.persistence;

import de.osthus.ambeth.util.IAlreadyLoadedCache;

public interface IContextProvider
{
	Long getCurrentTime();

	void setCurrentTime(Long currentTime);

	String getCurrentUser();

	void setCurrentUser(String currentUser);

	IAlreadyLoadedCache getAlreadyLoadedCache();

	void acquired();

	void clear();

	void clearAfterMerge();
}
