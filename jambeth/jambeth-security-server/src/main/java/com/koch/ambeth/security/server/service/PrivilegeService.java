package com.koch.ambeth.security.server.service;

import com.koch.ambeth.cache.interceptor.SingleCacheOnDemandProvider;
import com.koch.ambeth.event.IEventDispatcher;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.extendable.ClassExtendableListContainer;
import com.koch.ambeth.ioc.threadlocal.Forkable;
import com.koch.ambeth.ioc.threadlocal.IForkProcessor;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.IObjRefHelper;
import com.koch.ambeth.merge.IProxyHelper;
import com.koch.ambeth.merge.cache.CacheDirective;
import com.koch.ambeth.merge.cache.CacheFactoryDirective;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.cache.ICacheContext;
import com.koch.ambeth.merge.cache.ICacheFactory;
import com.koch.ambeth.merge.cache.IDisposableCache;
import com.koch.ambeth.merge.security.ISecurityActivation;
import com.koch.ambeth.merge.security.ISecurityScopeProvider;
import com.koch.ambeth.merge.util.IPrefetchConfig;
import com.koch.ambeth.merge.util.IPrefetchHandle;
import com.koch.ambeth.merge.util.IPrefetchHelper;
import com.koch.ambeth.merge.util.IPrefetchState;
import com.koch.ambeth.security.IAuthorization;
import com.koch.ambeth.security.IAuthorizationProcess;
import com.koch.ambeth.security.ISecurityContext;
import com.koch.ambeth.security.ISecurityContextHolder;
import com.koch.ambeth.security.SecurityContext;
import com.koch.ambeth.security.SecurityContextType;
import com.koch.ambeth.security.config.SecurityConfigurationConstants;
import com.koch.ambeth.security.events.ClearAllCachedPrivilegesEvent;
import com.koch.ambeth.security.privilege.model.ITypePrivilege;
import com.koch.ambeth.security.privilege.model.ITypePropertyPrivilege;
import com.koch.ambeth.security.privilege.transfer.IPrivilegeOfService;
import com.koch.ambeth.security.privilege.transfer.IPropertyPrivilegeOfService;
import com.koch.ambeth.security.privilege.transfer.ITypePrivilegeOfService;
import com.koch.ambeth.security.privilege.transfer.ITypePropertyPrivilegeOfService;
import com.koch.ambeth.security.privilege.transfer.PrivilegeOfService;
import com.koch.ambeth.security.privilege.transfer.PropertyPrivilegeOfService;
import com.koch.ambeth.security.privilege.transfer.TypePrivilegeOfService;
import com.koch.ambeth.security.privilege.transfer.TypePropertyPrivilegeOfService;
import com.koch.ambeth.security.server.privilege.EntityPermissionRuleAddedEvent;
import com.koch.ambeth.security.server.privilege.EntityPermissionRuleRemovedEvent;
import com.koch.ambeth.security.server.privilege.EntityTypePermissionRuleAddedEvent;
import com.koch.ambeth.security.server.privilege.EntityTypePermissionRuleRemovedEvent;
import com.koch.ambeth.security.server.privilege.IEntityPermissionRule;
import com.koch.ambeth.security.server.privilege.IEntityPermissionRuleExtendable;
import com.koch.ambeth.security.server.privilege.IEntityPermissionRuleProvider;
import com.koch.ambeth.security.server.privilege.IEntityTypePermissionRule;
import com.koch.ambeth.security.server.privilege.IEntityTypePermissionRuleExtendable;
import com.koch.ambeth.security.server.privilege.IEntityTypePermissionRuleProvider;
import com.koch.ambeth.security.server.privilege.evaluation.impl.EntityPermissionEvaluation;
import com.koch.ambeth.security.server.privilege.evaluation.impl.ScopedEntityPermissionEvaluation;
import com.koch.ambeth.security.service.IPrivilegeService;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.service.metadata.RelationMember;
import com.koch.ambeth.service.model.ISecurityScope;
import com.koch.ambeth.util.IInterningFeature;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.ILinkedMap;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.ISet;
import com.koch.ambeth.util.collections.LinkedHashMap;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.state.StateRollback;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

public class PrivilegeService
        implements IPrivilegeService, IEntityPermissionRuleExtendable, IEntityTypePermissionRuleExtendable, IEntityPermissionRuleProvider, IEntityTypePermissionRuleProvider, IThreadLocalCleanupBean {
    protected final ClassExtendableListContainer<IEntityPermissionRule<?>> entityPermissionRules = new ClassExtendableListContainer<>("entityPermissionRule", "entityType");
    protected final ClassExtendableListContainer<IEntityTypePermissionRule> entityTypePermissionRules = new ClassExtendableListContainer<>("entityTypePermissionRule", "entityType");
    @Forkable(processor = PrivilegeServiceForkProcessor.class)
    protected final ThreadLocal<IDisposableCache> privilegeCacheTL = new ThreadLocal<>();
    @Autowired(optional = true)
    protected IAuthorizationProcess authorizationProcess;
    @Autowired
    protected ICache cache;
    @Autowired
    protected ICacheContext cacheContext;
    @Autowired
    protected ICacheFactory cacheFactory;
    @Autowired
    protected IEntityMetaDataProvider entityMetaDataProvider;
    @Autowired
    protected IEventDispatcher eventDispatcher;
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
    protected ISecurityContextHolder securityContextHolder;
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
    @LogInstance
    private ILogger log;

    @Override
    public void cleanupThreadLocal() {
        IDisposableCache privilegeCache = privilegeCacheTL.get();
        if (privilegeCache != null) {
            privilegeCacheTL.set(null);
            privilegeCache.dispose();
        }
    }

    public IDisposableCache getOrCreatePrivilegeCache() {
        IDisposableCache privilegeCache = privilegeCacheTL.get();
        if (privilegeCache != null) {
            return privilegeCache;
        }
        privilegeCache = cacheFactory.createPrivileged(CacheFactoryDirective.SubscribeTransactionalDCE, false, Boolean.FALSE, "Privilege.ORIGINAL");
        privilegeCacheTL.set(privilegeCache);
        return privilegeCache;
    }

    public boolean isCreateAllowed(Object entity, ISecurityScope[] securityScopes) {
        return getPrivileges(entity, securityScopes).isCreateAllowed();
    }

    public boolean isUpdateAllowed(Object entity, ISecurityScope[] securityScopes) {
        return getPrivileges(entity, securityScopes).isUpdateAllowed();
    }

    public boolean isDeleteAllowed(Object entity, ISecurityScope[] securityScopes) {
        return getPrivileges(entity, securityScopes).isDeleteAllowed();
    }

    public boolean isReadAllowed(Object entity, ISecurityScope[] securityScopes) {
        return getPrivileges(entity, securityScopes).isReadAllowed();
    }

    public boolean isExecuteAllowed(Object entity, ISecurityScope[] securityScopes) {
        return getPrivileges(entity, securityScopes).isExecuteAllowed();
    }

    public IPrivilegeOfService getPrivileges(Object entity, ISecurityScope[] securityScopes) {
        IObjRef objRef = oriHelper.entityToObjRef(entity, true);
        IObjRef[] objRefs = new IObjRef[] { objRef };

        try {
            List<IPrivilegeOfService> result = getPrivileges(objRefs, securityScopes);
            return result.get(0);
        } catch (Exception e) {
            throw RuntimeExceptionUtil.mask(e);
        }
    }

    protected SingleCacheOnDemandProvider createCacheProvider() {
        return new SingleCacheOnDemandProvider() {
            @Override
            public void dispose() {
                // intended blank
            }

            @Override
            protected ICache resolveCurrentCache() {
                return getOrCreatePrivilegeCache();
            }
        };
    }

    @Override
    public List<IPrivilegeOfService> getPrivileges(IObjRef[] objRefs, final ISecurityScope[] securityScopes) {
        var cacheProviderForSecurityChecks = createCacheProvider();
        try {
            var rollback = StateRollback.chain(chain -> {
                chain.append(cacheContext.pushCache(cacheProviderForSecurityChecks));
                getAuthorization();
                chain.append(securityActivation.pushWithoutSecurity());
            });
            try {
                return getPrivilegesIntern(objRefs, securityScopes);
            } finally {
                rollback.rollback();
            }
        } finally {
            cacheProviderForSecurityChecks.dispose();
        }
    }

    @Override
    public List<ITypePrivilegeOfService> getPrivilegesOfTypes(Class<?>[] entityTypes, final ISecurityScope[] securityScopes) {
        var cacheProviderForSecurityChecks = createCacheProvider();
        try {
            var rollback = StateRollback.chain(chain -> {
                chain.append(cacheContext.pushCache(cacheProviderForSecurityChecks));
                getAuthorization();
                chain.append(securityActivation.pushWithoutSecurity());
            });
            try {
                return getPrivilegesOfTypesIntern(entityTypes, securityScopes);
            } finally {
                rollback.rollback();
            }
        } finally {
            cacheProviderForSecurityChecks.dispose();
        }
    }

    protected IObjRef[] filterAllowedEntityTypes(IObjRef[] objRefs, ISet<Class<?>> requestedTypes, Class<?>[] requestedTypesArray, ISecurityScope[] securityScopes) {
        List<ITypePrivilegeOfService> typePrivileges = getPrivilegesOfTypesIntern(requestedTypesArray, securityScopes);
        for (int a = typePrivileges.size(); a-- > 0; ) {
            ITypePrivilegeOfService typePrivilege = typePrivileges.get(a);
            if (Boolean.FALSE.equals(typePrivilege.isReadAllowed())) {
                // the read privilege is explicitly denied we filter the corresponding ObjRefs
                requestedTypes.remove(typePrivilege.getEntityType());
            }
        }
        if (requestedTypes.size() == requestedTypesArray.length) {
            // all requested entity types are allowed to read (in principle)
            return objRefs;
        }
        // at least one type is not allowed for reading so we remove those ObjRefs from the request
        IObjRef[] newObjRefs = new IObjRef[objRefs.length];
        for (int a = 0, size = objRefs.length; a < size; a++) {
            IObjRef objRef = objRefs[a];
            if (objRef == null) {
                continue;
            }
            if (requestedTypes.contains(objRef.getRealType())) {
                newObjRefs[a] = objRef;
            }
        }
        return newObjRefs;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    List<IPrivilegeOfService> getPrivilegesIntern(IObjRef[] objRefs, ISecurityScope[] securityScopes) {
        long start = System.currentTimeMillis();
        IPrefetchHelper prefetchHelper = this.prefetchHelper;
        HashSet<Class<?>> requestedTypes = new HashSet<>();
        for (int a = 0, size = objRefs.length; a < size; a++) {
            IObjRef objRef = objRefs[a];
            if (objRef == null) {
                continue;
            }
            Class<?> realType = objRef.getRealType();
            requestedTypes.add(realType);
        }
        Class<?>[] requestedTypesArray = requestedTypes.toArray(Class.class);
        objRefs = filterAllowedEntityTypes(objRefs, requestedTypes, requestedTypesArray, securityScopes);
        if (requestedTypes.size() != requestedTypesArray.length) {
            requestedTypesArray = requestedTypes.toArray(Class.class);
        }
        HashSet<Class<?>> entityTypeWithPermissionRule = HashSet.<Class<?>>create(requestedTypesArray.length);
        boolean hasAnEntityTypeWithoutPermissionRule = false;
        IPrefetchConfig prefetchConfig = null;
        for (Class<?> requestedType : requestedTypesArray) {
            IList<IEntityPermissionRule<?>> extensions = getEntityPermissionRules(requestedType);
            if (extensions.isEmpty()) {
                hasAnEntityTypeWithoutPermissionRule = true;
                continue;
            }
            entityTypeWithPermissionRule.add(requestedType);
            if (prefetchConfig == null) {
                prefetchConfig = prefetchHelper.createPrefetch();
            }
            for (int a = 0, size = extensions.size(); a < size; a++) {
                IEntityPermissionRule extension = extensions.get(a);
                extension.buildPrefetchConfig(requestedType, prefetchConfig);
            }
        }
        boolean isObjRefsToCheckEmpty = false;
        IObjRef[] objRefsToCheck = objRefs;
        if (hasAnEntityTypeWithoutPermissionRule) {
            // filter all objRefs which makes no sense to load them here because there is no rule
            // configured working on this entity instance
            // this means there is neither a row-level nor cell-level security necessary
            // this optimization here can result in a major performance boost
            objRefsToCheck = new IObjRef[objRefs.length];
            isObjRefsToCheckEmpty = true;
            for (int a = objRefs.length; a-- > 0; ) {
                IObjRef objRef = objRefs[a];
                if (objRef == null || !entityTypeWithPermissionRule.contains(objRef.getRealType())) {
                    continue;
                }
                objRefsToCheck[a] = objRef;
                isObjRefsToCheckEmpty = false;
            }
        }
        @SuppressWarnings("unused") IPrefetchState prefetchState = null;
        IList<Object> entitiesToCheck = null;
        if (!isObjRefsToCheckEmpty) {
            if (prefetchConfig == null) {
                prefetchConfig = prefetchHelper.createPrefetch();
            }
            entitiesToCheck = cache.getObjects(objRefsToCheck, CacheDirective.returnMisses());
            IPrefetchHandle prefetchHandle = prefetchConfig.build();
            prefetchState = prefetchHandle.prefetch(entitiesToCheck);
        }
        ArrayList<IPrivilegeOfService> privilegeResults = new ArrayList<>();

        IAuthorization authorization = getAuthorization();
        EntityPermissionEvaluation pe =
                new EntityPermissionEvaluation(securityScopes, isDefaultCreatePrivilege, isDefaultReadPrivilege, isDefaultUpdatePrivilege, isDefaultDeletePrivilege, isDefaultExecutePrivilege,
                        isDefaultCreatePropertyPrivilege, isDefaultReadPropertyPrivilege, isDefaultUpdatePropertyPrivilege, isDefaultDeletePropertyPrivilege);

        for (int a = 0, size = objRefs.length; a < size; a++) {
            IObjRef objRef = objRefs[a];
            if (objRef == null) {
                continue;
            }
            Class<?> entityType = objRef.getRealType();

            pe.reset();
            if (authorization != null) {
                applyEntityTypePermission(pe, authorization, entityType, securityScopes);
            }
            if (objRefsToCheck[a] != null) {
                Object entity = entitiesToCheck.get(a);
                if (entity != null) {
                    IList<IEntityPermissionRule<?>> extensions = entityPermissionRules.getExtensions(entityType);
                    for (int c = 0, sizeC = extensions.size(); c < sizeC; c++) {
                        IEntityPermissionRule extension = extensions.get(c);
                        extension.evaluatePermissionOnInstance(objRef, entity, authorization, securityScopes, pe);
                    }
                } else {
                    // an entity which can not be read even without active security is not valid
                    pe.denyEach();
                }
            }
            if (securityScopes.length > 1) {
                throw new UnsupportedOperationException("Multiple scopes at the same time not yet supported");
            }
            ScopedEntityPermissionEvaluation[] spes = pe.getSpes();

            for (int b = 0, sizeB = securityScopes.length; b < sizeB; b++) {
                ISecurityScope scope = securityScopes[b];
                ScopedEntityPermissionEvaluation spe = spes[b];

                PrivilegeOfService privilegeResult = buildPrivilegeResult(objRef, pe, scope, spe);
                privilegeResults.add(privilegeResult);
            }
        }
        logRequest(authorization, privilegeResults, start);
        return privilegeResults;
    }

    protected void logRequest(IAuthorization authorization, IList<IPrivilegeOfService> privilegeResults, long start) {
        if (!log.isDebugEnabled()) {
            return;
        }
        PrivilegeOfService[] debugResult = privilegeResults.toArray(PrivilegeOfService.class);

        long spent = System.currentTimeMillis() - start;
        Arrays.sort(debugResult, new Comparator<PrivilegeOfService>() {
            @Override
            public int compare(PrivilegeOfService o1, PrivilegeOfService o2) {
                IObjRef o1ref = o1.getReference();
                IObjRef o2ref = o2.getReference();
                int compare = o1ref.getRealType().getName().compareTo(o2ref.getRealType().getName());
                if (compare != 0) {
                    return compare;
                }
                compare = Byte.compare(o1ref.getIdNameIndex(), o2ref.getIdNameIndex());
                if (compare != 0) {
                    return compare;
                }
                Object o1id = o1ref.getId();
                Object o2id = o2ref.getId();
                if (o1id == null) {
                    return -1;
                }
                if (o2id == null) {
                    return 1;
                }
                return o1id.toString().compareTo(o2id.toString());
            }
        });
        StringBuilder sb = new StringBuilder("Resolved " + debugResult.length + " permissions for");
        if (authorization != null) {
            sb.append(" user with sid '").append(authorization.getSID()).append("'");
        } else {
            sb.append(" anonymous user");
        }
        sb.append(" (").append(spent).append("ms):");
        for (PrivilegeOfService privilegeResult : debugResult) {
            if (sb != null) {
                if (sb.length() > 0) {
                    sb.append("\n\t");
                }
                sb.append(privilegeResult.getReference()).append(' ');
                privilegeResult.toString(sb);
            }
        }
        log.debug(sb);
    }

    protected IAuthorization getAuthorization() {
        ISecurityContext securityContext = securityContextHolder.getContext();
        IAuthorization authorization = securityContext != null ? securityContext.getAuthorization() : null;
        if (authorization != null) {
            return authorization;
        }
        if (authorizationProcess == null) {
            return null;
        }
        authorizationProcess.tryAuthorization();
        securityContext = securityContextHolder.getContext();
        return securityContext != null ? securityContext.getAuthorization() : null;
    }

    List<ITypePrivilegeOfService> getPrivilegesOfTypesIntern(Class<?>[] entityTypes, ISecurityScope[] securityScopes) {
        ArrayList<ITypePrivilegeOfService> privilegeResults = new ArrayList<>();

        IAuthorization authorization = getAuthorization();
        EntityPermissionEvaluation pe =
                new EntityPermissionEvaluation(securityScopes, isDefaultCreatePrivilege, isDefaultReadPrivilege, isDefaultUpdatePrivilege, isDefaultDeletePrivilege, isDefaultExecutePrivilege,
                        isDefaultCreatePropertyPrivilege, isDefaultReadPropertyPrivilege, isDefaultUpdatePropertyPrivilege, isDefaultDeletePropertyPrivilege);

        for (int a = 0, size = entityTypes.length; a < size; a++) {
            Class<?> entityType = entityTypes[a];
            if (entityType == null) {
                privilegeResults.add(null);
                continue;
            }
            pe.reset();
            if (authorization != null) {
                applyEntityTypePermission(pe, authorization, entityType, securityScopes);
            }
            IList<IEntityTypePermissionRule> extensions = entityTypePermissionRules.getExtensions(entityType);
            for (int c = 0, sizeC = extensions.size(); c < sizeC; c++) {
                IEntityTypePermissionRule extension = extensions.get(c);
                extension.evaluatePermissionOnType(entityType, authorization, securityScopes, pe);
            }
            if (securityScopes.length > 1) {
                throw new UnsupportedOperationException("Multiple scopes at the same time not yet supported");
            }
            ScopedEntityPermissionEvaluation[] spes = pe.getSpes();

            for (int b = 0, sizeB = securityScopes.length; b < sizeB; b++) {
                ISecurityScope scope = securityScopes[b];
                ScopedEntityPermissionEvaluation spe = spes[b];

                TypePrivilegeOfService privilegeResult = buildTypePrivilegeResult(entityType, pe, scope, spe);
                privilegeResults.add(privilegeResult);
            }
        }
        return privilegeResults;
    }

    protected void applyEntityTypePermission(EntityPermissionEvaluation pe, IAuthorization authorization, Class<?> entityType, ISecurityScope[] securityScopes) {

        ITypePrivilege entityTypePrivilege = authorization.getEntityTypePrivilege(entityType, securityScopes);
        if (entityTypePrivilege.isCreateAllowed() != null) {
            if (entityTypePrivilege.isCreateAllowed()) {
                pe.allowCreate();
            } else {
                pe.denyCreate();
            }
        }
        if (entityTypePrivilege.isUpdateAllowed() != null) {
            if (entityTypePrivilege.isUpdateAllowed()) {
                pe.allowUpdate();
            } else {
                pe.denyUpdate();
            }
        }
        if (entityTypePrivilege.isDeleteAllowed() != null) {
            if (entityTypePrivilege.isDeleteAllowed()) {
                pe.allowDelete();
            } else {
                pe.denyDelete();
            }
        }
        if (entityTypePrivilege.isExecuteAllowed() != null) {
            if (entityTypePrivilege.isExecuteAllowed()) {
                pe.allowExecute();
            } else {
                pe.denyExecute();
            }
        }
        if (entityTypePrivilege.isReadAllowed() != null) {
            if (entityTypePrivilege.isReadAllowed()) {
                pe.allowRead();
            } else {
                pe.denyRead();
            }
        }
        ITypePropertyPrivilege defaultPropertyPrivilegeIfValid = entityTypePrivilege.getDefaultPropertyPrivilegeIfValid();
        if (defaultPropertyPrivilegeIfValid != null) {
            return;
        }
        IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType);
        Member[] primitiveMembers = metaData.getPrimitiveMembers();
        for (int primitiveIndex = primitiveMembers.length; primitiveIndex-- > 0; ) {
            ITypePropertyPrivilege propertyPrivilege = entityTypePrivilege.getPrimitivePropertyPrivilege(primitiveIndex);
            pe.applyTypePropertyPrivilege(primitiveMembers[primitiveIndex].getName(), propertyPrivilege);
        }
        RelationMember[] relationMembers = metaData.getRelationMembers();
        for (int relationIndex = relationMembers.length; relationIndex-- > 0; ) {
            ITypePropertyPrivilege propertyPrivilege = entityTypePrivilege.getRelationPropertyPrivilege(relationIndex);
            pe.applyTypePropertyPrivilege(relationMembers[relationIndex].getName(), propertyPrivilege);
        }
    }

    protected PrivilegeOfService buildPrivilegeResult(IObjRef objRef, EntityPermissionEvaluation pe, ISecurityScope scope, ScopedEntityPermissionEvaluation spe) {
        PrivilegeHandle ph = new PrivilegeHandle();
        ph.applyIfNull(spe);
        ph.applyIfNull(pe);
        ph.applyIfNull(isDefaultCreatePrivilege, isDefaultReadPrivilege, isDefaultUpdatePrivilege, isDefaultDeletePrivilege, isDefaultExecutePrivilege);

        boolean hasPropertyPrivileges = pe.getPropertyPermissions().size() > 0 || (spe != null && spe.getPropertyPermissions().size() > 0);

        PrivilegeOfService privilegeResult = new PrivilegeOfService();

        if (hasPropertyPrivileges) {
            HashSet<String> propertyNamesSet = new HashSet<>(pe.getPropertyPermissions().keySet());
            if (spe != null) {
                propertyNamesSet.addAll(spe.getPropertyPermissions().keySet());
            }
            String[] propertyNames = propertyNamesSet.toArray(String.class);
            IPropertyPrivilegeOfService[] propertyPrivileges = new IPropertyPrivilegeOfService[propertyNames.length];
            for (int a = 0, size = propertyNames.length; a < size; a++) {
                String propertyName = interningFeature.intern(propertyNames[a]);
                PrivilegeHandle propPH = new PrivilegeHandle();
                propPH.applyPropertySpecifics(spe, propertyName);
                propPH.applyPropertySpecifics(pe, propertyName);
                propPH.applyIfNull(ph);
                propertyNames[a] = propertyName;
                propertyPrivileges[a] = PropertyPrivilegeOfService.create(propPH.create.booleanValue(), propPH.read.booleanValue(), propPH.update.booleanValue(), propPH.delete.booleanValue());
            }
            privilegeResult.setPropertyPrivileges(propertyPrivileges);
            privilegeResult.setPropertyPrivilegeNames(propertyNames);
        }
        privilegeResult.setReference(objRef);
        privilegeResult.setSecurityScope(scope);
        privilegeResult.setCreateAllowed(ph.create);
        privilegeResult.setReadAllowed(ph.read);
        privilegeResult.setUpdateAllowed(ph.update);
        privilegeResult.setDeleteAllowed(ph.delete);
        privilegeResult.setExecuteAllowed(ph.execute);
        return privilegeResult;
    }

    protected TypePrivilegeOfService buildTypePrivilegeResult(Class<?> entityType, EntityPermissionEvaluation pe, ISecurityScope scope, ScopedEntityPermissionEvaluation spe) {
        PrivilegeHandle ph = new PrivilegeHandle();
        ph.applyIfNull(spe);
        ph.applyIfNull(pe);
        boolean hasPropertyPrivileges = pe.getPropertyPermissions().size() > 0 || (spe != null && spe.getPropertyPermissions().size() > 0);

        TypePrivilegeOfService privilegeResult = new TypePrivilegeOfService();

        if (hasPropertyPrivileges) {
            HashSet<String> propertyNamesSet = new HashSet<>(pe.getPropertyPermissions().keySet());
            if (spe != null) {
                propertyNamesSet.addAll(spe.getPropertyPermissions().keySet());
            }
            String[] propertyNames = propertyNamesSet.toArray(String.class);
            ITypePropertyPrivilegeOfService[] propertyPrivileges = new ITypePropertyPrivilegeOfService[propertyNames.length];
            for (int a = 0, size = propertyNames.length; a < size; a++) {
                String propertyName = interningFeature.intern(propertyNames[a]);
                PrivilegeHandle propPH = new PrivilegeHandle();
                propPH.applyPropertySpecifics(spe, propertyName);
                propPH.applyPropertySpecifics(pe, propertyName);
                propPH.applyIfNull(ph);
                propertyNames[a] = propertyName;
                propertyPrivileges[a] = TypePropertyPrivilegeOfService.create(propPH.create, propPH.read, propPH.update, propPH.delete);
            }
            privilegeResult.setPropertyPrivileges(propertyPrivileges);
            privilegeResult.setPropertyPrivilegeNames(propertyNames);
        }
        privilegeResult.setEntityType(entityType);
        privilegeResult.setSecurityScope(scope);
        privilegeResult.setCreateAllowed(ph.create);
        privilegeResult.setReadAllowed(ph.read);
        privilegeResult.setUpdateAllowed(ph.update);
        privilegeResult.setDeleteAllowed(ph.delete);
        privilegeResult.setExecuteAllowed(ph.execute);
        return privilegeResult;
    }

    @Override
    @SecurityContext(SecurityContextType.NOT_REQUIRED)
    public <T> void registerEntityPermissionRule(IEntityPermissionRule<? super T> entityPermissionRule, Class<T> entityType) {
        entityPermissionRules.register(entityPermissionRule, entityType);
        eventDispatcher.dispatchEvent(ClearAllCachedPrivilegesEvent.getInstance());
        eventDispatcher.dispatchEvent(new EntityPermissionRuleAddedEvent(entityPermissionRule, entityType));
    }

    @Override
    @SecurityContext(SecurityContextType.NOT_REQUIRED)
    public <T> void unregisterEntityPermissionRule(IEntityPermissionRule<? super T> entityPermissionRule, Class<T> entityType) {
        entityPermissionRules.unregister(entityPermissionRule, entityType);
        eventDispatcher.dispatchEvent(ClearAllCachedPrivilegesEvent.getInstance());
        eventDispatcher.dispatchEvent(new EntityPermissionRuleRemovedEvent(entityPermissionRule, entityType));
    }

    @Override
    public IList<IEntityPermissionRule<?>> getEntityPermissionRules(Class<?> entityType) {
        return entityPermissionRules.getExtensions(entityType);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public ILinkedMap<Class<?>, IList<IEntityPermissionRule<?>>> getAllEntityPermissionRules() {
        LinkedHashMap<Class<?>, IList<IEntityPermissionRule<?>>> allEntityPermissionRules = new LinkedHashMap<>();
        for (Entry<Class<?>, Object> entry : entityPermissionRules) {
            Class<?> entityType = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Collection) {
                allEntityPermissionRules.put(entityType, new ArrayList<IEntityPermissionRule<?>>((Collection) value));
            } else {
                allEntityPermissionRules.put(entityType, new ArrayList<IEntityPermissionRule<?>>(new Object[] { value }));
            }
        }
        return allEntityPermissionRules;
    }

    @Override
    @SecurityContext(SecurityContextType.NOT_REQUIRED)
    public void registerEntityTypePermissionRule(IEntityTypePermissionRule entityTypePermissionRule, Class<?> entityType) {
        entityTypePermissionRules.register(entityTypePermissionRule, entityType);
        eventDispatcher.dispatchEvent(ClearAllCachedPrivilegesEvent.getInstance());
        eventDispatcher.dispatchEvent(new EntityTypePermissionRuleAddedEvent(entityTypePermissionRule, entityType));
    }

    @Override
    @SecurityContext(SecurityContextType.NOT_REQUIRED)
    public void unregisterEntityTypePermissionRule(IEntityTypePermissionRule entityTypePermissionRule, Class<?> entityType) {
        entityTypePermissionRules.unregister(entityTypePermissionRule, entityType);
        eventDispatcher.dispatchEvent(ClearAllCachedPrivilegesEvent.getInstance());
        eventDispatcher.dispatchEvent(new EntityTypePermissionRuleRemovedEvent(entityTypePermissionRule, entityType));
    }

    @Override
    public IList<IEntityTypePermissionRule> getEntityTypePermissionRules(Class<?> entityType) {
        return entityTypePermissionRules.getExtensions(entityType);
    }

    public static class PrivilegeServiceForkProcessor implements IForkProcessor {
        @Autowired
        protected IPrivilegeService privilegeService;

        @Autowired
        protected ISecurityActivation securityActivation;

        @Override
        public Object resolveOriginalValue(Object bean, String fieldName, ThreadLocal<?> fieldValueTL) {
            if (!securityActivation.isFilterActivated()) {
                return null;
            }
            return ((PrivilegeService) privilegeService).getOrCreatePrivilegeCache();
        }

        @Override
        public Object createForkedValue(Object value) {
            return value;
        }

        @Override
        public void returnForkedValue(Object value, Object forkedValue) {
        }
    }
}
