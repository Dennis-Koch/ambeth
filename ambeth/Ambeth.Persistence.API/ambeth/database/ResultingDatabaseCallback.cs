using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Persistence;
using System;

namespace De.Osthus.Ambeth.Database
{
    public delegate R ResultingDatabaseCallback<R>(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap);
}