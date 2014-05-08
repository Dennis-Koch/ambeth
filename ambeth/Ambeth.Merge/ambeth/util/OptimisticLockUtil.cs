using De.Osthus.Ambeth.Exceptions;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Merge.Transfer;
using System;

namespace De.Osthus.Ambeth.Util
{
    public class OptimisticLockUtil
    {
        public static OptimisticLockException ThrowDeleted(IObjRef objRef)
        {
            throw new OptimisticLockException("Object outdated: " + objRef + " has been deleted concurrently", null, objRef);
        }

        public static OptimisticLockException ThrowDeleted(IObjRef objRef, Object obj)
        {
            throw new OptimisticLockException("Object outdated: " + objRef + " has been deleted concurrently", null, obj);
        }

        public static OptimisticLockException ThrowModified(IObjRef objRef, Object givenVersion)
        {
            return ThrowModified(objRef, givenVersion, null);
        }

        public static OptimisticLockException ThrowModified(IObjRef objRef, Object givenVersion, Object obj)
        {
            String givenVersionString = "";
            if (givenVersion != null)
            {
                givenVersionString = " - given version: " + givenVersion;
            }
            if (obj != null)
            {
                throw new OptimisticLockException("Object outdated: " + objRef + " has been modified concurrently" + givenVersionString, null, obj);
            }
            throw new OptimisticLockException("Object outdated: " + objRef + " has been modified concurrently" + givenVersionString, null, new ObjRef(
                    objRef.RealType, objRef.IdNameIndex, objRef.Id, givenVersion));
        }

        private OptimisticLockUtil()
        {
            // Intended blank
        }
    }
}