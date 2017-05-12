using De.Osthus.Ambeth.Annotation;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Util;
using System;
using System.Reflection;

namespace De.Osthus.Ambeth.Proxy
{
    public class MethodLevelBehavior<T> : IMethodLevelBehavior<T>
    {
        private static readonly IMethodLevelBehavior noBehavior = new NoBehavior();

        public class BehaviorKey
        {
            private readonly Type beanType;

            private readonly Type behaviourType;

            public BehaviorKey(Type beanType, Type behaviourType)
            {
                this.beanType = beanType;
                this.behaviourType = behaviourType;
            }

            public override bool Equals(Object obj)
            {
                if (Object.ReferenceEquals(obj, this))
                {
                    return true;
                }
                if (!(obj is BehaviorKey))
                {
                    return false;
                }
                BehaviorKey other = (BehaviorKey)obj;
                return beanType.Equals(other.beanType) && behaviourType.Equals(other.behaviourType);
            }

            public override int GetHashCode()
            {
                return beanType.GetHashCode() ^ behaviourType.GetHashCode();
            }
        }

        private static readonly SmartCopyMap<BehaviorKey, IMethodLevelBehavior> beanTypeToBehavior = new SmartCopyMap<BehaviorKey, IMethodLevelBehavior>(0.5f);

        public static IMethodLevelBehavior<T> Create<A>(Type beanType, AnnotationCache<A> annotationCache, Type behaviourType,
                IBehaviorTypeExtractor<A, T> behaviourTypeExtractor, IBeanContextFactory beanContextFactory, IServiceContext beanContext) where A : Attribute
        {
            BehaviorKey key = new BehaviorKey(beanType, behaviourType);

            IMethodLevelBehavior<T> behavior = (IMethodLevelBehavior<T>)beanTypeToBehavior.Get(key);
            if (behavior != null)
            {
                if (behavior == noBehavior)
                {
                    return null;
                }
                return behavior;
            }
            A annotation = annotationCache.GetAnnotation(beanType);
            if (annotation == null)
            {
                beanTypeToBehavior.Put(key, noBehavior);
                return null;
            }
            T defaultBehaviour = behaviourTypeExtractor.ExtractBehaviorType(annotation);
            MethodLevelHashMap<T> methodLevelBehaviour = null;

            MethodInfo[] methods = ReflectUtil.GetMethods(beanType);
            for (int a = methods.Length; a-- > 0; )
            {
                MethodInfo method = methods[a];
                A annotationOnMethod = annotationCache.GetAnnotation(method);
                if (annotationOnMethod != null)
                {
                    if (methodLevelBehaviour == null)
                    {
                        methodLevelBehaviour = new MethodLevelHashMap<T>();
                    }
                    T behaviourTypeOnMethod = behaviourTypeExtractor.ExtractBehaviorType(annotationOnMethod);
                    if (behaviourTypeOnMethod != null)
                    {
                        methodLevelBehaviour.Put(method, behaviourTypeOnMethod);
                    }
                }
            }
            if (methodLevelBehaviour == null)
            {
                methodLevelBehaviour = new MethodLevelHashMap<T>(0);
            }
            behavior = new MethodLevelBehavior<T>(defaultBehaviour, methodLevelBehaviour);
            beanTypeToBehavior.Put(key, behavior);
            return behavior;
        }

        protected readonly T defaultBehaviour;

        protected readonly MethodLevelHashMap<T> methodLevelBehaviour;

        public MethodLevelBehavior(T defaultBehaviour, MethodLevelHashMap<T> methodLevelBehaviour)
        {
            this.defaultBehaviour = defaultBehaviour;
            this.methodLevelBehaviour = methodLevelBehaviour;
        }

        public T GetDefaultBehaviour()
        {
            return defaultBehaviour;
        }

        public MethodLevelHashMap<T> GetMethodLevelBehaviour()
        {
            return methodLevelBehaviour;
        }

        public T GetBehaviourOfMethod(MethodInfo method)
        {
            T behaviourOfMethod = methodLevelBehaviour.Get(method);

            if (behaviourOfMethod == null)
            {
                behaviourOfMethod = defaultBehaviour;
            }
            return behaviourOfMethod;
        }

        Object IMethodLevelBehavior.GetDefaultBehaviour()
        {
            return GetDefaultBehaviour();
        }

        Object IMethodLevelBehavior.GetBehaviourOfMethod(MethodInfo method)
        {
            return GetBehaviourOfMethod(method);
        }
    }
}