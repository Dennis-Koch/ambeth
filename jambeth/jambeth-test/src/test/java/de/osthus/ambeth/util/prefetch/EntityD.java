package de.osthus.ambeth.util.prefetch;

import java.util.List;

public interface EntityD
{
	Integer getId();

	Integer getVersion();

	EntityD getParentDOfD();

	void setParentDOfD(EntityD parentD);

	List<EntityD> getDsOfD();
}
