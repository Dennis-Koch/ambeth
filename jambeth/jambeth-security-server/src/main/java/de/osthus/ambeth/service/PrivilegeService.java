package de.osthus.ambeth.service;

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
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.extendable.ClassExtendableListContainer;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IObjRefHelper;
import de.osthus.ambeth.merge.IProxyHelper;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.model.ISecurityScope;
import de.osthus.ambeth.privilege.IPrivilegeProviderExtension;
import de.osthus.ambeth.privilege.IPrivilegeProviderExtensionExtendable;
import de.osthus.ambeth.privilege.evaluation.impl.EntityPermissionEvaluation;
import de.osthus.ambeth.privilege.evaluation.impl.EntityPropertyPermissionEvaluation;
import de.osthus.ambeth.privilege.evaluation.impl.ScopedEntityPermissionEvaluation;
import de.osthus.ambeth.privilege.evaluation.impl.ScopedEntityPropertyPermissionEvaluation;
import de.osthus.ambeth.privilege.model.IPrivilege;
import de.osthus.ambeth.privilege.transfer.IPrivilegeOfService;
import de.osthus.ambeth.privilege.transfer.IPropertyPrivilegeOfService;
import de.osthus.ambeth.privilege.transfer.PrivilegeOfService;
import de.osthus.ambeth.privilege.transfer.PropertyPrivilegeOfService;
import de.osthus.ambeth.security.IAuthorization;
import de.osthus.ambeth.security.ISecurityActivation;
import de.osthus.ambeth.security.ISecurityScopeProvider;
import de.osthus.ambeth.security.SecurityContext;
import de.osthus.ambeth.security.SecurityContext.SecurityContextType;
import de.osthus.ambeth.security.config.SecurityConfigurationConstants;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;
import de.osthus.ambeth.util.IInterningFeature;
import de.osthus.ambeth.util.IPrefetchConfig;
import de.osthus.ambeth.util.IPrefetchHandle;
import de.osthus.ambeth.util.IPrefetchHelper;
import de.osthus.ambeth.util.IPrefetchState;

public class PrivilegeService implements IPrivilegeService, IPrivilegeProviderExtensionExtendable
{
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
	protected IInterningFeature interningFeature;

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

	@Property(name = SecurityConfigurationConstants.DefaultCreatePrivilegeActive, defaultValue = "true")
	protected boolean isDefaultCreatePrivilege;

	@Property(name = SecurityConfigurationConstants.DefaultUpdatePrivilegeActive, defaultValue = "true")
	protected boolean isDefaultUpdatePrivilege;

	@Property(name = SecurityConfigurationConstants.DefaultDeletePrivilegeActive, defaultValue = "true")
	protected boolean isDefaultDeletePrivilege;

	@Property(name = SecurityConfigurationConstants.DefaultExecutePrivilegeActive, defaultValue = "true")
	protected boolean isDefaultExecutePrivilege;

	@Property(name = SecurityConfigurationConstants.DefaultReadPropertyPrivilegeActive, defaultValue = "true")
	protected boolean isDefaultReadPropertyPrivilege;

	@Property(name = SecurityConfigurationConstants.DefaultCreatePropertyPrivilegeActive, defaultValue = "true")
	protected boolean isDefaultCreatePropertyPrivilege;

	@Property(name = SecurityConfigurationConstants.DefaultUpdatePropertyPrivilegeActive, defaultValue = "true")
	protected boolean isDefaultUpdatePropertyPrivilege;

	@Property(name = SecurityConfigurationConstants.DefaultDeletePropertyPrivilegeActive, defaultValue = "true")
	protected boolean isDefaultDeletePropertyPrivilege;

	public boolean isCreateAllowed(Object entity, ISecurityScope[] securityScopes)
	{
		return getPrivileges(entity, securityScopes).isCreateAllowed();
	}

	public boolean isUpdateAllowed(Object entity, ISecurityScope[] securityScopes)
	{
		return getPrivileges(entity, securityScopes).isUpdateAllowed();
	}

	public boolean isDeleteAllowed(Object entity, ISecurityScope[] securityScopes)
	{
		return getPrivileges(entity, securityScopes).isDeleteAllowed();
	}

	public boolean isReadAllowed(Object entity, ISecurityScope[] securityScopes)
	{
		return getPrivileges(entity, securityScopes).isReadAllowed();
	}

	public boolean isExecuteAllowed(Object entity, ISecurityScope[] securityScopes)
	{
		return getPrivileges(entity, securityScopes).isExecutionAllowed();
	}

	public IPrivilegeOfService getPrivileges(Object entity, ISecurityScope[] securityScopes)
	{
		IObjRef objRef = oriHelper.entityToObjRef(entity, true);
		IObjRef[] objRefs = new IObjRef[] { objRef };

		try
		{
			List<IPrivilegeOfService> result = getPrivileges(objRefs, securityScopes);
			return result.get(0);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public List<IPrivilegeOfService> getPrivileges(final IObjRef[] objRefs, final ISecurityScope[] securityScopes)
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

	@Override
	public List<IPrivilegeOfService> getPrivilegesOfTypes(final Class<?>[] entityTypes, final ISecurityScope[] securityScopes)
	{
		IDisposableCache cacheForSecurityChecks = cacheFactory.createPrivileged(CacheFactoryDirective.NoDCE, false, Boolean.FALSE);
		try
		{
			return cacheContext.executeWithCache(cacheForSecurityChecks, new ISingleCacheRunnable<List<IPrivilegeOfService>>()
			{
				@Override
				public List<IPrivilegeOfService> run() throws Throwable
				{
					return getPrivilegesOfTypesIntern(entityTypes, securityScopes);
				}
			});
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

	List<IPrivilegeOfService> getPrivilegesIntern(final IObjRef[] objRefs, final ISecurityScope[] securityScopes)
	{
		try
		{
			return securityActivation.executeWithoutSecurity(new IResultingBackgroundWorkerDelegate<List<IPrivilegeOfService>>()
			{
				@Override
				public List<IPrivilegeOfService> invoke() throws Throwable
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

	List<IPrivilegeOfService> getPrivilegesOfTypesIntern(final Class<?>[] entityTypes, final ISecurityScope[] securityScopes)
	{
		try
		{
			return securityActivation.executeWithoutSecurity(new IResultingBackgroundWorkerDelegate<List<IPrivilegeOfService>>()
			{
				@Override
				public List<IPrivilegeOfService> invoke() throws Throwable
				{
					return getPrivilegesOfTypesIntern2(entityTypes, securityScopes);
				}
			});
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	List<IPrivilegeOfService> getPrivilegesIntern2(IObjRef[] objRefs, ISecurityScope[] securityScopes)
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
		ArrayList<IPrivilegeOfService> privilegeResults = new ArrayList<IPrivilegeOfService>();

		IAuthorization authorization = securityScopeProvider.getAuthorization();
		EntityPermissionEvaluation pe = new EntityPermissionEvaluation(securityScopes, isDefaultCreatePrivilege, isDefaultReadPrivilege,
				isDefaultUpdatePrivilege, isDefaultDeletePrivilege, isDefaultExecutePrivilege, isDefaultCreatePropertyPrivilege,
				isDefaultReadPropertyPrivilege, isDefaultUpdatePropertyPrivilege, isDefaultDeletePropertyPrivilege);

		for (int a = 0, size = objRefs.length; a < size; a++)
		{
			IObjRef objRef = objRefs[a];
			if (objRef == null)
			{
				continue;
			}
			Object entity = entitiesToCheck.get(a);

			pe.reset();
			applyEntityTypePermission(pe, authorization, objRef.getRealType(), securityScopes);
			if (entity != null)
			{
				IList<IPrivilegeProviderExtension<?>> extensions = privilegeProviderExtensions.getExtensions(objRef.getRealType());
				for (int c = 0, sizeC = extensions.size(); c < sizeC; c++)
				{
					IPrivilegeProviderExtension extension = extensions.get(c);
					extension.evaluatePermissionOnInstance(objRef, entity, authorization, securityScopes, pe);
				}
			}
			else
			{
				// an entity which can not be read even without active security is not valid
				pe.denyEach();
			}
			if (securityScopes.length > 1)
			{
				throw new UnsupportedOperationException("Multiple scopes at the same time not yet supported");
			}
			ScopedEntityPermissionEvaluation[] spes = pe.getSpes();

			for (int b = 0, sizeB = securityScopes.length; b < sizeB; b++)
			{
				ISecurityScope scope = securityScopes[b];
				ScopedEntityPermissionEvaluation spe = spes[b];

				PrivilegeOfService privilegeResult = buildPrivilegeResult(objRef, pe, scope, spe);
				privilegeResults.add(privilegeResult);
			}
		}
		return privilegeResults;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	List<IPrivilegeOfService> getPrivilegesOfTypesIntern2(Class<?>[] entityTypes, ISecurityScope[] securityScopes)
	{
		ArrayList<IPrivilegeOfService> privilegeResults = new ArrayList<IPrivilegeOfService>();

		IAuthorization authorization = securityScopeProvider.getAuthorization();
		EntityPermissionEvaluation pe = new EntityPermissionEvaluation(securityScopes, isDefaultCreatePrivilege, isDefaultReadPrivilege,
				isDefaultUpdatePrivilege, isDefaultDeletePrivilege, isDefaultExecutePrivilege, isDefaultCreatePropertyPrivilege,
				isDefaultReadPropertyPrivilege, isDefaultUpdatePropertyPrivilege, isDefaultDeletePropertyPrivilege);

		for (int a = 0, size = entityTypes.length; a < size; a++)
		{
			Class<?> entityType = entityTypes[a];
			if (entityType == null)
			{
				continue;
			}
			pe.reset();
			applyEntityTypePermission(pe, authorization, entityType, securityScopes);
			IList<IPrivilegeProviderExtension<?>> extensions = privilegeProviderExtensions.getExtensions(entityType);
			for (int c = 0, sizeC = extensions.size(); c < sizeC; c++)
			{
				IPrivilegeProviderExtension extension = extensions.get(c);
				extension.evaluatePermissionOnType(entityType, authorization, securityScopes, pe);
			}
			if (securityScopes.length > 1)
			{
				throw new UnsupportedOperationException("Multiple scopes at the same time not yet supported");
			}
			ScopedEntityPermissionEvaluation[] spes = pe.getSpes();

			ObjRef objRef = new ObjRef(entityType, ObjRef.PRIMARY_KEY_INDEX, null, null);
			for (int b = 0, sizeB = securityScopes.length; b < sizeB; b++)
			{
				ISecurityScope scope = securityScopes[b];
				ScopedEntityPermissionEvaluation spe = spes[b];

				PrivilegeOfService privilegeResult = buildPrivilegeResult(objRef, pe, scope, spe);
				privilegeResults.add(privilegeResult);
			}
		}
		return privilegeResults;
	}

	protected void applyEntityTypePermission(EntityPermissionEvaluation pe, IAuthorization authorization, Class<?> entityType, ISecurityScope[] securityScopes)
	{
		IPrivilege entityTypePrivilege = authorization.getEntityTypePrivilege(entityType, securityScopes);
		if (entityTypePrivilege.isCreateAllowed())
		{
			pe.allowCreate();
		}
		else
		{
			pe.denyCreate();
		}
		if (entityTypePrivilege.isUpdateAllowed())
		{
			pe.allowUpdate();
		}
		else
		{
			pe.denyUpdate();
		}
		if (entityTypePrivilege.isDeleteAllowed())
		{
			pe.allowDelete();
		}
		else
		{
			pe.denyDelete();
		}
		if (entityTypePrivilege.isExecutionAllowed())
		{
			pe.allowExecute();
		}
		else
		{
			pe.denyExecute();
		}
		if (entityTypePrivilege.isReadAllowed())
		{
			pe.allowRead();
		}
		else
		{
			pe.denyRead();
		}
	}

	protected PrivilegeOfService buildPrivilegeResult(IObjRef objRef, EntityPermissionEvaluation pe, ISecurityScope scope, ScopedEntityPermissionEvaluation spe)
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

		boolean hasPropertyPrivileges = pe.getPropertyPermissions().size() > 0 || (spe != null && spe.getPropertyPermissions().size() > 0);

		PrivilegeOfService privilegeResult = new PrivilegeOfService();

		if (hasPropertyPrivileges)
		{
			HashSet<String> propertyNamesSet = new HashSet<String>(pe.getPropertyPermissions().keySet());
			if (spe != null)
			{
				propertyNamesSet.addAll(spe.getPropertyPermissions().keySet());
			}
			String[] propertyNames = propertyNamesSet.toArray(String.class);
			IPropertyPrivilegeOfService[] propertyPrivileges = new IPropertyPrivilegeOfService[propertyNames.length];
			for (int a = 0, size = propertyNames.length; a < size; a++)
			{
				String propertyName = interningFeature.intern(propertyNames[a]);
				Boolean pCreate = null, pRead = null, pUpdate = null, pDelete = null;
				if (spe != null)
				{
					ScopedEntityPropertyPermissionEvaluation sppe = spe.getPropertyPermissions().get(propertyName);
					if (sppe != null)
					{
						pCreate = sppe.getCreate();
						pRead = sppe.getRead();
						pUpdate = sppe.getUpdate();
						pDelete = sppe.getDelete();
					}
				}
				EntityPropertyPermissionEvaluation ppe = pe.getPropertyPermissions().get(propertyName);
				if (pCreate == null && ppe != null)
				{
					pCreate = ppe.getCreate();
				}
				if (pRead == null && ppe != null)
				{
					pRead = ppe.getRead();
				}
				if (pUpdate == null && ppe != null)
				{
					pUpdate = ppe.getUpdate();
				}
				if (pDelete == null && ppe != null)
				{
					pDelete = ppe.getDelete();
				}
				if (pCreate == null)
				{
					pCreate = create;
				}
				if (pRead == null)
				{
					pRead = read;
				}
				if (pUpdate == null)
				{
					pUpdate = update;
				}
				if (pDelete == null)
				{
					pDelete = delete;
				}
				propertyNames[a] = propertyName;
				propertyPrivileges[a] = PropertyPrivilegeOfService.create(pCreate.booleanValue(), pRead.booleanValue(), pUpdate.booleanValue(),
						pDelete.booleanValue());
			}
			privilegeResult.setPropertyPrivileges(propertyPrivileges);
			privilegeResult.setPropertyPrivilegeNames(propertyNames);
		}
		privilegeResult.setReference(objRef);
		privilegeResult.setSecurityScope(scope);
		privilegeResult.setReadAllowed(read.booleanValue());
		privilegeResult.setCreateAllowed(create.booleanValue());
		privilegeResult.setUpdateAllowed(update.booleanValue());
		privilegeResult.setDeleteAllowed(delete.booleanValue());
		privilegeResult.setExecuteAllowed(execute.booleanValue());
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
