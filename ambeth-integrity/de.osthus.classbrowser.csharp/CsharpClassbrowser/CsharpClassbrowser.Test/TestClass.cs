using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace CsharpClassbrowser
{
    public class TestClass
    {
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
