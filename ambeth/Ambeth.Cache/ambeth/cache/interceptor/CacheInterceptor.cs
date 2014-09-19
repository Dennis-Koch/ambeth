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
        private static ILogger LOG = LoggerFactory.GetLogger(typeof(CacheInterceptor));

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
			    serviceDescription = SyncToAsyncUtil.CreateServiceDescription(ServiceName, method, args);
                serviceResult = CacheService.GetORIsForServiceRequest(serviceDescription);
                return CreateResultObject(serviceResult, returnType, args);
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
		    List<IObjRef> orisToGet = new List<IObjRef>();
            FillOrisToGet(orisToGet, args, entityType, idIndex);
			return CreateResultObject(orisToGet, returnType);
        }

        protected virtual void FillOrisToGet(IList<IObjRef> orisToGet, Object[] args, Type entityType, sbyte idIndex)
	    {
		    Object argument = args[0];
		    if (argument is IList)
		    {
			    IList list = (IList) argument;
			    for (int a = 0, size = list.Count; a < size; a++)
			    {
				    Object id = list[a];
				    ObjRef objRef = new ObjRef();
				    objRef.RealType = entityType;
				    objRef.Id = id;
				    objRef.IdNameIndex = idIndex;
				    orisToGet.Add(objRef);
			    }
		    }
		    else if (argument is IEnumerable)
		    {
			    IEnumerator enumerator = ((IEnumerable) argument).GetEnumerator();
			    while (enumerator.MoveNext())
			    {
				    Object id = enumerator.Current;
				    ObjRef objRef = new ObjRef();
				    objRef.RealType = entityType;
				    objRef.Id = id;
				    objRef.IdNameIndex = idIndex;
				    orisToGet.Add(objRef);
			    }
		    }
		    else
		    {
			    ObjRef objRef = new ObjRef();
                objRef.RealType = entityType;
                objRef.Id = argument;
                objRef.IdNameIndex = idIndex;
                orisToGet.Add(objRef);
            }
	    }

      	protected Object CreateResultObject(IServiceResult serviceResult, Type expectedType, Object[] originalArgs)
	    {
		    IList<IObjRef> objRefs = serviceResult.ObjRefs;
		    IList<Object> syncObjects = Cache.GetObjects(objRefs, CacheDirective.None);
		    return PostProcessCacheResult(syncObjects, expectedType, serviceResult, originalArgs);
	    }

        protected virtual Object CreateResultObject(IList<IObjRef> oris, Type expectedType)
	    {
		    IList<Object> syncObjects = Cache.GetObjects(oris, CacheDirective.None);
		    return PostProcessCacheResult(syncObjects, expectedType, null, null);
	    }

        protected virtual Object PostProcessCacheResult(IList<Object> cacheResult, Type expectedType, IServiceResult serviceResult, Object[] originalArgs)
        {
            int cacheResultSize = cacheResult.Count;
			if (typeof(IEnumerable).IsAssignableFrom(expectedType))
			{
				Object targetCollection = ListUtil.CreateCollectionOfType(expectedType);

                MethodInfo addMethod = targetCollection.GetType().GetMethod("Add");
                Object[] parameters = new Object[1];

				for (int a = 0; a < cacheResultSize; a++)
				{
                    parameters[0] = cacheResult[a];
                    addMethod.Invoke(targetCollection, parameters);
				}
				return targetCollection;
			}
			else if (expectedType.IsArray)
			{
				Array array = Array.CreateInstance(expectedType.GetElementType(), cacheResultSize);
				for (int a = 0; a < cacheResultSize; a++)
				{
                    array.SetValue(cacheResult[a], a);
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
                    return cacheResult[0];
                }
            }
            Object additionalInformation = serviceResult != null ? serviceResult.AdditionalInformation : null;
            if (additionalInformation == null)
            {
                throw new Exception("Can not convert list of " + cacheResultSize + " results from cache to type " + expectedType.FullName);
            }
            IServiceResultProcessor serviceResultProcessor = ServiceResultProcessorRegistry.GetServiceResultProcessor(expectedType);
            return serviceResultProcessor.ProcessServiceResult(additionalInformation, cacheResult, expectedType, originalArgs);
        }
    }
}