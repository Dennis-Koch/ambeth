package com.koch.ambeth.cache.datachange.revert;

public interface IBackup {
	void restore(Object target);
}