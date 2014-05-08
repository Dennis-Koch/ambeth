using System;
using System.Collections.Generic;
using System.IO;
using System.Reflection;
using System.Text.RegularExpressions;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Log;

namespace De.Osthus.Ambeth.Util
{
    public class ClasspathScanner : IInitializingBean, IClasspathScanner
    {
        [LogInstance]
		public ILogger Log { private get; set; }

        [Property(XmlConfigurationConstants.PackageScanPatterns, DefaultValue = @"De\.Osthus\.Ambeth(?:\.[^\.]+)*(?:\.Transfer|\.Model)\..+")]
        public String PackageFilterPatterns { get; set; }

        protected Regex[] packageScanPatterns;

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(PackageFilterPatterns, "PackageFilterPatterns");
        }

        // Has to be used by getter since it might be needed before afterPropertiesSet() was called.
        protected Regex[] GetPackageScanPatterns()
        {
            if (packageScanPatterns == null)
            {
                ParamChecker.AssertNotNull(PackageFilterPatterns, "PackageFilterPatterns");

                String[] split = PackageFilterPatterns.Split(';');
                packageScanPatterns = new Regex[split.Length];
                for (int a = split.Length; a-- > 0; )
                {
                    String packagePattern = split[a];
                    packageScanPatterns[a] = new Regex(packagePattern);
                }
            }
            return packageScanPatterns;
        }

        public IList<Type> ScanClassesAnnotatedWith(params Type[] annotationTypes)
        {
            IList<Type> targetClassNames = ScanForClasses();
            IList<Type> classNamesFound = new List<Type>();
            for (int a = targetClassNames.Count; a-- > 0; )
            {
                Type className = targetClassNames[a];
                for (int b = annotationTypes.Length; b-- > 0; )
                {
                    Object[] annotations = className.GetCustomAttributes(annotationTypes[b], false);
                    if (annotations == null || annotations.Length == 0)
                    {
                        continue;
                    }
                    classNamesFound.Add(className);
                }
            }
            return classNamesFound;
        }

        public IList<Type> ScanClassesImplementing(params Type[] superTypes)
        {
            IList<Type> targetClassNames = ScanForClasses();
            IList<Type> classNamesFound = new List<Type>();
            for (int a = targetClassNames.Count; a-- > 0; )
            {
                Type className = targetClassNames[a];
                for (int b = superTypes.Length; b-- > 0; )
                {
                    if (!superTypes[b].IsAssignableFrom(className))
                    {
                        continue;
                    }
                    classNamesFound.Add(className);
                }
            }
            return classNamesFound;
        }

        protected virtual IList<Type> ScanForClasses()
        {
            ISet<Assembly> assemblies = new HashSet<Assembly>();
#if !SILVERLIGHT
            String path = System.AppDomain.CurrentDomain.BaseDirectory;

            foreach (string dll in Directory.GetFiles(path, "*.dll", SearchOption.AllDirectories))
            {
                try
                {
                    if (dll.Contains("Telerik"))
                    {
                        continue;
                    }
                    Assembly loadedAssembly = Assembly.LoadFile(dll);
                    assemblies.Add(loadedAssembly);
                }
                catch (FileLoadException)
                { } // The Assembly has already been loaded.
                catch (BadImageFormatException)
                { } // If a BadImageFormatException exception is thrown, the file is not an assembly.

            } // foreach dll
#else
            ISet<Assembly> registeredAssemblies = AssemblyHelper.Assemblies;
            foreach (Assembly registeredAssembly in registeredAssemblies)
            {
                assemblies.Add(registeredAssembly);
            }
#endif

            List<Type> filteredTypes = new List<Type>();

            foreach (Assembly assembly in assemblies)
            {
                try
                {
                    Type[] types = assembly.GetTypes();

                    for (int a = 0, size = types.Length; a < size; a++)
                    {
                        Type type = types[a];
                        Regex[] packageScanPatterns = GetPackageScanPatterns();
                        for (int b = packageScanPatterns.Length; b-- > 0; )
                        {
                            Match pathMatcher = packageScanPatterns[b].Match(type.FullName);
                            if (pathMatcher.Success)
                            {
                                filteredTypes.Add(type);
                            }
                        }
                    }
                }
                catch (ReflectionTypeLoadException e)
                {
                    Exception[] les = e.LoaderExceptions;
                    throw;
                }
            }
            return filteredTypes;
        }

        protected virtual String BuildPatternFailMessage(Regex pattern, String value)
        {
            return "Matcher should have matched: Pattern: '" + pattern.ToString() + "'. Value '" + value + "'";
        }
    }
}