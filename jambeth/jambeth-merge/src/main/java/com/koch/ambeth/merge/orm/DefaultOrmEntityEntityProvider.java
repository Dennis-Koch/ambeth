package com.koch.ambeth.merge.orm;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.util.xml.IXmlConfigUtil;

public class DefaultOrmEntityEntityProvider implements IOrmEntityTypeProvider
{
	@Autowired
	protected IXmlConfigUtil xmlConfigUtil;

	@Override
	public Class<?> resolveEntityType(String entityTypeName)
	{
		return xmlConfigUtil.getTypeForName(entityTypeName);
	}
}
