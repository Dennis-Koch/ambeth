package de.osthus.ambeth.service;

import java.util.List;

import de.osthus.ambeth.cache.CacheDirective;
import de.osthus.ambeth.cache.CacheFactoryDirective;
import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.cache.ICacheContext;
import de.osthus.ambeth.cache.ICacheFactory;
import de.osthus.ambeth.cache.IDisposableCache;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.ISet;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.extendable.ClassExtendableListContainer;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IObjRefHelper;
import de.osthus.ambeth.merge.IProxyHelper;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.model.ISecurityScope;
import de.osthus.ambeth.privilege.IPrivilegeProviderExtension;
import de.osthus.ambeth.privilege.IPrivilegeProviderExtensionExtendable;
import de.osthus.ambeth.privilege.evaluation.PermissionEvaluation;
import de.osthus.ambeth.privilege.evaluation.ScopedPermissionEvaluation;
import de.osthus.ambeth.privilege.model.PrivilegeEnum;
import de.osthus.ambeth.privilege.transfer.PrivilegeResult;
import de.osthus.ambeth.security.IAuthorization;
import de.osthus.ambeth.security.ISecurityActivation;
import de.osthus.ambeth.security.ISecurityScopeProvider;
import de.osthus.ambeth.security.SecurityContext;
import de.osthus.ambeth.security.SecurityContext.SecurityContextType;
import de.osthus.ambeth.security.config.SecurityConfigurationConstants;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;
import de.osthus.ambeth.util.IPrefetchConfig;
import de.osthus.ambeth.util.IPrefetchHandle;
import de.osthus.ambeth.util.IPrefetchHelper;
import de.osthus.ambeth.util.IPrefetchState;

public class PrivilegeService implements IPrivilegeService, IPrivilegeProviderExtensionExtendable
{
	private static final PrivilegeEnum[] emptyPrivileges = new PrivilegeEnum[0];

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected final ClassExtendableListContainer<IPrivilegeProviderExtension<?>> privilegeProviderExtensions = new ClassExtendableListContainer<IPrivilegeProviderExtension<?>>(
			"privilegeProviderExtension", "entityType");

	@Autowired
	protected ICache cache;

	@Autowired
	protected ICacheContext cacheContext;

	@Autowired
	protected ICacheFactory cacheFactory;

	@Autowired
	protected IObjRefHelper oriHelper;

	@Autowired
	protected IPrefetchHelper prefetchHelper;

	@Autowired
	protected IProxyHelper proxyHelper;

	@Autowired
	protected ISecurityActivation securityActivation;

	@Autowired
	protected ISecurityScopeProvider securityScopeProvider;

	@Property(name = SecurityConfigurationConstants.DefaultReadPrivilegeActive, defaultValue = "true")
	protected boolean isDefaultReadPrivilege;

	@Property(name = SecurityConfigurationConstants.DefaultUpdatePrivilegeActive, defaultValue = "true")
	protected boolean isDefaultUpdatePrivilege;

	@Property(name = SecurityConfigurationConstants.DefaultDeletePrivilegeActive, defaultValue = "true")
	protected boolean isDefaultDeletePrivilege;

	@Property(name = SecurityConfigurationConstants.DefaultExecutePrivilegeActive, defaultValue = "true")
	protected boolean isDefaultExecutePrivilege;

	@Property(name = SecurityConfigurationConstants.DefaultCreatePrivilegeActive, defaultValue = "true")
	protected boolean isDefaultCreatePrivilege;

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

	public ISet<PrivilegeEnum> getPrivileges(Object entity, ISecurityScope[] securityScopes)
	{
		IObjRef objRef = oriHelper.entityToObjRef(entity, true);
		IObjRef[] objRefs = new IObjRef[] { objRef };

		try
		{
			List<PrivilegeResult> result = getPrivileges(objRefs, securityScopes);
			PrivilegeResult privilegeResult = result.get(0);
			return new HashSet<PrivilegeEnum>(privilegeResult.getPrivileges());
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public List<PrivilegeResult> getPrivileges(final IObjRef[] objRefs, final ISecurityScope[] securityScopes)
	{
		IDisposableCache cacheForSecurityChecks = cacheFactory.createPrivileged(CacheFactoryDirective.NoDCE, false, Boolean.FALSE);
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

	List<PrivilegeResult> getPrivilegesIntern(final IObjRef[] objRefs, final ISecurityScope[] securityScopes)
	{
		try
		{
			return securityActivation.executeWithoutSecurity(new IResultingBackgroundWorkerDelegate<List<PrivilegeResult>>()
			{
				@Override
				public List<PrivilegeResult> invoke() throws Throwable
				{
					return getPrivilegesIntern2(objRefs, securityScopes);
				}
			});
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	List<PrivilegeResult> getPrivilegesIntern2(IObjRef[] objRefs, ISecurityScope[] securityScopes)
	{
		IPrefetchHelper prefetchHelper = this.prefetchHelper;
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
		IPrefetchConfig prefetchConfig = prefetchHelper.createPrefetch();
		for (Class<?> requestedType : requestedTypes)
		{
			IList<IPrivilegeProviderExtension<?>> extensions = privilegeProviderExtensions.getExtensions(requestedType);
			for (int a = 0, size = extensions.size(); a < size; a++)
			{
				IPrivilegeProviderExtension extension = extensions.get(a);
				extension.buildPrefetchConfig(requestedType, prefetchConfig);
			}
		}
		entitiesToCheck = cache.getObjects(objRefs, CacheDirective.returnMisses());
		IPrefetchHandle prefetchHandle = prefetchConfig.build();
		prefetchState = prefetchHandle.prefetch(entitiesToCheck);
		ArrayList<PrivilegeResult> privilegeResults = new ArrayList<PrivilegeResult>();
		ArrayList<PrivilegeEnum> privilegeEnums = new ArrayList<PrivilegeEnum>(4);

		IAuthorization authorization = securityScopeProvider.getAuthorization();
		PermissionEvaluation pe = new PermissionEvaluation(securityScopes, isDefaultCreatePrivilege, isDefaultReadPrivilege, isDefaultUpdatePrivilege,
				isDefaultDeletePrivilege, isDefaultExecutePrivilege);

		for (int a = 0, size = objRefs.length; a < size; a++)
		{
			IObjRef objRef = objRefs[a];
			if (objRef == null)
			{
				continue;
			}
			Object entity = entitiesToCheck.get(a);

			pe.reset();
			if (entity != null)
			{
				IList<IPrivilegeProviderExtension<?>> extensions = privilegeProviderExtensions.getExtensions(objRef.getRealType());
				for (int c = 0, sizeC = extensions.size(); c < sizeC; c++)
				{
					IPrivilegeProviderExtension extension = extensions.get(c);
					extension.evaluatePermission(objRef, entity, authorization, securityScopes, pe);
				}
			}
			else
			{
				// an entity which can not be read even without active security is not valid
				pe.denyEach();
			}
			ScopedPermissionEvaluation[] spes = pe.getSpes();

			for (int b = 0, sizeB = securityScopes.length; b < sizeB; b++)
			{
				ISecurityScope scope = securityScopes[b];
				ScopedPermissionEvaluation spe = spes[b];

				PrivilegeResult privilegeResult = buildPrivilegeResult(objRef, pe, scope, spe, privilegeEnums);
				privilegeResults.add(privilegeResult);
			}
		}
		return privilegeResults;
	}

	protected PrivilegeResult buildPrivilegeResult(IObjRef objRef, PermissionEvaluation pe, ISecurityScope scope, ScopedPermissionEvaluation spe,
			ArrayList<PrivilegeEnum> privilegeEnums)
	{
		Boolean create = null, read = null, update = null, delete = null, execute = null;
		if (spe != null)
		{
			create = spe.getCreate();
			read = spe.getRead();
			update = spe.getUpdate();
			delete = spe.getDelete();
			execute = spe.getExecute();
		}
		if (create == null)
		{
			create = pe.getCreate();
		}
		if (read == null)
		{
			read = pe.getRead();
		}
		if (update == null)
		{
			update = pe.getUpdate();
		}
		if (delete == null)
		{
			delete = pe.getDelete();
		}
		if (execute == null)
		{
			execute = pe.getExecute();
		}
		if (create == null)
		{
			create = Boolean.valueOf(isDefaultCreatePrivilege);
		}
		if (read == null)
		{
			read = Boolean.valueOf(isDefaultReadPrivilege);
		}
		if (update == null)
		{
			update = Boolean.valueOf(isDefaultUpdatePrivilege);
		}
		if (delete == null)
		{
			delete = Boolean.valueOf(isDefaultDeletePrivilege);
		}
		if (execute == null)
		{
			execute = Boolean.valueOf(isDefaultExecutePrivilege);
		}
		privilegeEnums.clear();
		if (create.booleanValue())
		{
			privilegeEnums.add(PrivilegeEnum.CREATE_ALLOWED);
		}
		if (read.booleanValue())
		{
			privilegeEnums.add(PrivilegeEnum.READ_ALLOWED);
		}
		if (update.booleanValue())
		{
			privilegeEnums.add(PrivilegeEnum.UPDATE_ALLOWED);
		}
		if (delete.booleanValue())
		{
			privilegeEnums.add(PrivilegeEnum.DELETE_ALLOWED);
		}
		if (execute.booleanValue())
		{
			privilegeEnums.add(PrivilegeEnum.EXECUTE_ALLOWED);
		}

		PrivilegeResult privilegeResult = new PrivilegeResult();
		privilegeResult.setReference(objRef);
		privilegeResult.setSecurityScope(scope);
		if (privilegeEnums.size() > 0)
		{
			privilegeResult.setPrivileges(privilegeEnums.toArray(PrivilegeEnum.class));
		}
		else
		{
			privilegeResult.setPrivileges(emptyPrivileges);
		}
		return privilegeResult;
	}

	@Override
	@SecurityContext(SecurityContextType.NOT_REQUIRED)
	public <T> void registerPrivilegeProviderExtension(IPrivilegeProviderExtension<? super T> privilegeProviderExtension, Class<T> entityType)
	{
		privilegeProviderExtensions.register(privilegeProviderExtension, entityType);
	}

	@Override
	@SecurityContext(SecurityContextType.NOT_REQUIRED)
	public <T> void unregisterPrivilegeProviderExtension(IPrivilegeProviderExtension<? super T> privilegeProviderExtension, Class<T> entityType)
	{
		privilegeProviderExtensions.unregister(privilegeProviderExtension, entityType);
	}
}
