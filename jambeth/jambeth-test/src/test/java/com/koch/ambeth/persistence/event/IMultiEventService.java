package com.koch.ambeth.persistence.event;

import java.util.List;

public interface IMultiEventService
{
	void save(List<MultiEventEntity> multiEventEntities);

	void save(MultiEventEntity2 multiEventEntity2);

	void doMultipleThings(List<MultiEventEntity> multiEventEntities);

	void doMultipleThings2(List<MultiEventEntity> multiEventEntities);
}
