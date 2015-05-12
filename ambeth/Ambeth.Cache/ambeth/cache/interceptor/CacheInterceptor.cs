using System;
using System.Collections;
using System.Collections.Generic;
using System.Reflection;
#if !SILVERLIGHT
using Castle.DynamicProxy;
using System.Threading;
#else
using Castle.Core.Interceptor;
using Castle.DynamicProxy;
#endif
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Service;
using De.Osthus.Ambeth.Transfer;
using De.Osthus.Ambeth.Merge.Transfer;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Merge.Interceptor;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Cache.Config;
using De.Osthus.Ambeth.Model;
using De.Osthus.Ambeth.Annotation;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Cache.Model;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Metadata;

namespace De.Osthus.Ambeth.Cache.Interceptor
{
    public class CacheInterceptor : MergeInterceptor
    {
        public static readonly MethodInfo GetORIsForServiceRequestMethod = typeof(ICacheService).GetMethod("GetORIsForServiceRequest", new Type[] { typeof(IServiceDescription) });

        protected static readonly Type listType = typeof(List<Object>).GetGenericTypeDefinition();

        public static readonly ThreadLocal<bool> pauseCache = new ThreadLocal<bool>(delegate()
        {
            return false;
        });

        public class CachedAnnotationCache : AnnotationCache<CachedAttribute>
        {
            protected override bool AnnotationEquals(CachedAttribute left, CachedAttribute right)
            {
                return Object.Equals(left.Type, right.Type) && Object.Equals(left.AlternateIdName, right.AlternateIdName);
            }
        }

        [LogInstance]
	    public new ILogger Log { private get; set; }

        protected readonly CachedAnnotationCache cachedAnnotationCache = new CachedAnnotationCache();

        [Autowired]
        public ICache Cache { protected get; set; }

        [Autowired]
        public ICacheService CacheService { protected get; set; }

        [Autowired]
        public IServiceResultProcessorRegistry ServiceResultProcessorRegistry { protected get; set; }

        [Property(CacheConfigurationConstants.CacheServiceName, DefaultValue = "CacheService")]
        public String CacheServiceName { protected get; set; }

        protected override Object InterceptLoad(IInvocation invocation, Attribute annotation, Boolean? isAsyncBegin)
        {
            ServiceDescription serviceDescription;
		    IServiceResult serviceResult;
            MethodInfo method = invocation.Method;
            Object[] args = invocation.Arguments;

            CachedAttribute cached = annotation is CachedAttribute ? (CachedAttribute)annotation : null;
		    if (cached == null && pauseCache.Value)
		    {
                return base.InterceptLoad(invocation, annotation, isAsyncBegin);
		    }
		    Type returnType = method.ReturnType;
		    if (ImmutableTypeSet.IsImmutableType(returnType))
		    {
			    // No possible result which might been read by cache
                return base.InterceptLoad(invocation, annotation, isAsyncBegin);
		    }
		    if (cached == null)
		    {
                ISecurityScope[] securityScopes = SecurityScopeProvider.SecurityScopes;
			    serviceDescription = SyncToAsyncUtil.CreateServiceDescription(ServiceName, method, args, securityScopes);
                serviceResult = CacheService.GetORIsForServiceRequest(serviceDescription);
				return CreateResultObject(serviceResult, returnType, args, annotation);
		    }

		    if (args.Length != 1)
		    {
			    throw new Exception("This annotation is only allowed on methods with exactly 1 argument. Please check your "
					    + typeof(CachedAttribute).FullName + " annotation on method " + method.ToString());
		    }
		    Type entityType = cached.Type;
		    if (entityType == null || typeof(void).Equals(entityType))
		    {
                entityType = TypeInfoItemUtil.GetElementTypeUsingReflection(returnType, null);
		    }
		    if (entityType == null || typeof(void).Equals(entityType))
		    {
			    throw new Exception("Please specify a valid returnType for the " + typeof(CachedAttribute).FullName + " annotation on method "
					    + method.ToString());
		    }
       		IEntityMetaData metaData = GetSpecifiedMetaData(method, typeof(CachedAttribute), entityType);
            Member member = GetSpecifiedMember(method, typeof(CachedAttribute), metaData, cached.AlternateIdName);

            sbyte idIndex;
		    try
		    {
			    idIndex = metaData.GetIdIndexByMemberName(member.Name);
		    }
            catch (Exception e)
		    {
                throw new Exception(
                        "Member "
                                + entityType.FullName
                                + "."
                                + cached.AlternateIdName
                                + " is not configured as an alternate ID member. There must be a single-column unique contraint on the respective table column. Please check your "
                                + typeof(CachedAttribute).FullName + " annotation on method " + method.ToString(), e);
            }
            bool returnMisses = cached.ReturnMisses;
		    List<IObjRef> orisToGet = new List<IObjRef>();
            FillOrisToGet(orisToGet, args, entityType, idIndex, returnMisses);
			return CreateResultObject(orisToGet, returnType, returnMisses, annotation);
        }

        protected virtual void FillOrisToGet(IList<IObjRef> orisToGet, Object[] args, Type entityType, sbyte idIndex, bool returnMisses)
	    {
		    Object argument = args[0];
		    if (argument is IList)
		    {
			    IList list = (IList) argument;
			    for (int a = 0, size = list.Count; a < size; a++)
			    {
				    Object id = list[a];
                    if (id == null)
                    {
                        if (returnMisses)
                        {
                            orisToGet.Add(null);
                        }
                        else
                        {
                            continue;
                        }
                    }
                    ObjRef objRef = new ObjRef(entityType, idIndex, id, null);
				    orisToGet.Add(objRef);
			    }
		    }
		    else if (argument is IEnumerable)
		    {
			    IEnumerator enumerator = ((IEnumerable) argument).GetEnumerator();
			    while (enumerator.MoveNext())
			    {
				    Object id = enumerator.Current;
                    if (id == null)
                    {
                        if (returnMisses)
                        {
                            orisToGet.Add(null);
                        }
                        else
                        {
                            continue;
                        }
                    }
                    ObjRef objRef = new ObjRef(entityType, idIndex, id, null);
				    orisToGet.Add(objRef);
			    }
		    }
		    else
		    {
                ObjRef objRef = new ObjRef(entityType, idIndex, argument, null);
                orisToGet.Add(objRef);
            }
	    }

		protected Object CreateResultObject(IServiceResult serviceResult, Type expectedType, Object[] originalArgs, Attribute annotation)
	    {
		    IList<IObjRef> objRefs = serviceResult.ObjRefs;
			IList<Object> syncObjects = null;
			if (annotation is FindAttribute && ((FindAttribute)annotation).ResultType != QueryResultType.REFERENCES)
			{
				syncObjects = Cache.GetObjects(objRefs, CacheDirective.None);
			}
			return PostProcessCacheResult(objRefs, syncObjects, expectedType, serviceResult, originalArgs, annotation);
	    }

		protected virtual Object CreateResultObject(IList<IObjRef> objRefs, Type expectedType, bool returnMisses, Attribute annotation)
	    {
			IList<Object> syncObjects = Cache.GetObjects(objRefs, returnMisses ? CacheDirective.ReturnMisses : CacheDirective.None);
			return PostProcessCacheResult(objRefs, syncObjects, expectedType, null, null, annotation);
	    }

		protected virtual Object PostProcessCacheResult(IList<IObjRef> objRefs, IList<Object> cacheResult, Type expectedType, IServiceResult serviceResult, Object[] originalArgs,
			Attribute annotation)
        {
			int cacheResultSize = cacheResult != null ? cacheResult.Count : objRefs.Count;
			if (typeof(IEnumerable).IsAssignableFrom(expectedType))
			{
				Object targetCollection = ListUtil.CreateCollectionOfType(expectedType);

                MethodInfo addMethod = targetCollection.GetType().GetMethod("Add");
                Object[] parameters = new Object[1];

				if (cacheResult != null)
				{
					for (int a = 0; a < cacheResultSize; a++)
					{
						parameters[0] = cacheResult[a];
						addMethod.Invoke(targetCollection, parameters);
					}
				}
				else
				{
					for (int a = 0; a < cacheResultSize; a++)
					{
						parameters[0] = objRefs[a];
						addMethod.Invoke(targetCollection, parameters);
					}
				}
				return targetCollection;
			}
			else if (expectedType.IsArray)
			{
				Array array = Array.CreateInstance(expectedType.GetElementType(), cacheResultSize);

				if (cacheResult != null)
				{
					for (int a = 0; a < cacheResultSize; a++)
					{
						array.SetValue(cacheResult[a], a);
					}
				}
				else
				{
					for (int a = 0; a < cacheResultSize; a++)
					{
						array.SetValue(objRefs[a], a);
					}
				}
				return array;
			}
            IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(expectedType, true);
            if (metaData != null)
            {
                // It is a simple entity which can be returned directly
                if (cacheResultSize == 0)
                {
                    return null;
                }
                else if (cacheResultSize == 1)
                {
                    return cacheResult != null ? cacheResult[0] : objRefs[0];
                }
            }
            Object additionalInformation = serviceResult != null ? serviceResult.AdditionalInformation : null;
            if (additionalInformation == null)
            {
                throw new Exception("Can not convert list of " + cacheResultSize + " results from cache to type " + expectedType.FullName);
            }
            IServiceResultProcessor serviceResultProcessor = ServiceResultProcessorRegistry.GetServiceResultProcessor(expectedType);
			return serviceResultProcessor.ProcessServiceResult(additionalInformation, objRefs, cacheResult, expectedType, originalArgs, annotation);
        }
    }
}