package de.osthus.ambeth.compositeid;

import de.osthus.ambeth.bytecode.IBytecodeEnhancer;
import de.osthus.ambeth.cache.AbstractCacheValue;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.metadata.IMemberTypeProvider;
import de.osthus.ambeth.metadata.Member;
import de.osthus.ambeth.metadata.PrimitiveMember;
import de.osthus.ambeth.util.IConversionHelper;

public class CompositeIdFactory implements ICompositeIdFactory, IInitializingBean
{
	@LogInstance
	private ILogger log;

	@Autowired(optional = true)
	protected IBytecodeEnhancer bytecodeEnhancer;

	@Autowired
	protected IConversionHelper conversionHelper;

	@Autowired
	protected IMemberTypeProvider memberTypeProvider;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		if (bytecodeEnhancer == null)
		{
			log.debug("No bytecodeEnhancer specified: Composite ID feature deactivated");
		}
	}

	@Override
	public PrimitiveMember createCompositeIdMember(IEntityMetaData metaData, PrimitiveMember[] idMembers)
	{
		return createCompositeIdMember(metaData.getEntityType(), idMembers);
	}

	@Override
	public PrimitiveMember createCompositeIdMember(Class<?> entityType, PrimitiveMember[] idMembers)
	{
		if (bytecodeEnhancer == null)
		{
			throw new UnsupportedOperationException("No bytecodeEnhancer specified");
		}
		StringBuilder nameSB = new StringBuilder();
		// order does matter here
		for (int a = 0, size = idMembers.length; a < size; a++)
		{
			String name = idMembers[a].getName();
			if (a > 0)
			{
				nameSB.append('&');
			}
			nameSB.append(name);
		}
		Class<?> compositeIdType = bytecodeEnhancer.getEnhancedType(Object.class, new CompositeIdEnhancementHint(idMembers));
		try
		{
			return new CompositeIdMember(entityType, compositeIdType, nameSB.toString(), idMembers, memberTypeProvider);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public Object createCompositeId(IEntityMetaData metaData, PrimitiveMember compositeIdMember, Object... ids)
	{
		IConversionHelper conversionHelper = this.conversionHelper;
		CompositeIdMember cIdTypeInfoItem = (CompositeIdMember) compositeIdMember;
		Member[] members = cIdTypeInfoItem.getMembers();
		for (int a = ids.length; a-- > 0;)
		{
			Object id = ids[a];
			Object convertedId = conversionHelper.convertValueToType(members[a].getRealType(), id);
			if (convertedId != id)
			{
				ids[a] = convertedId;
			}
		}
		try
		{
			return cIdTypeInfoItem.getRealTypeConstructorAccess().newInstance(ids);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public Object createIdFromPrimitives(IEntityMetaData metaData, int idIndex, Object[] primitives)
	{
		int[][] alternateIdMemberIndicesInPrimitives = metaData.getAlternateIdMemberIndicesInPrimitives();
		int[] compositeIndex = alternateIdMemberIndicesInPrimitives[idIndex];

		if (compositeIndex.length == 1)
		{
			return primitives[compositeIndex[0]];
		}
		PrimitiveMember compositeIdMember = metaData.getAlternateIdMembers()[idIndex];
		Object[] ids = new Object[compositeIndex.length];
		for (int a = compositeIndex.length; a-- > 0;)
		{
			ids[a] = primitives[compositeIndex[a]];
		}
		return createCompositeId(metaData, compositeIdMember, ids);
	}

	@Override
	public Object createIdFromPrimitives(IEntityMetaData metaData, int idIndex, AbstractCacheValue cacheValue)
	{
		int[][] alternateIdMemberIndicesInPrimitives = metaData.getAlternateIdMemberIndicesInPrimitives();
		int[] compositeIndex = alternateIdMemberIndicesInPrimitives[idIndex];

		if (compositeIndex.length == 1)
		{
			return cacheValue.getPrimitive(compositeIndex[0]);
		}
		PrimitiveMember compositeIdMember = metaData.getAlternateIdMembers()[idIndex];
		Object[] ids = new Object[compositeIndex.length];
		for (int a = compositeIndex.length; a-- > 0;)
		{
			ids[a] = cacheValue.getPrimitive(compositeIndex[a]);
		}
		return createCompositeId(metaData, compositeIdMember, ids);
	}

	@Override
	public Object createIdFromEntity(IEntityMetaData metaData, int idIndex, Object entity)
	{
		int[][] alternateIdMemberIndicesInPrimitives = metaData.getAlternateIdMemberIndicesInPrimitives();
		int[] compositeIndex = alternateIdMemberIndicesInPrimitives[idIndex];

		if (compositeIndex.length == 1)
		{
			return metaData.getPrimitiveMembers()[compositeIndex[0]].getValue(entity);
		}
		PrimitiveMember compositeIdMember = metaData.getAlternateIdMembers()[idIndex];
		PrimitiveMember[] primitiveMembers = metaData.getPrimitiveMembers();
		Object[] ids = new Object[compositeIndex.length];
		for (int a = compositeIndex.length; a-- > 0;)
		{
			ids[a] = primitiveMembers[compositeIndex[a]].getValue(entity);
		}
		return createCompositeId(metaData, compositeIdMember, ids);
	}
}
