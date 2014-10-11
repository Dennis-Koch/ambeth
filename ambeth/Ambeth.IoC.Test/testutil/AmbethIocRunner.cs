using De.Osthus.Ambeth.Ioc;
using System;
using De.Osthus.Ambeth.Log;
using System.Reflection;
using De.Osthus.Ambeth.Config;
using System.Collections.Generic;
using De.Osthus.Ambeth.Annotation;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Ioc.Threadlocal;
using System.Collections;

namespace De.Osthus.Ambeth.Testutil
{
    public class AmbethIocRunner : BlockJUnit4ClassRunner
    {
        protected bool hasContextBeenRebuildForThisTest;

        protected bool isRebuildContextForThisTestRecommended;

        protected IServiceContext testClassLevelContext;

        protected IServiceContext beanContext;

        protected Object originalTestInstance;

        protected Type testClass;

        public AmbethIocRunner(Type testClass, Object test)
        {
            this.testClass = testClass;
            originalTestInstance = test;
        }

        public Type GetTestType()
        {
            return testClass;
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
    			IThreadLocalCleanupController tlCleanupController = testClassLevelContext.GetService<IThreadLocalCleanupController>();
                testClassLevelContext.Dispose();
                testClassLevelContext = null;
                beanContext = null;
                tlCleanupController.CleanupThreadLocal();
            }
        }

        protected IList<Type> BuildTestModuleList(MethodInfo frameworkMethod)
	    {
		    IList<IAnnotationInfo<TestModule>> testModulesList = FindAnnotations<TestModule>(testClass, frameworkMethod);

		    List<Type> moduleList = new List<Type>();
            foreach (IAnnotationInfo<TestModule> testModuleItem in testModulesList)
		    {
			    TestModule testFrameworkModule = testModuleItem.Annotation;
                moduleList.AddRange(testFrameworkModule.Value);
		    }
            return moduleList;
	    }

	    protected IList<Type> BuildFrameworkTestModuleList(MethodInfo frameworkMethod)
	    {
            IList<IAnnotationInfo<TestFrameworkModule>> testFrameworkModulesList = FindAnnotations<TestFrameworkModule>(testClass,
                    frameworkMethod);

		    List<Type> frameworkModuleList = new List<Type>();
            foreach (IAnnotationInfo<TestFrameworkModule> testModuleItem in testFrameworkModulesList)
		    {
			    TestFrameworkModule testFrameworkModule = testModuleItem.Annotation;
			    frameworkModuleList.AddRange(testFrameworkModule.Value);
		    }
		    return frameworkModuleList;
	    }

        public void RebuildContext(MethodInfo frameworkMethod)
        {
            DisposeContext();
            Properties.ResetApplication();
            Properties.LoadBootstrapPropertyFile();

            Properties baseProps = new Properties(Properties.Application);

            ExtendProperties(frameworkMethod, baseProps);

            LinkedHashSet<Type> testClassLevelTestFrameworkModulesList = new LinkedHashSet<Type>();
            LinkedHashSet<Type> testClassLevelTestModulesList = new LinkedHashSet<Type>();

            testClassLevelTestModulesList.AddAll(BuildTestModuleList(frameworkMethod));
            testClassLevelTestFrameworkModulesList.AddAll(BuildFrameworkTestModuleList(frameworkMethod));

            Type[] frameworkModules = testClassLevelTestFrameworkModulesList.ToArray();
            Type[] applicationModules = testClassLevelTestModulesList.ToArray();

            testClassLevelContext = BeanContextFactory.CreateBootstrap(baseProps);
            bool success = false;
            try
            {
                IServiceContext currentBeanContext = testClassLevelContext;
                if (frameworkModules.Length > 0)
                {
                    currentBeanContext = currentBeanContext.CreateService(delegate(IBeanContextFactory childContextFactory)
                        {
                            RebuildContextDetails(childContextFactory);
                        }, frameworkModules);
                }
                if (applicationModules.Length > 0)
                {
                    currentBeanContext = currentBeanContext.CreateService(applicationModules);
                }
                currentBeanContext.RegisterWithLifecycle(originalTestInstance).Finish();
                beanContext = currentBeanContext;
                success = true;
            }
            finally
            {
                if (!success && testClassLevelContext != null)
			    {
				    testClassLevelContext.GetService<IThreadLocalCleanupController>().CleanupThreadLocal();
			    }
            }
        }

        protected virtual IList<TestProperties> GetAllTestProperties(MethodInfo frameworkMethod)
	    {
            IList<IAnnotationInfo<TestProperties>> testPropertiesList = FindAnnotations<TestProperties>(testClass, frameworkMethod);

		    List<TestProperties> allTestProperties = new List<TestProperties>();

		    for (int a = 0, size = testPropertiesList.Count; a < size; a++)
		    {
			    TestProperties testPropertiesItem = testPropertiesList[a].Annotation;

    		    allTestProperties.Add(testPropertiesItem);
		    }
		    return allTestProperties;
	    }

        protected virtual void ExtendProperties(MethodInfo frameworkMethod, Properties props)
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
            childContextFactory.RegisterExternalBean(new TestContext(this)).Autowireable<ITestContext>();
        }

        protected override Statement WithAfterClasses(Statement statement)
        {
            Statement withAfterClasses = base.WithAfterClasses(statement);

            return new Statement(delegate()
            {
                withAfterClasses.Invoke();
                DisposeContext();
            });
        }

        protected override Statement WithAfters(MethodInfo method, Object target, Statement statement)
        {
            Statement returningStatement = base.WithAfters(method, target, statement);
		    return new Statement(delegate()
			    {
				    if (typeof(IRunnerAware).IsAssignableFrom(target.GetType()))
				    {
					    beanContext.RegisterWithLifecycle(target).PropertyValue("Runner", this).Finish();
				    }
				    else
				    {
                        beanContext.RegisterWithLifecycle(target).Finish();
				    }
				    returningStatement();
			    });
        }

        protected override Object CreateTest()
	    {
            return originalTestInstance;
	    }

        protected override Statement MethodBlock(MethodInfo method)
	    {
		    Statement statement = base.MethodBlock(method);
		    return new Statement(delegate()
		    {
				if (!hasContextBeenRebuildForThisTest)
				{
					if (method == null || !AnnotationUtil.IsAnnotationPresent<IgnoreAttribute>(method, false))
					{
                        IList<IAnnotationInfo<TestRebuildContext>> rebuildContextList = FindAnnotations<TestRebuildContext>(testClass);
                        if (rebuildContextList.Count > 0)
                        {
                            bool rebuildContext = ((TestRebuildContext)rebuildContextList[rebuildContextList.Count - 1].Annotation).Value;
                            if (rebuildContext)
                            {
                                RebuildContext(method);
								hasContextBeenRebuildForThisTest = true;
								isRebuildContextForThisTestRecommended = false;
                            }
                        }
						if (method != null)
						{
							if (AnnotationUtil.IsAnnotationPresent<TestModule>(method, false) || AnnotationUtil.IsAnnotationPresent<TestFrameworkModule>(method, false)
									|| AnnotationUtil.IsAnnotationPresent<TestProperties>(method, false))
							{
								RebuildContext(method);
								hasContextBeenRebuildForThisTest = true;
								isRebuildContextForThisTestRecommended = false;
							}
						}
					}
				}
				if (beanContext == null || isRebuildContextForThisTestRecommended)
				{
					RebuildContext(method);
				}
				statement();
		    });
        }

        protected override void RunChild(MethodInfo method, Object notifier)
        {
            hasContextBeenRebuildForThisTest = false;
            isRebuildContextForThisTestRecommended = false;
            base.RunChild(method, notifier);
        }

        public void RunChild(MethodInfo method)
        {
            RunChild(method, null);
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