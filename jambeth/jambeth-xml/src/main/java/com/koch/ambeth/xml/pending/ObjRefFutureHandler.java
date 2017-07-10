package com.koch.ambeth.xml.pending;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.IEntityFactory;
import com.koch.ambeth.merge.cache.CacheDirective;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.model.IDirectObjRef;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class ObjRefFutureHandler implements IObjectFutureHandler {
	@Autowired
	protected ICache cache;

	@Autowired
	protected IEntityFactory entityFactory;

	@Override
	public void handle(IList<IObjectFuture> objectFutures) {
		IEntityFactory entityFactory = this.entityFactory;
		IList<IObjRef> oris = new ArrayList<>(objectFutures.size());
		// ObjectFutures have to be handled in order
		for (int i = 0, size = objectFutures.size(); i < size; i++) {
			IObjectFuture objectFuture = objectFutures.get(i);
			if (!(objectFuture instanceof ObjRefFuture)) {
				throw new IllegalArgumentException(
						"'" + getClass().getName() + "' cannot handle " + IObjectFuture.class.getSimpleName()
								+ " implementations of type '" + objectFuture.getClass().getName() + "'");
			}

			ObjRefFuture objRefFuture = (ObjRefFuture) objectFuture;
			IObjRef ori = objRefFuture.getOri();
			if (ori.getId() != null) {
				oris.add(ori);
			}
			else if (ori instanceof IDirectObjRef && ((IDirectObjRef) ori).getDirect() != null) {
				Object entity = ((IDirectObjRef) ori).getDirect();
				objRefFuture.setValue(entity);
				oris.add(null);
			}
			else {
				try {
					Object newEntity = entityFactory.createEntity(ori.getRealType());
					objRefFuture.setValue(newEntity);
					oris.add(null);
				}
				catch (Exception e) {
					throw RuntimeExceptionUtil.mask(e);
				}
			}
		}

		IList<Object> objects = cache.getObjects(oris, CacheDirective.returnMisses());

		// ObjectFutures have to be handled in order
		for (int i = 0, size = objectFutures.size(); i < size; i++) {
			if (oris.get(i) == null) {
				continue;
			}

			ObjRefFuture objRefFuture = (ObjRefFuture) objectFutures.get(i);
			Object object = objects.get(i);
			objRefFuture.setValue(object);
		}
	}
}
