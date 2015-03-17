using System;
using System.IO;
using System.Text.RegularExpressions;

namespace De.Osthus.Ambeth.Testutil
{
    public class TestContext : ITestContext
    {
	    //private static readonly String nl = "\r\n";

	    private static readonly Regex pathSeparator = new Regex(Regex.Escape("" + Path.PathSeparator));

	    protected readonly AmbethIocRunner runner;

	    public TestContext(AmbethIocRunner runner)
	    {
		    this.runner = runner;
	    }

	    public String GetContextFile(String fileName)
	    {
		    Type testClass = runner.GetTestType();
		    return GetContextFile(fileName, testClass);
	    }

	    public String GetContextFile(String fileName, Type testClass)
	    {
            throw new NotImplementedException("Not yet implemented");
            //File file = null;
            //File tempFile = new File(fileName);
            //if (tempFile.canRead())
            //{
            //    file = tempFile;
            //}
            //if (file == null)
            //{
            //    String callingNamespace = ((Class<?>) testClass).getPackage().getName();
            //    String relativePath = fileName.startsWith("/") ? "." + fileName : callingNamespace.replace(".", File.separator) + File.separator + fileName;
            //    String[] classPaths = pathSeparator.split(System.getProperty("java.class.path"));
            //    for (int i = 0; i < classPaths.length; i++)
            //    {
            //        tempFile = new File(classPaths[i], relativePath);
            //        if (tempFile.canRead())
            //        {
            //            file = tempFile;
            //            break;
            //        }
            //    }
            //    if (file == null)
            //    {
            //        Pattern fileSuffixPattern = Pattern.compile(".+\\.(?:[^\\.]*)");
            //        Matcher matcher = fileSuffixPattern.matcher(relativePath);
            //        if (!matcher.matches())
            //        {
            //            relativePath += ".sql";
            //            for (int i = 0; i < classPaths.length; i++)
            //            {
            //                tempFile = new File(classPaths[i], relativePath);
            //                if (tempFile.canRead())
            //                {
            //                    file = tempFile;
            //                    break;
            //                }
            //            }
            //        }
            //    }
            //    if (file == null && !fileName.startsWith("/"))
            //    {
            //        // Path is not with root-slash specified. Try to add this before giving up:
            //        return getContextFile("/" + fileName, testClass);
            //    }
            //    if (file == null)
            //    {
            //        ILogger log = LoggerFactory.getLogger(testClass);
            //        if (log.isWarnEnabled())
            //        {
            //            String error = "Cannot find '" + relativePath + "' in class path:" + nl;
            //            Arrays.sort(classPaths);
            //            for (int i = 0; i < classPaths.length; i++)
            //            {
            //                error += "\t" + classPaths[i] + nl;
            //            }
            //            log.warn(error);
            //        }
            //        return null;
            //    }
            //}

            //ILogger log = LoggerFactory.getLogger(testClass);

            //if (log.isDebugEnabled())
            //{
            //    log.debug("Resolved test context file: " + file.getAbsolutePath());
            //}
            //return file;
	    }
    }
}