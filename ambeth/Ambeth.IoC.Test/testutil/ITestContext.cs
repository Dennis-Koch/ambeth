using System;
using System.IO;

namespace De.Osthus.Ambeth.Testutil
{
    public interface ITestContext
    {
	    String GetContextFile(String fileName);

        String GetContextFile(String fileName, Type testClass);
    }
}