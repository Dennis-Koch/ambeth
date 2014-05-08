using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Annotation;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Model;
using System.Runtime.Serialization;

namespace De.Osthus.Ambeth.Privilege
{
    public interface IPrivilegeItem
    {
        bool IsCreateAllowed();

        bool IsUpdateAllowed();

        bool IsDeleteAllowed();

        bool IsReadAllowed();
    }
}
