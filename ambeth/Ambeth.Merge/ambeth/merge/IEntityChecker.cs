using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using De.Osthus.Ambeth.Merge.Model;

namespace De.Osthus.Ambeth.Typeinfo
{
    public enum EntityTypeResult
    {
        UNDEFINED,
        ENTITY_TRUE,
        ENTITY_FALSE
    }

    public interface IEntityChecker
    {
        EntityTypeResult CheckForEntityType(Type entityType);
    }
}
