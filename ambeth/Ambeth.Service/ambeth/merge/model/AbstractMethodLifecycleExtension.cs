using De.Osthus.Ambeth.Exceptions;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Util;
using System;
using System.Reflection;

namespace De.Osthus.Ambeth.Merge.Model
{
    public abstract class AbstractMethodLifecycleExtension : IEntityLifecycleExtension, IInitializingBean
    {
        protected static readonly Object[] EMPTY_ARGS = new Object[0];

        [LogInstance]
        public ILogger Log { private get; set; }

        [Autowired]
        public IEntityMetaDataProvider EntityMetaDataProvider { protected get; set; }

        public MethodInfo Method { protected get; set; }

        protected int methodIndex;

        public void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(Method, "method");
        }

        protected void CallMethod(Object entity, String message)
        {
            try
            {
                Method.Invoke(entity, EMPTY_ARGS);
            }
            catch (Exception e)
            {
                Type entityType = EntityMetaDataProvider.GetMetaData(entity.GetType()).EntityType;
                throw RuntimeExceptionUtil.Mask(e, "Error occured while handling " + message + " method of entity type " + entityType.FullName);
            }
        }

        public abstract void PostCreate(IEntityMetaData metaData, Object newEntity);

        public abstract void PrePersist(IEntityMetaData metaData, Object entity);

        public abstract void PostLoad(IEntityMetaData metaData, Object entity);
    }
}