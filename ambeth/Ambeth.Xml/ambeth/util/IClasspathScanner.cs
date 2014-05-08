using System.Collections.Generic;
using System;

namespace De.Osthus.Ambeth.Util
{
    public interface IClasspathScanner
    {
        IList<Type> ScanClassesAnnotatedWith(params Type[] annotationTypes);

        IList<Type> ScanClassesImplementing(params Type[] superTypes);
    }
}