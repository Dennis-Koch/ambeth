package de.osthus.ambeth.persistence.jdbc.mapping;

import java.util.Date;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.mapping.CopyDirection;
import de.osthus.ambeth.mapping.IDedicatedMapper;
import de.osthus.ambeth.persistence.jdbc.mapping.models.OneToManyEntity;
import de.osthus.ambeth.persistence.jdbc.mapping.models.OneToManyEntityVO;

public class OneToManyEntityMapper implements IDedicatedMapper
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void applySpecialMapping(Object businessObject, Object valueObject, CopyDirection direction)
	{
		OneToManyEntity bo = (OneToManyEntity) businessObject;
		OneToManyEntityVO vo = (OneToManyEntityVO) valueObject;
		if (CopyDirection.BO_TO_VO == direction)
		{
			Date needsSpecialMapping = bo.getNeedsSpecialMapping();
			if (needsSpecialMapping != null)
			{
				vo.setNeedsSpecialMapping(needsSpecialMapping.getTime());
			}
		}
		else if (CopyDirection.VO_TO_BO == direction)
		{
			double needsSpecialMapping = vo.getNeedsSpecialMapping();
			if (needsSpecialMapping > 0)
			{
				Date date = new Date((long) needsSpecialMapping);
				bo.setNeedsSpecialMapping(date);
			}
		}
		else
		{
			throw new UnsupportedOperationException("CopyDirection " + direction.toString() + " not supported");
		}
	}

}
