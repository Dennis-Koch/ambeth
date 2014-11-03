using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Reflection;

namespace CsharpClassbrowser
{
    class Program
    {

        // ============================================================================================
        #region Variables
        // ============================================================================================

        private static string libraryPathString;

        #endregion

        // ============================================================================================
        #region Constants
        // ============================================================================================

        public const string ARG_KEY_HELP = "-help";
        public const string ARG_KEY_TARGETPATH = "-targetPath";
        public const string ARG_KEY_ASSEMBLYPATHS = "-assemblyPaths";
        public const string ARG_KEY_LIBRARYASSEMBLYPATHS = "-libraryAssemblyPaths";
        public const string ARG_KEY_MODULEROOTPATH = "-moduleRootPath";
        public const string ARG_KEY_MODULES = "-modulesToBeAnalyzed";
        public const char ARG_PATH_DELIMITER = ',';

        #endregion

        // ============================================================================================
        #region Methods
        // ============================================================================================

        /// <summary>
        /// The entry point of the console application.
        /// </summary>
        /// <param name="args">Program arguments</param>
        /// <returns>Exit code</returns>
        static int Main(string[] args)
        {
            try
            {
                Run(args);
                return (int)ExitCode.Success;
            }
            catch (Exception e)
            {
                Console.WriteLine(e.Message);
            }
            return (int)ExitCode.Error;
        }

        /// <summary>
        /// The program logic.
        /// </summary>
        /// <param name="args">Program arguments</param>
        private static void Run(string[] args)
        {
            if (WantsHelp(args))
            {
                DisplayHelpAndWait();
                return;
            }

            IList<Assembly> assemblies = GetAssemblies(args);
            if (assemblies == null || assemblies.Count == 0)
            {
                Console.WriteLine("No ASSEMBLIES found!");
                return;
            }
            else
            {
                Console.WriteLine("All " + assemblies.Count + " ASSEMBLIES successfully read.");
            }
            string targetPath = GetTargetPathEnsured(args);

            // Read the source files of all modules and create a map to identify the modules
            string moduleRootPath = GetModuleRootPathEnsured(args);
            IDictionary<string, string> moduleMap = null;
            string skipScan = GetArgIfExists("-skipModuleScan", args);
            if (string.IsNullOrWhiteSpace(skipScan) || "false".Equals(skipScan, StringComparison.InvariantCultureIgnoreCase))
            {
                moduleMap = CreateModuleMap(moduleRootPath);
            }

            IList<TypeDescription> foundTypes = ParserUtil.AnalyzeAssemblies(assemblies, moduleMap);

            OutputUtil.Export(foundTypes, targetPath);
            Console.WriteLine("FINISHED!");
        }

        /// <summary>
        /// Check if help should be displayed.
        /// </summary>
        /// <param name="args">Program arguments; optional</param>
        /// <returns>True if the help should be displayed</returns>
        private static bool WantsHelp(string[] args)
        {
            if (args == null || args.Length == 0 || (args.Length == 1 && ARG_KEY_HELP == args[0]))
            {
                return true;
            }
            return false;
        }

        /// <summary>
        /// Display the help text.
        /// </summary>
        private static void DisplayHelpAndWait()
        {
            Console.WriteLine("========================================");
            Console.WriteLine("    Welcome to the C# class browser.");
            Console.WriteLine("========================================" + System.Environment.NewLine);
            Console.WriteLine("The following arguments are supported: " + System.Environment.NewLine);
            Console.WriteLine(ARG_KEY_HELP + " displays this help screen" + System.Environment.NewLine);
            Console.WriteLine(ARG_KEY_TARGETPATH + "={path} sets the path where the export file should be written to" + System.Environment.NewLine);
            Console.WriteLine(ARG_KEY_MODULEROOTPATH + "={modulerootpath} sets the root path to the source files needed to identify the modules; "
                        + "modules have to be direct children of this path" + System.Environment.NewLine);
            Console.WriteLine(ARG_KEY_ASSEMBLYPATHS + "={assemblypaths} defines the paths to the assemblies " +
                "to be analyzed; the path separator is the '" + ARG_PATH_DELIMITER + "'" + System.Environment.NewLine + System.Environment.NewLine);
            Console.WriteLine(ARG_KEY_MODULES + "={assemblylist} defines the a list of assemblies to be loaded from the given assembly " +
                "paths (optional; if omitted all assmeblies from the paths are loaded); the path separator is the '" + ARG_PATH_DELIMITER +
                "'; no whitespaces allowed" + System.Environment.NewLine + System.Environment.NewLine);
            Console.WriteLine(ARG_KEY_LIBRARYASSEMBLYPATHS + "={assemblypaths} paths to needed libraries" + System.Environment.NewLine + System.Environment.NewLine);
            Console.WriteLine("Example (which exports the file '" + OutputUtil.EXPORT_FILE_NAME + "' to the given target path): " + System.Environment.NewLine);
            Console.WriteLine("CsharpClassbrowser.exe " + ARG_KEY_ASSEMBLYPATHS + @"=c:\temp\ClassBrowser\MyAssemblies" + ARG_PATH_DELIMITER +
                @"c:\temp\OtherAssemblies " + ARG_KEY_TARGETPATH + @"=c:\temp\export" + System.Environment.NewLine);
            Console.WriteLine("========================================" + System.Environment.NewLine);
        }

        ///// <summary>
        ///// Wait until the user presses a key
        ///// </summary>
        //private static void WaitForKeyPress()
        //{
        //    Console.WriteLine("Press key to continue...");
        //    try
        //    {
        //        Console.ReadKey();
        //    }
        //    catch (NullReferenceException)
        //    {
        //        // if run from ANT this may result in a NullPointer exception -> ignore
        //    }
        //}

        /// <summary>
        /// Get target path where the export should go. Throws an ArgumentException if the path can't be found or doesn't exist. 
        /// </summary>
        /// <param name="args">Program arguments to get the needed info from</param>
        /// <returns>Target path</returns>
        private static string GetTargetPathEnsured(string[] args)
        {
            return GetPathEnsured(args, ARG_KEY_TARGETPATH, "Target path");
        }

        /// <summary>
        /// Get module root path. Throws an ArgumentException if the path can't be found or doesn't exist. 
        /// </summary>
        /// <param name="args">Program arguments to get the needed info from</param>
        /// <returns>Module root path</returns>
        private static string GetModuleRootPathEnsured(string[] args)
        {
            return GetPathEnsured(args, ARG_KEY_MODULEROOTPATH, "Module root path");
        }

        /// <summary>
        /// Get path from a key and ensure that it is a directory. Throws an ArgumentException if the path can't be found or doesn't exist. 
        /// </summary>
        /// <param name="args">Program arguments to get the needed info from</param>
        /// <param name="propertyKey"></param>
        /// <param name="identifier">Identifier used in all message texts</param>
        /// <returns>Path</returns>
        private static string GetPathEnsured(string[] args, string propertyKey, string identifier)
        {
            string path = GetArg(propertyKey, args);
            // Check the path
            if (string.IsNullOrWhiteSpace(path))
            {
                throw new ArgumentException(identifier + " not found!");
            }
            if (!System.IO.Directory.Exists(path))
            {
                throw new ArgumentException(identifier + " '" + path + "' is not a directory!");
            }
            return path;
        }

        /// <summary>
        /// Get the value for the given key from the given arguments. Throws an exception if no value for the key can be found!
        /// </summary>
        /// <param name="key">Key</param>
        /// <param name="args">List of arguments to look at; mandatory</param>
        /// <returns>Value for the key</returns>
        private static string GetArg(string key, string[] args)
        {
            var result = GetArgIfExists(key, args);
            if (result == null)
            {
                throw new ArgumentNullException("Program argument for key '" + key + "' not found!");
            }
            return result;
        }

        /// <summary>
        /// Get the value for the given key from the given arguments, otherwise null.
        /// </summary>
        /// <param name="key">Key</param>
        /// <param name="args">List of arguments to look at; mandatory</param>
        /// <returns>Value for the key or null</returns>
        private static string GetArgIfExists(string key, string[] args)
        {
            if (args == null)
            {
                throw new ArgumentNullException("Program arguments missing!");
            }
            foreach (var arg in args)
            {
                if (arg.StartsWith(key))
                {
                    string possibleValue = arg.Substring(key.Length).Trim();
                    if (possibleValue.StartsWith("="))
                    {
                        var value = possibleValue.Substring(1).Trim();
                        if (value.EndsWith(ARG_PATH_DELIMITER.ToString()))
                        {
                            throw new ArgumentException("Please check the argument '" + key + "'! It seems to be incomplete...");
                        }
                        return value;
                    }
                }
            }
            return null;
        }

        /// <summary>
        /// Get the assemblies to analyze.
        /// </summary>
        /// <param name="args">Program arguments to get the needed info from</param>
        /// <returns>List of assemblies; never null (but may be empty)</returns>
        private static IList<Assembly> GetAssemblies(string[] args)
        {
            IList<Assembly> assemblies = new List<Assembly>();

            string pathString = GetArg(ARG_KEY_ASSEMBLYPATHS, args);
            var paths = pathString.Split(ARG_PATH_DELIMITER);

            libraryPathString = GetArg(ARG_KEY_LIBRARYASSEMBLYPATHS, args);
            AppDomain currentDomain = AppDomain.CurrentDomain;
            currentDomain.AssemblyResolve += new ResolveEventHandler(MyResolveEventHandler);

            foreach (var path in paths)
            {
                var files = Directory.EnumerateFiles(path, "*.*", SearchOption.TopDirectoryOnly)
                    .Where(s => s.EndsWith(".exe", StringComparison.OrdinalIgnoreCase) || s.EndsWith(".dll", StringComparison.OrdinalIgnoreCase));
                foreach (var fileName in files)
                {
                    if (useAssembly(fileName, args))
                    {
                        Assembly assembly = Assembly.LoadFrom(fileName);
                        if (!assemblies.Contains(assembly))
                        {
                            assemblies.Add(assembly);
                        }
                    }
                    // TODO: maybe log that an assembly was skipped!? or throw an exception?
                }
            }

            return assemblies; ;
        }

        /// <summary>
        /// Check if the given assembly should be used (contains types to inspect).
        /// </summary>
        /// <param name="fullFileName">Assembly file name including the path</param>
        /// <param name="args">Program arguments to get the needed info from</param>
        /// <returns>True if used</returns>
        private static bool useAssembly(string fullFileName, string[] args)
        {
            if (string.IsNullOrWhiteSpace(fullFileName))
            {
                throw new ArgumentNullException("File name not set!");
            }
            string assemblyList = GetArgIfExists(ARG_KEY_MODULES, args);
            if (assemblyList == null)
            {
                return true;
            }
            string[] assembliesToUse = assemblyList.Split(ARG_PATH_DELIMITER);
            string fileName = System.IO.Path.GetFileNameWithoutExtension(fullFileName); // maximum string.Empty because path is set
            return assembliesToUse.Contains(fileName, StringComparer.InvariantCultureIgnoreCase);
        }

        /// <summary>
        /// Check if DLL file exists in the GAC folders.
        /// </summary>
        /// <param name="dllName">Name of the DLL</param>
        /// <returns>Full file name if the DLL exists</returns>
        private static string GetFromGlobalAssembly(string dllName)
        {
            IList<string> gacFolders = new List<string>() { @"c:\windows\assembly\GAC", @"c:\windows\assembly\GAC_32", @"c:\windows\assembly\GAC_64", 
                @"c:\windows\assembly\GAC_MSIL", @"c:\windows\assembly\NativeImages_v2.0.50727_32", @"c:\windows\assembly\NativeImages_v2.0.50727_64" };
            return GetFromFolders(dllName, gacFolders, false);
        }

        /// <summary>
        /// Check if DLL file exists in the given folders.
        /// </summary>
        /// <param name="dllName">Name of the DLL</param>
        /// <param name="folders">Folders to check</param>
        /// <param name="useRoot">Flag if also the root folder has to be checked</param>
        /// <returns>Full file name if the DLL exists</returns>
        private static string GetFromFolders(string dllName, IList<string> folders, bool useRoot)
        {
            foreach (string folder in folders)
            {
                if (Directory.Exists(folder))
                {
                    if (useRoot)
                    {
                        string fullAssemblyFileName = Path.Combine(folder, dllName);
                        if (File.Exists(fullAssemblyFileName))
                        {
                            return fullAssemblyFileName;
                        }
                    }
                    string[] assemblyFolders = Directory.GetDirectories(folder);
                    foreach (string assemblyFolder in assemblyFolders)
                    {
                        string fullAssemblyFileName = Path.Combine(assemblyFolder, dllName);
                        if (File.Exists(fullAssemblyFileName))
                        {
                            return fullAssemblyFileName;
                        }
                    }
                }
            }
            return null;
        }

        /// <summary>
        /// Create the map with the module name of each class file.
        /// </summary>
        /// <param name="rootPath">The root path - all modules have to be direct children of this path</param>
        /// <returns>Dictionary with the module name of each class file; key is the full qualified class name in LOWER CASE and value the module name</returns>
        private static IDictionary<string, string> CreateModuleMap(string rootPath)
        {
            IDictionary<string, string> moduleMap = new Dictionary<string, string>();
            // Assumption: the modules are the first child hierarchy
            if (!Directory.Exists(rootPath))
            {
                throw new ArgumentException("Root path '" + rootPath + "' is not a directory!");
            }
            // Not needed at the moment because at the moment the modules are the assemblies. In the future this may change
            // and will be handled like in JAVA. But attention: the name in the file system doen't match the namespace and
            // type name e.g. Ambeth.Cache.Bytecode.DLL contains type De.Osthus.Ambeth.Bytecode.Behavior.CacheMapEntryVisitor
            // which can be found in path Ambeth.Cache.Bytecode\ambeth\bytecode\visitor\CacheMapEntryVisitor.cs
            string[] foundInRoot = Directory.GetDirectories(rootPath);
            foreach (string moduleDir in foundInRoot)
            {
                var separators = new char[] { Path.DirectorySeparatorChar, Path.AltDirectorySeparatorChar };
                string[] splittedModuleDir = moduleDir.Split(separators, StringSplitOptions.RemoveEmptyEntries);
                string moduleName = splittedModuleDir.Last();
                var files = Directory.GetFiles(moduleDir, "*.cs", SearchOption.AllDirectories);
                foreach (string file in files)
                {
                    string relativeName = file.Replace(moduleDir, string.Empty);
                    string[] splittedRelativeName = relativeName.Split(separators, StringSplitOptions.RemoveEmptyEntries);
                    string className = string.Join(".", splittedRelativeName).ToLower();
                    // FIXME Code is broken. Key here is the file name, lookup is done with the class name (namespace + Type).
                    moduleMap[className] = moduleName;
                }
            }
            return moduleMap;
        }

        #endregion

        // ============================================================================================
        #region Eventhandler
        // ============================================================================================

        public static Assembly MyResolveEventHandler(Object sender, ResolveEventArgs args)
        {
            string dllName = args.Name.Split(',')[0] + ".dll";
            Assembly result = null;
            string message = "Resolving '" + dllName + "'. Found ";
            // Lookup in library paths
            if (libraryPathString != null)
            {
                var libPaths = libraryPathString.Split(ARG_PATH_DELIMITER);
                foreach (var libPath in libPaths)
                {
                    string fullLibsFileName = Path.Combine(libraryPathString, dllName);
                    if (File.Exists(fullLibsFileName))
                    {
                        result = Assembly.LoadFile(fullLibsFileName);
                        message += "in library path."; // fullLibsFileName;
                    }
                }
            }
            // Check GAC
            if (result == null)
            {
                string fullGacFileName = GetFromGlobalAssembly(dllName);
                if (fullGacFileName != null)
                {
                    result = Assembly.LoadFile(fullGacFileName);
                    message += "in GAC."; // fullGacFileName;
                }
            }
            // Check Silverlight folders
            if (result == null)
            {
                IList<string> slFolders = new List<string>() { @"c:\Program Files (x86)\Reference Assemblies\Microsoft\Framework\Silverlight\v5.0\", 
                        @"c:\Program Files (x86)\Microsoft SDKs\Silverlight\v5.0\Libraries\Client\" };
                string fullSLFileName = GetFromFolders(dllName, slFolders, true);
                if (fullSLFileName != null)
                {
                    result = Assembly.LoadFile(fullSLFileName);
                    message += "in Silverlight paths."; // fullSLFileName;
                }
            }
            if (result == null)
            {
                message += "neither in library path, GAC nor in Silverlight paths!";
            }
            Console.WriteLine(message);
            return result;
        }

        #endregion

    }
}
