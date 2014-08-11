using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Persistence;
using System;

namespace De.Osthus.Ambeth.Database
{
    public delegate void DatabaseCallback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap);
}
