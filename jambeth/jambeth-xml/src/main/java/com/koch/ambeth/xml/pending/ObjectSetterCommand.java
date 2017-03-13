package com.koch.ambeth.xml.pending;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.xml.IReader;

public class ObjectSetterCommand extends AbstractObjectCommand implements IObjectCommand, IInitializingBean
{
	@Property
	protected Member member;

	@Override
	public void execute(IReader reader)
	{
		Object value = objectFuture.getValue();
		member.setValue(parent, value);
	}
}
