package de.osthus.ambeth.orm;

import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.util.xml.IXmlConfigUtil;

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
