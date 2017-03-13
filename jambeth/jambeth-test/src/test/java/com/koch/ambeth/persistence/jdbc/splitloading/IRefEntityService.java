package com.koch.ambeth.persistence.jdbc.splitloading;

import java.util.List;

public interface IRefEntityService
{
	void save(List<RefEntity> entities);
}