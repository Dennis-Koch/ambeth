package de.osthus.ambeth.merge.orihelper;

import javax.persistence.PersistenceContext;

import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.merge.IEntityFactory;
import de.osthus.ambeth.merge.independent.EntityB;
import de.osthus.ambeth.proxy.Service;
import de.osthus.ambeth.util.ParamChecker;

@Service(ORIHelperTestService.class)
@PersistenceContext
public class ORIHelperTestServiceImpl implements ORIHelperTestService, IInitializingBean
{
	protected IEntityFactory entityFactory;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(entityFactory, "EntityFactory");
	}

	public void setEntityFactory(IEntityFactory entityFactory)
	{
		this.entityFactory = entityFactory;
	}

	@Override
	public EntityB[] getAllEntityBs()
	{
		return new EntityB[] { getE(1, 1), null, getE(2, 1) };
	}

	private EntityB getE(int id, int v)
	{
		EntityB e = entityFactory.createEntity(EntityB.class);
		e.setId(id);
		e.setVersion(v);
		return e;
	}
}
