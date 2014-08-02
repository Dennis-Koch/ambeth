package de.osthus.ambeth.persistence;

import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.metadata.Member;
import de.osthus.ambeth.util.ParamChecker;

public class DirectedExternalLink extends DirectedLink implements IDirectedLink, IInitializingBean
{
	protected Member toMember;

	protected Member fromMember;

	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Override
	public void afterPropertiesSet()
	{
		// super.afterPropertiesSet() intentionally not called here!
		ParamChecker.assertNotNull(entityMetaDataProvider, "entityMetaDataProvider");
		ParamChecker.assertTrue(fromTable != null || toTable != null, "fromTable or toTable");
		if (fromTable != null)
		{
			ParamChecker.assertNotNull(fromField, "fromField");
			ParamChecker.assertNotNull(toMember, "toMember");
		}
		else
		{
			ParamChecker.assertNotNull(toField, "toField");
			ParamChecker.assertNotNull(fromMember, "fromMember");
		}
		ParamChecker.assertNotNull(link, "link");
	}

	public void setEntityMetaDataProvider(IEntityMetaDataProvider entityMetaDataProvider)
	{
		this.entityMetaDataProvider = entityMetaDataProvider;
	}

	@Override
	public Class<?> getFromEntityType()
	{
		if (fromTable != null)
		{
			return super.getFromEntityType();
		}
		else
		{
			return fromMember.getDeclaringType();
		}
	}

	@Override
	public Class<?> getToEntityType()
	{
		if (toTable != null)
		{
			return super.getToEntityType();
		}
		else
		{
			return toMember.getDeclaringType();
		}
	}

	@Override
	public byte getFromIdIndex()
	{
		if (fromField != null)
		{
			return super.getToIdIndex();
		}
		else
		{
			IEntityMetaData metaData = entityMetaDataProvider.getMetaData(getFromEntityType());
			byte fromIdIndex = metaData.getIdIndexByMemberName(fromMember.getName());
			return fromIdIndex;
		}
	}

	@Override
	public byte getToIdIndex()
	{
		if (toField != null)
		{
			return super.getToIdIndex();
		}
		else
		{
			IEntityMetaData metaData = entityMetaDataProvider.getMetaData(getFromEntityType());
			byte toIdIndex = metaData.getIdIndexByMemberName(toMember.getName());
			return toIdIndex;
		}
	}

	@Override
	public Member getFromMember()
	{
		return fromMember;
	}

	public void setFromMember(Member fromMember)
	{
		this.fromMember = fromMember;
	}

	@Override
	public Member getToMember()
	{
		return toMember;
	}

	public void setToMember(Member toMember)
	{
		this.toMember = toMember;
	}

	@Override
	public String toString()
	{
		return "DirectedExternalLink: " + getName();
	}
}
