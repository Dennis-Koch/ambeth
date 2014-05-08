using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Util;
using System.Reflection;
using De.Osthus.Ambeth.Config;
#if SILVERLIGHT
using System.Windows.Resources;
using System.Windows;
#endif

namespace De.Osthus.Ambeth.Io
{
    public class FileUtil
    {
        private const char CONFIG_SEPARATOR = ';';

        private const char PATH_SEPARATOR = '\\';

        protected FileUtil()
        {
            // Intended blank
        }

        public static String[] SplitConfigFileNames(String fileNames)
        {
            String[] splittedfileNames = fileNames.Split(CONFIG_SEPARATOR);
            return splittedfileNames;
        }

        public static Stream[] OpenFileStreams(String fileNames)
        {
            Stream[] fileStreams = OpenFileStreams(fileNames, (ILogger)null);
            return fileStreams;
        }

        public static Stream[] OpenFileStreams(String fileNames, ILogger log)
        {
            String[] splittedfileNames = SplitConfigFileNames(fileNames);
            Stream[] fileStreams = OpenFileStreams(splittedfileNames, false, log);
            return fileStreams;
        }

        public static Stream[] OpenFileStreams(params String[] fileNames)
        {
            Stream[] streams = OpenFileStreams(fileNames, false, null);
            return streams;
        }

        public static Stream[] OpenFileStreams(String[] fileNames, ILogger log)
        {
            return OpenFileStreams(fileNames, false, log);
        }

        public static Stream[] OpenFileStreams(String[] fileNames, bool ignoreEmptyNames, ILogger log)
        {
            Stream[] streams = new Stream[fileNames.Length];
            String combinesFileNames = null;

            for (int i = 0, size = fileNames.Length; i < size; i++)
            {
                String fileName = fileNames[i];
                Stream file = OpenFileStream(fileName, log);
                if (file != null)
                {
                    streams[i] = file;
                    continue;
                }
                // In .NET a file may be reference 'full qualified' with its assembly suffix
                // If it is, a semicolon has been specified with lead to a seperate string token here
                String nextFileName = fileNames.Length > i + 1 ? fileNames[i + 1] : null;
                if (nextFileName != null)
                {
                    // A token exists which may be the 'real' file path while our current token is the assembly name
                    String fqFileName = fileName + ";" + nextFileName;
                    file = OpenFileStream(fqFileName);
                    if (file != null)
                    {
                        streams[i] = file;
                        // concatenation has been successful. we have to 'consume' the following token explicitly
                        i++;
                        continue;
                    }
                }

                if (combinesFileNames == null)
                {
                    combinesFileNames = Combine(fileNames);
                }
                throw new ArgumentException(String.Format("File source '{0}' not found in filesystem and classpath. Filenames: '{1}'", fileName,
                        combinesFileNames));
            }

            return streams;
        }

        public static Stream OpenFileStream(String fileName)
        {
            Stream stream = OpenFileStream(fileName, null);
            return stream;
        }

        public static Stream OpenFileStream(String fileName, ILogger log)
        {
            Stream fileStream = null;
#if !SILVERLIGHT
            String namespacePath = fileName.Replace('\\', '.').Replace('/', '.');
            foreach (Assembly assembly in AssemblyHelper.Assemblies)
            {
                foreach (String resourceName in assembly.GetManifestResourceNames())
                {
                    if (resourceName.EndsWith(namespacePath))
                    {
                        fileStream = assembly.GetManifestResourceStream(resourceName);
                        if (fileStream != null)
                        {
                            return fileStream;
                        }
                    }
                }
            }

            fileStream = typeof(FileUtil).Assembly.GetFile(fileName);
            if (fileStream != null && fileStream.CanRead)
            {
                return fileStream;
            }
            fileStream = File.OpenRead(fileName);
            if (fileStream == null || !fileStream.CanRead)
            {
                throw new ArgumentException("File '" + fileName + "' not readable");
            }
#else

            String urlString = fileName;//"/Minerva.Client;component/" + fileName;
            if (!urlString.StartsWith("/"))
            {
                // Force leading slash
                urlString = "/" + urlString;
            }
            StreamResourceInfo streamResourceInfo = Application.GetResourceStream(new Uri(urlString, UriKind.Relative));
            if (streamResourceInfo != null)
            {
                fileStream = streamResourceInfo.Stream;
            }
#endif

            return fileStream;
        }

        protected static String Combine(String[] strings)
        {
            if (strings == null || strings.Length == 0)
            {
                return "";
            }
            else if (strings.Length == 1)
            {
                return strings[0];
            }
            else
            {
                StringBuilder sb = new StringBuilder(strings[0]);
                for (int i = 1; i < strings.Length; i++)
                {
                    sb.Append(", ").Append(strings[i]);
                }
                return sb.ToString();
            }
        }
    }
}
