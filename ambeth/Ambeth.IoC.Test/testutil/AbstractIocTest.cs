using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Util;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System.Threading;
using System;
using System.IO;
using System.Collections.Generic;
using System.Text.RegularExpressions;
using System.Reflection;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Debug;
using De.Osthus.Ambeth.Ioc.Annotation;

namespace De.Osthus.Ambeth.Testutil
{
    /// <summary>
    /// Abstract test class easing usage of IOC containers in test scenarios. Isolated modules can be registered with the <code>TestModule</code> annotation. The test
    /// itself will be registered as a bean within the IOC container. Therefore it can consume any components for testing purpose and behave like a productively
    /// bean.
    /// 
    /// In addition to registering custom modules the environment can be constructed for specific testing purpose with the <code>TestProperties</code> annotation.
    /// Multiple properties can be wrapped using the <code>TestPropertiesList</code> annotation.
    /// 
    /// All annotations can be used on test class level as well as on test method level. In ambiguous scenarios the method annotations will gain precedence.
    /// </summary>
    [TestClass]
    [TestFrameworkModule(typeof(IocModule))]
    [TestProperties(Name = IocConfigurationConstants.TrackDeclarationTrace, Value = "true")]
    [TestProperties(Name = IocConfigurationConstants.DebugModeActive, Value = "true")]
    [TestProperties(Name = "ambeth.log.level.De.Osthus.Ambeth.Accessor.AccessorTypeProvider", Value = "INFO")]
    [TestProperties(Name = "ambeth.log.level.De.Osthus.Ambeth.Bytecode.Core.BytecodeEnhancer", Value = "INFO")]
    [TestProperties(Name = "ambeth.log.level.De.Osthus.Ambeth.Bytecode.Visitor.ClassWriter", Value = "INFO")]
    [TestProperties(Name = "ambeth.log.level.De.Osthus.Ambeth.Bytecode.Visitor.LogImplementationsClassVisitor", Value = "INFO")]
    [TestProperties(Name = "ambeth.log.level.De.Osthus.Ambeth.Template.PropertyChangeTemplate", Value = "INFO")]
    public abstract class AbstractIocTest : IInitializingBean, IStartingBean, IDisposableBean
    {
        public class Assert
        {
            public static void AssertNull(Object obj)
            {
                Microsoft.VisualStudio.TestTools.UnitTesting.Assert.IsNull(obj);
            }

            public static void AssertNotNull(Object obj)
            {
                Microsoft.VisualStudio.TestTools.UnitTesting.Assert.IsNotNull(obj);
            }

            public static void AssertNotSame(Object notExpected, Object value)
            {
                Microsoft.VisualStudio.TestTools.UnitTesting.Assert.IsFalse(Object.ReferenceEquals(notExpected, value));
            }

            public static void AssertSame(Object expected, Object value, String message = null)
            {
                if (message == null)
                {
                    Microsoft.VisualStudio.TestTools.UnitTesting.Assert.IsTrue(Object.ReferenceEquals(expected, value));
                }
                else
                {
                    Microsoft.VisualStudio.TestTools.UnitTesting.Assert.IsTrue(Object.ReferenceEquals(expected, value), message);
                }
            }

            public static void AssertTrue(bool actual, String message = null)
            {
                if (message == null)
                {
                    Microsoft.VisualStudio.TestTools.UnitTesting.Assert.IsTrue(actual);
                }
                else
                {
                    Microsoft.VisualStudio.TestTools.UnitTesting.Assert.IsTrue(actual, message);
                }
            }

            public static void AssertFalse(bool actual, String message = null)
            {
                if (message == null)
                {
                    Microsoft.VisualStudio.TestTools.UnitTesting.Assert.IsFalse(actual);
                }
                else
                {
                    Microsoft.VisualStudio.TestTools.UnitTesting.Assert.IsFalse(actual, message);
                }
            }

            public static void AssertEquals<T>(T expected, T actual, String message = null)
            {
                if (message == null)
                {
                    Microsoft.VisualStudio.TestTools.UnitTesting.Assert.AreEqual(expected, actual);
                }
                else
                {
                    Microsoft.VisualStudio.TestTools.UnitTesting.Assert.AreEqual(expected, actual, message);
                }
            }

            public static void AssertNotEquals<T>(T expected, T actual, String message = null)
            {
                if (message == null)
                {
                    Microsoft.VisualStudio.TestTools.UnitTesting.Assert.AreNotEqual(expected, actual);
                }
                else
                {
                    Microsoft.VisualStudio.TestTools.UnitTesting.Assert.AreNotEqual(expected, actual, message);
                }
            }

            public static void AssertNotEquals(Object expected, Object actual, String message = null)
            {
                if (message == null)
                {
                    Microsoft.VisualStudio.TestTools.UnitTesting.Assert.AreNotEqual(expected, actual);
                }
                else
                {
                    Microsoft.VisualStudio.TestTools.UnitTesting.Assert.AreNotEqual(expected, actual, message);
                }
            }

            public static void Fail(String message)
            {
                Microsoft.VisualStudio.TestTools.UnitTesting.Assert.Fail(message);
            }

            public static void IsInstanceOfType(Object value, Type expectedType)
            {
                Microsoft.VisualStudio.TestTools.UnitTesting.Assert.IsInstanceOfType(value, expectedType);
            }

            public static void IsNotInstanceOfType(Object value, Type expectedType)
            {
                Microsoft.VisualStudio.TestTools.UnitTesting.Assert.IsNotInstanceOfType(value, expectedType);
            }

            public static void AssertArrayEquals<T>(T[] a1, T[] a2)
            {
                if (ReferenceEquals(a1, a2))
                {
                    return;
                }

                if (a1 == null || a2 == null)
                {
                    Fail("One array instance is null");
                    return;
                }
                if (a1.Length != a2.Length)
                {
                    Fail("Length is not equal: " + a1.Length + " != " + a2.Length);
                    return;
                }
                for (int i = 0; i < a1.Length; i++)
                {
                    if (!Object.Equals(a1[i], a2[i]))
                    {
                        Fail("Item at index " + i + " is not equal");
                        return;
                    }
                }
            }
        }

        private static bool assemblyInitRan = false;

        private readonly AmbethIocRunner runner;

        private IServiceContext beanContext;

        [Autowired]
        public IServiceContext BeanContext
        {
            protected get
            {
                return beanContext;
            }
            set
            {
                beanContext = value;
                FlattenHierarchyProxy.Context = beanContext;
            }
        }

        public TestContext TestContext { get; set; }

        public AbstractIocTest()
        {
            runner = new AmbethIocRunner(GetType(), this);
        }

        /// <summary>
        /// Workaround to get all needed assemblies known by the AssemblyHelper.
        /// </summary>
        /// <param name="context"></param>
        [AssemblyInitialize]
        public static void RegisterAssemblies(TestContext context)
        {
            assemblyInitRan = true;
            AssemblyHelper.RegisterAssemblyFromType(typeof(IProperties)); // for xsd files from Ambeth.Util
        }

        [TestInitialize]
        public virtual void InitAutomatically()
        {
            if (!assemblyInitRan)
            {
                RegisterAssemblies(null);
            }
            InitManually(GetType());
        }

        //[TestInitialize]
        public virtual void InitManually(Type testType)
        {
            //String stackTrace = Environment.StackTrace;
            //List<String> lines = new List<String>();
            //StringReader reader = new StringReader(stackTrace);
            //while (true)
            //{
            //    String line = reader.ReadLine();
            //    if (line == null)
            //    {
            //        break;
            //    }
            //    lines.Add(line);
            //}
            //Regex regex = new Regex(" *(?:Microsoft|System).(.+)");
            //Regex fqClassNameRegex = new Regex(@" *(?:[^ ]+ +)?([^ ]+)\.([^ \.]+)\(\) +.+");
            //String fqClassNameLine = null;
            //for (int a = lines.Count; a-- > 0;)
            //{
            //    String line = lines[a];
            //    Match match = regex.Match(line);
            //    if (match.Success)
            //    {
            //        continue;
            //    }
            //    fqClassNameLine = line;
            //    break;
            //}
            String methodName = TestContext.TestName;
            AssemblyHelper.RegisterAssemblyFromType(testType);
            MethodInfo method = testType.GetMethod(methodName);
            runner.RebuildContext(method);
            BeanContext = runner.GetBeanContext();
        }

        [TestCleanup]
        public void Cleanup()
        {
            if (BeanContext != null)
            {
                BeanContext.GetRoot().Dispose();
                BeanContext = null;
            }
        }

        public virtual void AfterPropertiesSet()
        {
            // Intended blank
        }

        public virtual void AfterStarted()
        {
            // Intended blank
        }

        public virtual void Destroy()
        {
            if (Object.ReferenceEquals(FlattenHierarchyProxy.Context, BeanContext))
            {
                FlattenHierarchyProxy.Context = null;
            }
        }
    }
}