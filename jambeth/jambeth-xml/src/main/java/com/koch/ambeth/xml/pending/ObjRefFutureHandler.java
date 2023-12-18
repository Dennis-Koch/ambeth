package com.koch.ambeth.xml.pending;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.IEntityFactory;
import com.koch.ambeth.merge.cache.CacheDirective;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.model.IDirectObjRef;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.collections.ArrayList;

import java.util.List;

public class ObjRefFutureHandler implements IObjectFutureHandler {
    @Autowired
    protected ICache cache;

    @Autowired
    protected IEntityFactory entityFactory;

    @Override
    public void handle(List<IObjectFuture> objectFutures) {
        var entityFactory = this.entityFactory;
        var objRefs = new ArrayList<IObjRef>(objectFutures.size());
        // ObjectFutures have to be handled in order
        for (int i = 0, size = objectFutures.size(); i < size; i++) {
            var objectFuture = objectFutures.get(i);
            if (!(objectFuture instanceof ObjRefFuture)) {
                throw new IllegalArgumentException(
                        "'" + getClass().getName() + "' cannot handle " + IObjectFuture.class.getSimpleName() + " implementations of type '" + objectFuture.getClass().getName() + "'");
            }

            var objRefFuture = (ObjRefFuture) objectFuture;
            var ori = objRefFuture.getOri();
            if (ori.getId() != null) {
                objRefs.add(ori);
            } else if (ori instanceof IDirectObjRef && ((IDirectObjRef) ori).getDirect() != null) {
                var entity = ((IDirectObjRef) ori).getDirect();
                objRefFuture.setValue(entity);
                objRefs.add(null);
            } else {
                var newEntity = entityFactory.createEntity(ori.getRealType());
                objRefFuture.setValue(newEntity);
                objRefs.add(null);
            }
        }

        var objects = cache.getObjects(objRefs, CacheDirective.returnMisses());

        // ObjectFutures have to be handled in order
        for (int i = 0, size = objectFutures.size(); i < size; i++) {
            if (objRefs.get(i) == null) {
                continue;
            }

            var objRefFuture = (ObjRefFuture) objectFutures.get(i);
            var object = objects.get(i);
            objRefFuture.setValue(object);
        }
    }
}
