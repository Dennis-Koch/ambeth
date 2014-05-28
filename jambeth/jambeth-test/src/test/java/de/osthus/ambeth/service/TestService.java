package de.osthus.ambeth.service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.model.Material;
import de.osthus.ambeth.model.MaterialGroup;
import de.osthus.ambeth.proxy.Service;
import de.osthus.ambeth.transfer.ITestService;
import de.osthus.ambeth.util.ParamChecker;

@Service(ITestService.class)
public class TestService implements ITestService, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected ICache cache;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(cache, "Cache");
	}

	public void setCache(ICache cache)
	{
		this.cache = cache;
	}

	@Override
	public void noParamNoReturn()
	{
	}

	@Override
	public void primitiveParamNoReturn(int param)
	{
	}

	@Override
	public void dateParamNoReturn(Date param)
	{
	}

	@Override
	public void primitiveArrayParamNoReturn(int[] param)
	{
	}

	@Override
	public void primitiveListParamNoReturn(List<Integer> param)
	{
	}

	@Override
	public void entityParamNoReturn(MaterialGroup param)
	{
	}

	@Override
	public void entityWithRelationParamNoReturn(Material param)
	{
	}

	@Override
	public void mixedParamsNoReturn(int number, Material material1, String text, MaterialGroup materialGroup, Material material2, Date date)
	{
	}

	@Override
	public int noParamPrimitiveReturn()
	{
		return 1;
	}

	@Override
	public Date noParamDateReturn()
	{
		return new Date();
	}

	@Override
	public int[] noParamPrimitiveArrayReturn()
	{
		return new int[] { 1, 2, 34 };
	}

	@Override
	public List<Integer> noParamPrimitiveListReturn()
	{
		return Arrays.asList(12, 3, 4);
	}

	@Override
	public MaterialGroup noParamEntityReturn()
	{
		return cache.getObject(MaterialGroup.class, "1");
	}

	@Override
	public Material noParamEntityWithRelationReturn()
	{
		return cache.getObject(Material.class, 1);
	}
}
