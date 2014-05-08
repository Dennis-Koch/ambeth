using System;
using De.Osthus.Ambeth.Merge.Model;

namespace De.Osthus.Ambeth.Exceptions
{
    public class OptimisticLockException : Exception
    {
        public Object CurrentVersion { get; private set; }

        public Object VersionToMerge { get; private set; }

        public Object EntityInstance { get; private set; }

        public OptimisticLockException(Object currentVersion, Object versionToMerge, Object entityInstance)
            : base("Object outdated: Current version '" + currentVersion + "' vs. given version '" + versionToMerge
                        + "' on entity " + entityInstance)
        {
            this.CurrentVersion = currentVersion;
            this.VersionToMerge = versionToMerge;
            this.EntityInstance = entityInstance;
        }

        public OptimisticLockException(IObjRef ori)
            : base("Object outdated on entity " + ori)
        {
            this.VersionToMerge = ori.Version;
            this.EntityInstance = ori;
        }

        public OptimisticLockException(Object entity)
            : base("Object outdated on entity " + entity)
        {
            this.EntityInstance = entity;
        }
    }
}
