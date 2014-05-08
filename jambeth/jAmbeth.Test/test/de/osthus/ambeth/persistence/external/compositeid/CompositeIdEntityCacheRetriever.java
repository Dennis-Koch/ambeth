package de.osthus.ambeth.persistence.external.compositeid;

import java.util.Collection;
import java.util.List;

import de.osthus.ambeth.cache.model.ILoadContainer;
import de.osthus.ambeth.cache.model.IObjRelation;
import de.osthus.ambeth.cache.model.IObjRelationResult;
import de.osthus.ambeth.cache.transfer.LoadContainer;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.compositeid.CompositeIdTypeInfoItem;
import de.osthus.ambeth.compositeid.ICompositeIdFactory;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.service.ICacheRetriever;
import de.osthus.ambeth.util.ParamChecker;

public class CompositeIdEntityCacheRetriever implements ICacheRetriever, IInitializingBean
{
	public static final Object[] id1_2_data = { (int) 1, "einszwo", "name_einszwo1", (short) 2, (long) 3 };

	public static final Object[] entity2_id1_2_data = { (int) 1, "einszwo", "name_einszwo1", (short) 2, "alt_einszwo" };

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected ICompositeIdFactory compositeIdFactory;

	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(compositeIdFactory, "compositeIdFactory");
		ParamChecker.assertNotNull(entityMetaDataProvider, "entityMetaDataProvider");
	}

	public void setCompositeIdFactory(ICompositeIdFactory compositeIdFactory)
	{
		this.compositeIdFactory = compositeIdFactory;
	}

	public void setEntityMetaDataProvider(IEntityMetaDataProvider entityMetaDataProvider)
	{
		this.entityMetaDataProvider = entityMetaDataProvider;
	}

	@Override
	public List<ILoadContainer> getEntities(List<IObjRef> orisToLoad)
	{
		ArrayList<ILoadContainer> lcs = new ArrayList<ILoadContainer>(orisToLoad.size());
		for (int a = 0, size = orisToLoad.size(); a < size; a++)
		{
			IObjRef objRef = orisToLoad.get(a);
			Class<?> realType = objRef.getRealType();
			IEntityMetaData metaData = entityMetaDataProvider.getMetaData(realType);
			if (CompositeIdEntity.class.equals(realType))
			{
				handleCompositeIdEntity(metaData, lcs, objRef);
			}
			else if (CompositeIdEntity2.class.equals(realType))
			{
				handleCompositeIdEntity2(metaData, lcs, objRef);
			}
		}
		return lcs;
	}

	protected void handleCompositeIdEntity(IEntityMetaData metaData, Collection<ILoadContainer> lcs, IObjRef objRef)
	{
		Object id = objRef.getId();
		byte idNameIndex = objRef.getIdNameIndex();
		if (idNameIndex == ObjRef.PRIMARY_KEY_INDEX)
		{
			Object id1 = ((CompositeIdTypeInfoItem) metaData.getIdMember()).getDecompositedValue(id, 0);
			Object id2 = ((CompositeIdTypeInfoItem) metaData.getIdMember()).getDecompositedValue(id, 1);
			if (id1_2_data[0].equals(id1) && id1_2_data[1].equals(id2))
			{
				LoadContainer lc = new LoadContainer();
				lc.setReference(objRef);
				lc.setRelations(ObjRef.EMPTY_ARRAY_ARRAY);
				lc.setPrimitives(new Object[metaData.getPrimitiveMembers().length]);
				lc.getPrimitives()[metaData.getIndexByPrimitiveName("Name")] = id1_2_data[2];
				lc.getPrimitives()[metaData.getIndexByPrimitiveName("Aid1")] = id1_2_data[3];
				lc.getPrimitives()[metaData.getIndexByPrimitiveName("Aid2")] = id1_2_data[4];
				lcs.add(lc);
			}
		}
		else if (idNameIndex == 0)
		{
			Object id1 = ((CompositeIdTypeInfoItem) metaData.getAlternateIdMembers()[idNameIndex]).getDecompositedValue(id, 1);
			Object id2 = ((CompositeIdTypeInfoItem) metaData.getAlternateIdMembers()[idNameIndex]).getDecompositedValue(id, 0);
			if (id1_2_data[3].equals(id1) && id1_2_data[4].equals(id2))
			{
				LoadContainer lc = new LoadContainer();
				Object primaryId = compositeIdFactory.createCompositeId(metaData, metaData.getIdMember(), id1_2_data[0], id1_2_data[1]);
				lc.setReference(new ObjRef(metaData.getEntityType(), ObjRef.PRIMARY_KEY_INDEX, primaryId, null));
				lc.setRelations(ObjRef.EMPTY_ARRAY_ARRAY);
				lc.setPrimitives(new Object[metaData.getPrimitiveMembers().length]);
				lc.getPrimitives()[metaData.getIndexByPrimitiveName("Name")] = id1_2_data[2];
				lc.getPrimitives()[metaData.getIndexByPrimitiveName("Aid1")] = id1_2_data[3];
				lc.getPrimitives()[metaData.getIndexByPrimitiveName("Aid2")] = id1_2_data[4];
				lcs.add(lc);
			}
		}
	}

	protected void handleCompositeIdEntity2(IEntityMetaData metaData, Collection<ILoadContainer> lcs, IObjRef objRef)
	{
		Object id = objRef.getId();
		byte idNameIndex = objRef.getIdNameIndex();
		if (idNameIndex == ObjRef.PRIMARY_KEY_INDEX)
		{
			Object id1 = ((CompositeIdTypeInfoItem) metaData.getIdMember()).getDecompositedValue(id, 0);
			Object id2 = ((CompositeIdTypeInfoItem) metaData.getIdMember()).getDecompositedValue(id, 1);
			if (entity2_id1_2_data[0].equals(id1) && entity2_id1_2_data[1].equals(id2))
			{
				LoadContainer lc = new LoadContainer();
				lc.setReference(objRef);
				lc.setRelations(ObjRef.EMPTY_ARRAY_ARRAY);
				lc.setPrimitives(new Object[metaData.getPrimitiveMembers().length]);
				lc.getPrimitives()[metaData.getIndexByPrimitiveName("Name")] = entity2_id1_2_data[2];
				lc.getPrimitives()[metaData.getIndexByPrimitiveName("Aid1")] = entity2_id1_2_data[3];
				lc.getPrimitives()[metaData.getIndexByPrimitiveName("Aid2.Sid")] = entity2_id1_2_data[4];
				lcs.add(lc);
			}
		}
		else if (idNameIndex == 0)
		{
			Object id1 = ((CompositeIdTypeInfoItem) metaData.getAlternateIdMembers()[idNameIndex]).getDecompositedValue(id, 1);
			Object id2 = ((CompositeIdTypeInfoItem) metaData.getAlternateIdMembers()[idNameIndex]).getDecompositedValue(id, 0);
			if (entity2_id1_2_data[3].equals(id1) && entity2_id1_2_data[4].equals(id2))
			{
				LoadContainer lc = new LoadContainer();
				Object primaryId = compositeIdFactory.createCompositeId(metaData, metaData.getIdMember(), entity2_id1_2_data[0], entity2_id1_2_data[1]);
				lc.setReference(new ObjRef(metaData.getEntityType(), ObjRef.PRIMARY_KEY_INDEX, primaryId, null));
				lc.setRelations(ObjRef.EMPTY_ARRAY_ARRAY);
				lc.setPrimitives(new Object[metaData.getPrimitiveMembers().length]);
				lc.getPrimitives()[metaData.getIndexByPrimitiveName("Name")] = entity2_id1_2_data[2];
				lc.getPrimitives()[metaData.getIndexByPrimitiveName("Aid1")] = entity2_id1_2_data[3];
				lc.getPrimitives()[metaData.getIndexByPrimitiveName("Aid2.Sid")] = entity2_id1_2_data[4];
				lcs.add(lc);
			}
		}
	}

	@Override
	public List<IObjRelationResult> getRelations(List<IObjRelation> objRelations)
	{
		throw new UnsupportedOperationException();
	}
}
