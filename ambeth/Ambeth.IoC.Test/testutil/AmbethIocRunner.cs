using De.Osthus.Ambeth.Ioc;
using System;
using De.Osthus.Ambeth.Log;
using System.Reflection;
using De.Osthus.Ambeth.Config;
using System.Collections.Generic;
using De.Osthus.Ambeth.Annotation;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Ioc.Factory;

namespace De.Osthus.Ambeth.Testutil
{
    public class AmbethIocRunner
    {
        protected IServiceContext testClassLevelContext;

        protected IServiceContext beanContext;

        protected Object originalTestInstance;

        protected Type testClass;

        public AmbethIocRunner(Type testClass, Object test)
        {
            this.testClass = testClass;
            originalTestInstance = test;
        }

        public IServiceContext GetBeanContext()
        {
            return beanContext;
        }

        public virtual Statement WithBeforeClasses(Statement statement)
        {
            return statement;
        }

        public void DisposeContext()
        {
            if (testClassLevelContext != null)
            {
                testClassLevelContext.Dispose();
                testClassLevelContext = null;
                beanContext = null;
            }
        }

        public void RebuildContext(MethodInfo frameworkMethod)
        {
            DisposeContext();
            Properties.ResetApplication();
            Properties.LoadBootstrapPropertyFile();

            Properties baseProps = new Properties(Properties.Application);

            ExtendProperties(frameworkMethod, baseProps);

            IList<IAnnotationInfo<TestModule>> testModulesList = FindAnnotations<TestModule>(testClass, frameworkMethod);

            IList<IAnnotationInfo<TestFrameworkModule>> testFrameworkModulesList = FindAnnotations<TestFrameworkModule>(testClass,
                    frameworkMethod);

            ISet<Type> testClassLevelTestFrameworkModulesList = new HashSet<Type>();
            ISet<Type> testClassLevelTestModulesList = new HashSet<Type>();

            foreach (IAnnotationInfo<TestFrameworkModule> testModuleItem in testFrameworkModulesList)
            {
                TestFrameworkModule testFrameworkModule = testModuleItem.Annotation;
                foreach (Type type in testFrameworkModule.Value)
                {
                    AssemblyHelper.RegisterAssemblyFromType(type);
                    testClassLevelTestFrameworkModulesList.Add(type);
                }
            }
            foreach (IAnnotationInfo<TestModule> testModuleItem in testModulesList)
            {
                TestModule testModule = testModuleItem.Annotation;
                foreach (Type type in testModule.Value)
                {
                    AssemblyHelper.RegisterAssemblyFromType(type);
                    testClassLevelTestModulesList.Add(type);
                }
            }
            Type[] frameworkModules = ListUtil.ToArray(testClassLevelTestFrameworkModulesList);
            Type[] bootstrapModules = ListUtil.ToArray(testClassLevelTestModulesList);

            testClassLevelContext = BeanContextFactory.CreateBootstrap(baseProps);
            IServiceContext currentBeanContext = testClassLevelContext;
            if (frameworkModules.Length > 0)
            {
                currentBeanContext = currentBeanContext.CreateService(delegate(IBeanContextFactory childContextFactory)
                    {
                        RebuildContextDetails(childContextFactory);
                    }, frameworkModules);
            }
            if (bootstrapModules.Length > 0)
            {
                currentBeanContext = currentBeanContext.CreateService(bootstrapModules);
            }
            currentBeanContext.RegisterWithLifecycle(originalTestInstance).Finish();
            beanContext = currentBeanContext;
        }

        protected void ExtendProperties(MethodInfo frameworkMethod, Properties props)
        {
            IList<IAnnotationInfo<TestProperties>> testPropertiesList = FindAnnotations<TestProperties>(testClass, frameworkMethod);

            foreach (IAnnotationInfo<TestProperties> testPropertiesItem in testPropertiesList)
            {
                TestProperties testProperties = testPropertiesItem.Annotation;
                Type testPropertiesType = testProperties.Type;
                if (testPropertiesType != null && !typeof(IPropertiesProvider).Equals(testPropertiesType))
                {
                    IPropertiesProvider propertiesProvider = (IPropertiesProvider)Activator.CreateInstance(testPropertiesType);
                    propertiesProvider.FillProperties(props);
                }
                String testPropertiesFile = testProperties.File;
                if (testPropertiesFile != null && testPropertiesFile.Length > 0)
                {
                    props.Load(testPropertiesFile);
                }
                String testPropertyName = testProperties.Name;
                String testPropertyValue = testProperties.Value;
                if (testPropertyName != null && testPropertyName.Length > 0)
                {
                    if (testPropertyValue != null && testPropertyValue.Length > 0)
                    {
                        // Override intended
                        props.Set(testPropertyName, testPropertyValue);
                    }
                }
            }
        }

        protected virtual void RebuildContextDetails(IBeanContextFactory childContextFactory)
        {
            // Intended blank
        }

        protected Statement WithAfterClasses(Statement statement)
        {
            Statement withAfterClasses = WithAfterClasses(statement);

            return new Statement(delegate()
            {
                withAfterClasses.Invoke();
                DisposeContext();
            });
        }

        protected virtual Statement WithAfterClassesWithinContext(Statement statement)
        {
            //		return super.withAfterClasses(statement);
            return statement;
        }

        protected void RunChild(MethodInfo method, Object notifier)
        {
            RunChildWithContext(method, notifier, false);
        }

        protected void RunChildWithContext(MethodInfo method, Object/*RunNotifier*/ notifier, bool hasContextBeenRebuild)
        {
            try
            {
                if (!hasContextBeenRebuild && (method == null || !AnnotationUtil.IsAnnotationPresent<IgnoreAttribute>(method, false)))
                {
                    IList<IAnnotationInfo<TestRebuildContext>> rebuildContextList = FindAnnotations<TestRebuildContext>(testClass);
                    if (rebuildContextList.Count > 0)
                    {
                        bool rebuildContext = ((TestRebuildContext)rebuildContextList[rebuildContextList.Count].Annotation).Value;
                        if (rebuildContext)
                        {
                            RebuildContext(method);
                        }
                    }
                }
            }
            //catch (MaskingRuntimeException e)
            //{
            //    notifier.fireTestFailure(new Failure(Description.createTestDescription(getTestClass().getJavaClass(), method.getName()), e.getMessage() == null ? e
            //            .getCause() : e));
            //    return;
            //}
            //catch (Throwable e)
            //{
            //    notifier.fireTestFailure(new Failure(Description.createTestDescription(getTestClass().getJavaClass(), method.getName()), e));
            //    return;
            //}
            finally
            {
            }
            method.Invoke(originalTestInstance, null);
            //super.runChild(method, notifier);
        }

        protected IList<IAnnotationInfo<V>> FindAnnotations<V>(Type type) where V : Attribute
        {
            return FindAnnotations<V>(type, null);
        }

        protected IList<IAnnotationInfo<V>> FindAnnotations<V>(Type type, MethodInfo method) where V : Attribute
        {
            IList<IAnnotationInfo<V>> targetList = new List<IAnnotationInfo<V>>();
            FindAnnotations<V>(type, targetList, true);

            if (method != null)
            {
                IList<V> annotations = AnnotationUtil.GetAnnotations<V>(method, true);
                for (int a = 0, size = annotations.Count; a < size; a++)
                {
                    targetList.Add(new AnnotationInfo<V>(annotations[a], method));
                }
            }
            return targetList;
        }

        protected void FindAnnotations<V>(Type type, IList<IAnnotationInfo<V>> targetList, bool isFirst) where V : Attribute
        {
            if (type == null || typeof(Object).Equals(type))
            {
                return;
            }
            if (!type.IsInterface)
            {
                FindAnnotations<V>(type.BaseType, targetList, false);
            }
            IList<V> annotations = AnnotationUtil.GetAnnotations<V>(type, false);
            for (int a = 0, size = annotations.Count; a < size; a++)
            {
                targetList.Add(new AnnotationInfo<V>(annotations[a], type));
            }
            //if (isFirst)
            //{
            //    Type[] interfaces = type.GetInterfaces();
            //    foreach (Type currInterface in interfaces)
            //    {
            //        IList<V> annotationsOfInterface = AnnotationUtil.GetAnnotations<V>(currInterface, true);
            //        for (int a = 0, size = annotationsOfInterface.Count; a < size; a++)
            //        {
            //            targetList.Add(new AnnotationInfo<V>(annotationsOfInterface[a], type));
            //        }
            //    }
            //}
        }
    }
}