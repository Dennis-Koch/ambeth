using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using De.Osthus.Ambeth.Cache.Model;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Merge.Model;

namespace De.Osthus.Ambeth.Security
{
    abstract public class AbstractEntityFilter<E> : IInitializingBean, IEntityFilter where E : class
    {
        public virtual void AfterPropertiesSet()
        {
            // Intended blank
        }

        public virtual ReadPermission CheckReadPermissionOnEntity(Object entity, IUserHandle userHandle)
        {
            if (userHandle == null)
            {
                return ReadPermission.FORBIDDEN;
            }
            if (entity is E)
            {
                return CheckReadPermissionOnEntity((E)entity, userHandle);
            }
            else if (entity is IObjRef)
            {
                IObjRef ori = (IObjRef)entity;
                Type realType = ori.RealType;
                if (typeof(E).IsAssignableFrom(realType))
                {
                    return CheckReadPermissionOnEntity(realType, ori, userHandle);
                }
            }
            else if (entity is ILoadContainer)
            {
                ILoadContainer loadContainer = (ILoadContainer)entity;
                Type realType = loadContainer.Reference.RealType;
                if (typeof(E).IsAssignableFrom(realType))
                {
                    return CheckReadPermissionOnEntity(realType, loadContainer, userHandle);
                }
            }
            return ReadPermission.UNDEFINED;
        }

        protected virtual ReadPermission CheckReadPermissionOnEntity(Type type, IObjRef ori, IUserHandle userHandle)
        {
            return ReadPermission.UNDEFINED;
        }

        protected virtual ReadPermission CheckReadPermissionOnEntity(Type type, ILoadContainer entity, IUserHandle userHandle)
        {
            throw new NotImplementedException();
        }

        public virtual ReadPermission CheckReadPermissionOnEntity(E entity, IUserHandle userHandle)
        {
            throw new NotImplementedException();
        }
    }
}
