using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System;

namespace CsharpClassbrowser
{
    public class TestClass : AbstractTestClass, TestInterface1, TestInterface2
    {
        public const String CONST = "test constant";

        private const int DEFAULT = 42;

        [LogInstance]
        private object log;

        [Autowired]
        protected object service;

        [Autowired(typeof(TestClass))]
        protected object service2;

        [Autowired(Name = "test", Optional = true)]
        protected object service3;

        protected object internalField;

        [TestInitialize]
        public void setInternalField(object internalField)
        {
            this.internalField = internalField;
        }
    }
}
