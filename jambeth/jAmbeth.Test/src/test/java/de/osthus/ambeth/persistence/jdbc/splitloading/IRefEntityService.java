package de.osthus.ambeth.persistence.jdbc.splitloading;

import java.util.List;

public interface IRefEntityService
{
	void save(List<RefEntity> entities);
}