using System;
using System.Text;
using De.Osthus.Ambeth.Copy;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Testutil;
using De.Osthus.Ambeth.Util;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace De.Osthus.Ambeth.Ioc
{
    [TestClass]
    [TestModule(typeof(ObjectCopierModule))]
    public class ObjectCopierTest : AbstractIocTest
    {
        [LogInstance]
        public new ILogger Log { private get; set; }

        public IObjectCopier ObjectCopier { protected get; set; }

        public override void AfterPropertiesSet()
        {
            base.AfterPropertiesSet();

            ParamChecker.AssertNotNull(ObjectCopier, "ObjectCopier");
        }

        [TestMethod]
        public void cloneInteger()
        {
            InitManually(GetType());
            int? original = 5;
            int? clone = ObjectCopier.Clone(original);
            Assert.AssertEquals(original, clone);
        }

        [TestMethod]
        public void cloneLong()
        {
            InitManually(GetType());
            long? original = 5;
            long? clone = ObjectCopier.Clone(original);
            Assert.AssertEquals(original, clone);
        }

        [TestMethod]
        public void cloneDouble()
        {
            InitManually(GetType());
            Double original = 5;
            Double clone = ObjectCopier.Clone(original);
            Assert.AssertEquals(original, clone);
        }

        [TestMethod]
        public void cloneFloat()
        {
            InitManually(GetType());
            Single original = 5;
            Single clone = ObjectCopier.Clone(original);
            Assert.AssertEquals(original, clone);
        }

        [TestMethod]
        public void cloneByte()
        {
            InitManually(GetType());
            byte? original = 5;
            byte? clone = ObjectCopier.Clone(original);
            Assert.AssertEquals(original, clone);
        }

        [TestMethod]
        public void cloneCharacter()
        {
            InitManually(GetType());
            char? original = (char)5;
            char? clone = ObjectCopier.Clone(original);
            Assert.AssertEquals(original, clone);
        }

        [TestMethod]
        public void cloneBoolean()
        {
            InitManually(GetType());
            bool original = true;
            bool clone = ObjectCopier.Clone(original);
            Assert.AssertEquals(original, clone);
        }

        [TestMethod]
        public void cloneDate()
        {
            InitManually(GetType());
            DateTime original = new DateTime(DateTime.Now.Ticks - 1000);
            DateTime clone = ObjectCopier.Clone(original);
            Assert.AssertNotSame(original, clone);
            Assert.AssertEquals(original, clone);
        }

        [TestMethod]
        public void cloneMaterial()
        {
            InitManually(GetType());
            StringBuilder original = new StringBuilder("abc");
            StringBuilder clone = ObjectCopier.Clone(original);
            Assert.AssertNotSame(original, clone);
            Assert.AssertEquals(original.ToString(), clone.ToString());
        }

        [TestMethod]
        public void cloneByteArrayNative()
        {
            InitManually(GetType());
            byte[] original = new byte[] { 5, 4, 3, 2, 1 };
            byte[] clone = ObjectCopier.Clone(original);
            Assert.AssertNotSame(original, clone);
            CollectionAssert.AreEqual(original, clone);
        }

        [TestMethod]
        public void cloneByteArray()
        {
            InitManually(GetType());
            Byte[] original = new Byte[] { 5, 4, 3, 2, 1 };
            Byte[] clone = ObjectCopier.Clone(original);
            Assert.AssertNotSame(original, clone);
            CollectionAssert.AreEqual(original, clone);
        }

        [TestMethod]
        public void cloneArrayOfArrays()
        {
            InitManually(GetType());
            Object[][] original = new Object[][] { new Object[] { (int)5 }, new Object[] { (long)6, (Double)7 } };
            Object[][] clone = ObjectCopier.Clone(original);
            Assert.AssertNotSame(original, clone);
            for (int a = original.Length; a-- > 0; )
            {
                CollectionAssert.AreEqual(original[a], clone[a]);
            }
        }
    }
}