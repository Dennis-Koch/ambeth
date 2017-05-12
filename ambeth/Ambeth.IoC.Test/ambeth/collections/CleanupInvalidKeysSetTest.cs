using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Ioc.Exceptions;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Testutil;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System;
using System.Runtime.CompilerServices;

namespace De.Osthus.Ambeth.Ioc.Annotation
{
    [TestClass]
    public class CleanupInvalidKeysSetTest : AbstractIocTest, IInvalidKeyChecker<De.Osthus.Ambeth.Ioc.Annotation.CleanupInvalidKeysSetTest.TestKey>
    {
        public class TestKey : WeakReference
        {
            protected readonly int hash;

            public TestKey(String referent)
                : base(referent)
            {
                this.hash = referent.GetHashCode();
            }

            public override bool Equals(Object obj)
            {
                if (obj == this)
                {
                    return true;
                }
                if (!(obj is TestKey))
                {
                    return false;
                }
                String value = (String)Target;
                if (value == null)
                {
                    return false;
                }
                return value.Equals(((TestKey)obj).Target);
            }

            public override int GetHashCode()
            {
                return hash;
            }

            public override String ToString()
            {
                return (String)Target;
            }
        }

        [LogInstance]
        public new ILogger Log { private get; set; }

        public bool IsKeyValid(TestKey key)
        {
            return key.Target != null;
        }

        [TestMethod]
        public void InvalidKeys()
        {
            CleanupInvalidKeysSet<TestKey> set = new CleanupInvalidKeysSet<TestKey>(this);
            int count = 1000000;
            for (int a = count; a-- > 0; )
            {
                set.Add(new TestKey("key" + a));
                if (a % (count / 100) == 0)
                {
                    GC.Collect();
                }
            }
            Assert.AssertNotEquals(set.Count, count);
        }
    }
}