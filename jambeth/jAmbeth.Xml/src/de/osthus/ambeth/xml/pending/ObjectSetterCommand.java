package de.osthus.ambeth.xml.pending;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.typeinfo.ITypeInfoItem;
import de.osthus.ambeth.util.ParamChecker;
import de.osthus.ambeth.xml.IReader;

public class ObjectSetterCommand extends AbstractObjectCommand implements IObjectCommand, IInitializingBean
{
	ITypeInfoItem member;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		ParamChecker.assertNotNull(member, "Member");
	}

	public void setMember(ITypeInfoItem member)
	{
		this.member = member;
	}

	@Override
	public void execute(IReader reader)
	{
		Object value = objectFuture.getValue();
		try
		{
			member.setValue(parent, value);
		}
		catch (Exception e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
