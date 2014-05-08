package de.osthus.ambeth.privilege.service;

import java.util.List;

import de.osthus.ambeth.cache.CacheDirective;
import de.osthus.ambeth.cache.CacheFactoryDirective;
import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.cache.ICacheContext;
import de.osthus.ambeth.cache.ICacheFactory;
import de.osthus.ambeth.cache.IDisposableCache;
import de.osthus.ambeth.cache.ISingleCacheRunnable;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.ISet;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IObjRefHelper;
import de.osthus.ambeth.merge.IProxyHelper;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.model.ISecurityScope;
import de.osthus.ambeth.privilege.IPrivilegeProvider;
import de.osthus.ambeth.privilege.IPrivilegeRegistry;
import de.osthus.ambeth.privilege.model.PrivilegeEnum;
import de.osthus.ambeth.privilege.transfer.PrivilegeResult;
import de.osthus.ambeth.security.SecurityContext;
import de.osthus.ambeth.security.SecurityContext.SecurityContextType;
import de.osthus.ambeth.security.config.SecurityConfigurationConstants;
import de.osthus.ambeth.util.ICacheHelper;
import de.osthus.ambeth.util.IPrefetchConfig;
import de.osthus.ambeth.util.IPrefetchHandle;
import de.osthus.ambeth.util.IPrefetchState;
import de.osthus.ambeth.util.ParamChecker;

@SecurityContext(SecurityContextType.AUTHENTICATED)
public class PrivilegeService implements IPrivilegeService, IInitializingBean
{
	public static class PrivilegeServiceCall implements ISingleCacheRunnable<List<PrivilegeResult>>
	{
		private final IObjRef[] objRefs;

		private final ISecurityScope[] securityScopes;

		private final PrivilegeService privilegeService;

		public PrivilegeServiceCall(IObjRef[] objRefs, ISecurityScope[] securityScopes, PrivilegeService privilegeService)
		{
			this.objRefs = objRefs;
			this.securityScopes = securityScopes;
			this.privilegeService = privilegeService;
		}

		@Override
		public List<PrivilegeResult> run() throws Throwable
		{
			return privilegeService.getPrivilegesIntern(objRefs, securityScopes);
		}
	}

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	private static final PrivilegeEnum[] emptyPrivileges = new PrivilegeEnum[0];

	protected ICache cache;

	protected ICacheContext cacheContext;

	protected ICacheFactory cacheFactory;

	protected ICacheHelper cacheHelper;

	protected IObjRefHelper oriHelper;

	protected IPrivilegeRegistry privilegeRegistry;

	protected IProxyHelper proxyHelper;

	protected boolean isDefaultReadPrivilege;

	protected boolean isDefaultUpdatePrivilege;

	protected boolean isDefaultDeletePrivilege;

	protected boolean isDefaultCreatePrivilege;

	@Override
	public void afterPropertiesSet()
	{
		ParamChecker.assertNotNull(cache, "Cache");
		ParamChecker.assertNotNull(cacheContext, "CacheContext");
		ParamChecker.assertNotNull(cacheFactory, "CacheFactory");
		ParamChecker.assertNotNull(cacheHelper, "CacheHelper");
		ParamChecker.assertNotNull(oriHelper, "OriHelper");
		ParamChecker.assertNotNull(proxyHelper, "ProxyHelper");
		ParamChecker.assertNotNull(privilegeRegistry, "PrivilegeRegistry");
	}

	@Property(name = SecurityConfigurationConstants.DefaultCreatePrivilegeActive, defaultValue = "true")
	public void setDefaultCreatePrivilege(boolean isDefaultCreatePrivilege)
	{
		this.isDefaultCreatePrivilege = isDefaultCreatePrivilege;
	}

	@Property(name = SecurityConfigurationConstants.DefaultDeletePrivilegeActive, defaultValue = "true")
	public void setDefaultDeletePrivilege(boolean isDefaultDeletePrivilege)
	{
		this.isDefaultDeletePrivilege = isDefaultDeletePrivilege;
	}

	@Property(name = SecurityConfigurationConstants.DefaultReadPrivilegeActive, defaultValue = "true")
	public void setDefaultReadPrivilege(boolean isDefaultReadPrivilege)
	{
		this.isDefaultReadPrivilege = isDefaultReadPrivilege;
	}

	@Property(name = SecurityConfigurationConstants.DefaultUpdatePrivilegeActive, defaultValue = "true")
	public void setDefaultUpdatePrivilege(boolean isDefaultUpdatePrivilege)
	{
		this.isDefaultUpdatePrivilege = isDefaultUpdatePrivilege;
	}

	public void setCache(ICache cache)
	{
		this.cache = cache;
	}

	public void setCacheContext(ICacheContext cacheContext)
	{
		this.cacheContext = cacheContext;
	}

	public void setCacheFactory(ICacheFactory cacheFactory)
	{
		this.cacheFactory = cacheFactory;
	}

	public void setCacheHelper(ICacheHelper cacheHelper)
	{
		this.cacheHelper = cacheHelper;
	}

	public void setOriHelper(IObjRefHelper oriHelper)
	{
		this.oriHelper = oriHelper;
	}

	public void setPrivilegeRegistry(IPrivilegeRegistry privilegeRegistry)
	{
		this.privilegeRegistry = privilegeRegistry;
	}

	public void setProxyHelper(IProxyHelper proxyHelper)
	{
		this.proxyHelper = proxyHelper;
	}

	public boolean isCreateAllowed(Object entity, ISecurityScope[] securityScopes)
	{
		return getPrivileges(entity, securityScopes).contains(PrivilegeEnum.CREATE_ALLOWED);
	}

	public boolean isUpdateAllowed(Object entity, ISecurityScope[] securityScopes)
	{
		return getPrivileges(entity, securityScopes).contains(PrivilegeEnum.UPDATE_ALLOWED);
	}

	public boolean isDeleteAllowed(Object entity, ISecurityScope[] securityScopes)
	{
		return getPrivileges(entity, securityScopes).contains(PrivilegeEnum.DELETE_ALLOWED);
	}

	public boolean isReadAllowed(Object entity, ISecurityScope[] securityScopes)
	{
		return getPrivileges(entity, securityScopes).contains(PrivilegeEnum.READ_ALLOWED);
	}

	public ISet<PrivilegeEnum> getPrivileges(Object entity, final ISecurityScope[] securityScopes)
	{
		IObjRef objRef = oriHelper.entityToObjRef(entity, true);
		final IObjRef[] objRefs = new IObjRef[] { objRef };
		IDisposableCache cacheForSecurityChecks = cacheFactory.create(CacheFactoryDirective.NoDCE);
		try
		{
			List<PrivilegeResult> result = cacheContext.executeWithCache(cacheForSecurityChecks, new PrivilegeServiceCall(objRefs, securityScopes, this));
			PrivilegeResult privilegeResult = result.get(0);
			return new HashSet<PrivilegeEnum>(privilegeResult.getPrivileges());
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			cacheForSecurityChecks.dispose();
		}
	}

	@Override
	public List<PrivilegeResult> getPrivileges(IObjRef[] objRefs, ISecurityScope[] securityScopes)
	{
		IDisposableCache cacheForSecurityChecks = cacheFactory.create(CacheFactoryDirective.NoDCE);
		try
		{
			return cacheContext.executeWithCache(cacheForSecurityChecks, new PrivilegeServiceCall(objRefs, securityScopes, this));
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			cacheForSecurityChecks.dispose();
		}
	}

	protected List<PrivilegeResult> getPrivilegesIntern(IObjRef[] objRefs, ISecurityScope[] securityScopes)
	{
		ICacheHelper cacheHelper = this.cacheHelper;
		IPrivilegeRegistry privilegeRegistry = this.privilegeRegistry;
		HashSet<Class<?>> requestedTypes = new HashSet<Class<?>>();
		IList<Object> entitiesToCheck = null;
		@SuppressWarnings("unused")
		IPrefetchState prefetchState;
		for (int a = 0, size = objRefs.length; a < size; a++)
		{
			IObjRef objRef = objRefs[a];
			if (objRef == null)
			{
				continue;
			}
			Class<?> realType = objRef.getRealType();
			requestedTypes.add(realType);
		}
		IPrefetchConfig prefetchConfig = cacheHelper.createPrefetch();
		for (Class<?> requestedType : requestedTypes)
		{
			IPrivilegeProvider privilegeProviderExtension = privilegeRegistry.getExtension(requestedType);
			if (privilegeProviderExtension == null)
			{
				continue;
			}
			privilegeProviderExtension.buildPrefetchConfig(requestedType, prefetchConfig);
		}
		entitiesToCheck = cache.getObjects(objRefs, CacheDirective.returnMisses());
		IPrefetchHandle prefetchHandle = prefetchConfig.build();
		prefetchState = prefetchHandle.prefetch(entitiesToCheck);
		ArrayList<PrivilegeResult> privilegeResults = new ArrayList<PrivilegeResult>(4);

		ISecurityScope[] currentSecurityScope = new ISecurityScope[1];
		for (int a = 0, size = objRefs.length; a < size; a++)
		{
			IObjRef objRef = objRefs[a];
			if (objRef == null)
			{
				continue;
			}
			Object entity = entitiesToCheck.get(a);
			Class<?> realType = objRef.getRealType();

			IPrivilegeProvider privilegeProviderExtension = privilegeRegistry.getExtension(realType);
			for (int b = securityScopes.length; b-- > 0;)
			{
				currentSecurityScope[0] = securityScopes[b];

				ArrayList<PrivilegeEnum> privilegeEnums = new ArrayList<PrivilegeEnum>();
				if (privilegeProviderExtension != null)
				{
					if (privilegeProviderExtension.isReadAllowed(entity, currentSecurityScope))
					{
						privilegeEnums.add(PrivilegeEnum.READ_ALLOWED);
					}
				}
				else if (isDefaultReadPrivilege)
				{
					privilegeEnums.add(PrivilegeEnum.READ_ALLOWED);
				}
				if (privilegeProviderExtension != null)
				{
					if (privilegeProviderExtension.isCreateAllowed(entity, currentSecurityScope))
					{
						privilegeEnums.add(PrivilegeEnum.CREATE_ALLOWED);
					}
				}
				else if (isDefaultCreatePrivilege)
				{
					privilegeEnums.add(PrivilegeEnum.CREATE_ALLOWED);
				}
				if (privilegeProviderExtension != null)
				{
					if (privilegeProviderExtension.isUpdateAllowed(entity, currentSecurityScope))
					{
						privilegeEnums.add(PrivilegeEnum.UPDATE_ALLOWED);
					}
				}
				else if (isDefaultUpdatePrivilege)
				{
					privilegeEnums.add(PrivilegeEnum.UPDATE_ALLOWED);
				}
				if (privilegeProviderExtension != null)
				{
					if (privilegeProviderExtension.isDeleteAllowed(entity, currentSecurityScope))
					{
						privilegeEnums.add(PrivilegeEnum.DELETE_ALLOWED);
					}
				}
				else if (isDefaultDeletePrivilege)
				{
					privilegeEnums.add(PrivilegeEnum.DELETE_ALLOWED);
				}

				PrivilegeResult privilegeResult = new PrivilegeResult();
				privilegeResult.setReference(objRef);
				privilegeResult.setSecurityScope(currentSecurityScope[0]);
				if (privilegeEnums.size() > 0)
				{
					privilegeResult.setPrivileges(privilegeEnums.toArray(new PrivilegeEnum[privilegeEnums.size()]));
				}
				else
				{
					privilegeResult.setPrivileges(emptyPrivileges);
				}
				privilegeResults.add(privilegeResult);
			}
		}
		return privilegeResults;
	}
}
