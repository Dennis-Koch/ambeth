using De.Osthus.Ambeth.Transfer;

namespace De.Osthus.Ambeth.Transfer
{
    public class Assert
    {
        public static void AssertEquals(ServiceDescription expected, ServiceDescription actual)
        {
            if (expected == null)
            {
                Microsoft.VisualStudio.TestTools.UnitTesting.Assert.IsNull(actual);
                return;
            }

            Microsoft.VisualStudio.TestTools.UnitTesting.Assert.IsNotNull(actual);
            Microsoft.VisualStudio.TestTools.UnitTesting.Assert.AreEqual(expected.ServiceName, actual.ServiceName);
            Microsoft.VisualStudio.TestTools.UnitTesting.Assert.AreEqual(expected.MethodName, actual.MethodName);
            De.Osthus.Ambeth.Testutil.AbstractIocTest.Assert.AssertArrayEquals(expected.ParamTypes, actual.ParamTypes);
            De.Osthus.Ambeth.Testutil.AbstractIocTest.Assert.AssertArrayEquals(expected.Arguments, actual.Arguments);
            De.Osthus.Ambeth.Testutil.AbstractIocTest.Assert.AssertArrayEquals(expected.SecurityScopes, actual.SecurityScopes);
        }
    }
}
