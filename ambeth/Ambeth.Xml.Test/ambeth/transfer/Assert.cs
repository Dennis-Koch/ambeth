
namespace De.Osthus.Ambeth.Transfer.Test
{
    public class Assert
    {
        private Assert()
        {
            // Intended blank
        }

        public static void AreEqual(ServiceDescription expected, ServiceDescription actual)
        {
            if (expected == null)
            {
                Microsoft.VisualStudio.TestTools.UnitTesting.Assert.IsNull(actual);
                return;
            }

            Microsoft.VisualStudio.TestTools.UnitTesting.Assert.IsNotNull(actual);
            Microsoft.VisualStudio.TestTools.UnitTesting.Assert.AreEqual(expected.ServiceName, actual.ServiceName);
            Microsoft.VisualStudio.TestTools.UnitTesting.Assert.AreEqual(expected.MethodName, actual.MethodName);
            AreArraysEqual(expected.ParamTypes, actual.ParamTypes);
            AreArraysEqual(expected.Arguments, actual.Arguments);
            AreArraysEqual(expected.SecurityScopes, actual.SecurityScopes);
        }

        private static void AreArraysEqual(object[] expected, object[] actual)
        {
            Microsoft.VisualStudio.TestTools.UnitTesting.Assert.AreEqual(expected.Length, actual.Length);
            for (int i = 0; i < expected.Length; i++)
            {
                Microsoft.VisualStudio.TestTools.UnitTesting.Assert.AreEqual(expected[i], actual[i]);
            }
        }
    }
}
