package de.osthus.ambeth.xml.pending;

import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.metadata.Member;
import de.osthus.ambeth.xml.IReader;

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
