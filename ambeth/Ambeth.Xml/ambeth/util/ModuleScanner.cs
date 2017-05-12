using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Annotation;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;

namespace De.Osthus.Ambeth.Util
{
    public class ModuleScanner : IInitializingBean, IModuleProvider
    {
        [LogInstance]
	    public ILogger log;
        
	    public IClasspathScanner ClasspathScanner { protected get; set; }

	    public void AfterPropertiesSet()
	    {
		    ParamChecker.AssertNotNull(ClasspathScanner, "ClasspathScanner");
	    }

	    protected Type[] GetModules(bool scanForFrameworkModule)
	    {
		    if (log.InfoEnabled)
		    {
			    log.Info("Looking for " + (scanForFrameworkModule ? "Ambeth" : "Application") + " bootstrap modules in classpath...");
		    }
		    IList<Type> bootstrapOrFrameworkModules = ClasspathScanner.ScanClassesAnnotatedWith(scanForFrameworkModule ? typeof(FrameworkModuleAttribute)
				    : typeof(BootstrapModuleAttribute));

		    List<Type> bootstrapModules = new List<Type>(bootstrapOrFrameworkModules.Count);

		    foreach (Type bootstrapOrFrameworkModule in bootstrapOrFrameworkModules)
		    {
			    if (scanForFrameworkModule && AnnotationUtil.IsAnnotationPresent<FrameworkModuleAttribute>(bootstrapOrFrameworkModule, false))
			    {
				    bootstrapModules.Add(bootstrapOrFrameworkModule);
			    }
			    else if (AnnotationUtil.IsAnnotationPresent<BootstrapModuleAttribute>(bootstrapOrFrameworkModule, false)
					    && !AnnotationUtil.IsAnnotationPresent<FrameworkModuleAttribute>(bootstrapOrFrameworkModule, false))
			    {
				    bootstrapModules.Add(bootstrapOrFrameworkModule);
			    }
		    }
		    if (log.InfoEnabled)
		    {
			    log.Info("Found " + bootstrapModules.Count + (scanForFrameworkModule ? " Ambeth" : " Application")
					    + " modules in classpath to include in bootstrap...");
			    bootstrapModules.Sort(delegate(Type o1, Type o2)
				    {
					    return o1.FullName.CompareTo(o2.FullName);
				    });
			    for (int a = 0, size = bootstrapModules.Count; a < size; a++)
			    {
				    Type boostrapModule = bootstrapModules[a];
				    log.Info("Including " + boostrapModule.FullName);
			    }
		    }
		    return bootstrapModules.ToArray();
	    }

	    public Type[] GetFrameworkModules()
	    {
		    return GetModules(true);
	    }

	    public Type[] GetBootstrapModules()
	    {
		    return GetModules(false);
	    }
    }
}
