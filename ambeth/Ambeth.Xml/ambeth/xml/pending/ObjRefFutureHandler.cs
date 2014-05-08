using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Util;
using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Xml.Pending
{
    public class ObjRefFutureHandler : IObjectFutureHandler, IInitializingBean
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        public virtual ICache Cache { protected get; set; }

        public virtual IEntityFactory EntityFactory { protected get; set; }

        public void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(Cache, "Cache");
            ParamChecker.AssertNotNull(EntityFactory, "EntityFactory");
        }

        public void Handle(IList<IObjectFuture> objectFutures)
        {
            IEntityFactory entityFactory = EntityFactory;
            IList<IObjRef> oris = new List<IObjRef>(objectFutures.Count);
            // ObjectFutures have to be handled in order
            for (int i = 0, size = objectFutures.Count; i < size; i++)
            {
                IObjectFuture objectFuture = objectFutures[i];
                if (!(objectFuture is ObjRefFuture))
                {
                    throw new ArgumentException("'" + GetType().Name + "' cannot handle " + typeof(IObjectFuture).Name
                            + " implementations of type '" + objectFuture.GetType().Name + "'");
                }
                if (objectFuture.Value != null)
                {
                    continue;
                }

                ObjRefFuture objRefFuture = (ObjRefFuture)objectFuture;
                IObjRef ori = objRefFuture.Ori;
                if (ori.Id != null && !Object.Equals(ori.Id, 0))
                {
                    oris.Add(ori);
                }
                else if (ori is IDirectObjRef && ((IDirectObjRef)ori).Direct != null)
                {
                    Object entity = ((IDirectObjRef)ori).Direct;
                    objRefFuture.Value = entity;
                    oris.Add(null);
                }
                else
                {
                    Object newEntity = entityFactory.CreateEntity(ori.RealType);
                    objRefFuture.Value = newEntity;
                    oris.Add(null);
                }
            }

            IList<Object> objects = Cache.GetObjects(oris, CacheDirective.ReturnMisses);

            for (int i = 0, size = objectFutures.Count; i < size; i++)
            {
                if (oris[i] == null)
                {
                    continue;
                }

                ObjRefFuture objRefFuture = (ObjRefFuture)objectFutures[i];
                Object obj = objects[i];
                objRefFuture.Value = obj;
            }
        }
    }
}
