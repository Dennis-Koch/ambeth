package de.osthus.ambeth.persistence;

import java.util.Set;

import de.osthus.ambeth.util.IAlreadyLoadedCache;

public interface IContextProvider
{
	Long getCurrentTime();

	void setCurrentTime(Long currentTime);

	String getCurrentUser();

	void setCurrentUser(String currentUser);

	IAlreadyLoadedCache getCache();

	Set<Object> getAlreadyHandledSet();

	void clear();
}
