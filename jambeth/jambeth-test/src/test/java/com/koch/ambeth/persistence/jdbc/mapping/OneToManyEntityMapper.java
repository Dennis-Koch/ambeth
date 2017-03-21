package com.koch.ambeth.persistence.jdbc.mapping;

/*-
 * #%L
 * jambeth-test
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import java.util.Date;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.mapping.CopyDirection;
import com.koch.ambeth.mapping.IDedicatedMapper;
import com.koch.ambeth.persistence.jdbc.mapping.models.OneToManyEntity;
import com.koch.ambeth.persistence.jdbc.mapping.models.OneToManyEntityVO;

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
