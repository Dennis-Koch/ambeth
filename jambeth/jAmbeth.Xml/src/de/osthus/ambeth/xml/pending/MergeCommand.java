package de.osthus.ambeth.xml.pending;

import java.util.Collection;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.ILinkedSet;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.LinkedHashSet;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.IObjRefHelper;
import de.osthus.ambeth.merge.IProxyHelper;
import de.osthus.ambeth.merge.model.IChangeContainer;
import de.osthus.ambeth.merge.model.IDirectObjRef;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.model.IPrimitiveUpdateItem;
import de.osthus.ambeth.merge.model.IRelationUpdateItem;
import de.osthus.ambeth.merge.transfer.CreateContainer;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.merge.transfer.UpdateContainer;
import de.osthus.ambeth.typeinfo.IRelationInfoItem;
import de.osthus.ambeth.typeinfo.ITypeInfoItem;
import de.osthus.ambeth.util.DirectValueHolderRef;
import de.osthus.ambeth.util.ListUtil;
import de.osthus.ambeth.util.OptimisticLockUtil;
import de.osthus.ambeth.util.ParamChecker;
import de.osthus.ambeth.xml.IReader;

public class MergeCommand extends AbstractObjectCommand implements IObjectCommand, IInitializingBean
{
	@Autowired
	protected ICommandBuilder commandBuilder;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IObjRefHelper oriHelper;

	@Autowired
	protected IProxyHelper proxyHelper;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		ParamChecker.assertParamOfType(parent, "Parent", IChangeContainer.class);
	}

	@Override
	public void execute(IReader reader)
	{
		IPrimitiveUpdateItem[] puis;
		IRelationUpdateItem[] ruis;
		if (parent instanceof CreateContainer)
		{
			CreateContainer createContainer = (CreateContainer) parent;
			puis = createContainer.getPrimitives();
			ruis = createContainer.getRelations();
		}
		else if (parent instanceof UpdateContainer)
		{
			UpdateContainer updateContainer = (UpdateContainer) parent;
			puis = updateContainer.getPrimitives();
			ruis = updateContainer.getRelations();
		}
		else
		{
			throw new IllegalArgumentException("Unsupported " + IChangeContainer.class.getSimpleName() + " of type '" + parent.getClass().getName() + "'");
		}

		Object entity = objectFuture.getValue();
		Class<?> realType = entity.getClass();
		IEntityMetaData metadata = entityMetaDataProvider.getMetaData(realType);
		applyPrimitiveUpdateItems(entity, puis, metadata);
		applyRelationUpdateItems(entity, ruis, parent instanceof UpdateContainer, metadata, reader);
	}

	protected void applyPrimitiveUpdateItems(Object entity, IPrimitiveUpdateItem[] puis, IEntityMetaData metadata)
	{
		if (puis == null)
		{
			return;
		}

		for (IPrimitiveUpdateItem pui : puis)
		{
			String memberName = pui.getMemberName();
			Object newValue = pui.getNewValue();
			ITypeInfoItem member = metadata.getMemberByName(memberName);
			member.setValue(entity, newValue);
		}
	}

	protected void applyRelationUpdateItems(Object entity, IRelationUpdateItem[] ruis, boolean isUpdate, IEntityMetaData metadata, IReader reader)
	{
		if (ruis == null)
		{
			return;
		}

		IList<Object> toPrefetch = new ArrayList<Object>();
		IProxyHelper proxyHelper = this.proxyHelper;
		for (IRelationUpdateItem rui : ruis)
		{
			String memberName = rui.getMemberName();
			IRelationInfoItem member = (IRelationInfoItem) metadata.getMemberByName(memberName);
			if (proxyHelper.isInitialized(entity, member))
			{
				throw new IllegalStateException("ValueHolder already initialized for property '" + memberName + "'");
			}

			IObjRef[] existingORIs = proxyHelper.getObjRefs(entity, memberName);
			IObjRef[] addedORIs = rui.getAddedORIs();
			IObjRef[] removedORIs = rui.getRemovedORIs();

			IObjRef[] newORIs;
			if (existingORIs.length == 0)
			{
				if (removedORIs != null && addedORIs.length > 0)
				{
					throw new IllegalArgumentException("Removing from empty member");
				}
				newORIs = addedORIs != null ? addedORIs : ObjRef.EMPTY_ARRAY;
			}
			else
			{
				// Set to efficiently remove entries
				ILinkedSet<IObjRef> existingORIsSet = new LinkedHashSet<IObjRef>(existingORIs);
				if (removedORIs != null && removedORIs.length > 0)
				{
					for (IObjRef removedORI : removedORIs)
					{
						if (!existingORIsSet.remove(removedORI))
						{
							throw OptimisticLockUtil.throwModified(oriHelper.entityToObjRef(entity), null, entity);
						}
					}
				}
				if (addedORIs != null && addedORIs.length > 0)
				{
					for (IObjRef addedORI : addedORIs)
					{
						if (!existingORIsSet.add(addedORI))
						{
							throw OptimisticLockUtil.throwModified(oriHelper.entityToObjRef(entity), null, entity);
						}
					}
				}
				if (existingORIsSet.size() == 0)
				{
					newORIs = ObjRef.EMPTY_ARRAY;
				}
				else
				{
					newORIs = existingORIsSet.toArray(IObjRef.class);
				}
			}

			if (isUpdate)
			{
				proxyHelper.setObjRefs(entity, member, newORIs);
				DirectValueHolderRef dvhr = new DirectValueHolderRef(entity, member);
				toPrefetch.add(dvhr);
			}
			else
			{
				resolveAndSetEntities(entity, newORIs, member, reader);
			}
		}
		if (!toPrefetch.isEmpty())
		{
			IObjectFuture objectFuture = new PrefetchFuture(toPrefetch);
			IObjectCommand command = commandBuilder.build(reader.getCommandTypeRegistry(), objectFuture, null);
			reader.addObjectCommand(command);
		}
	}

	protected void resolveAndSetEntities(Object entity, IObjRef[] newORIs, IRelationInfoItem member, IReader reader)
	{
		if (!member.isToMany())
		{
			if (newORIs.length == 0)
			{
				return;
			}
			else if (newORIs.length == 1)
			{
				IObjectFuture objectFuture = new ObjRefFuture(newORIs[0]);
				IObjectCommand command = commandBuilder.build(reader.getCommandTypeRegistry(), objectFuture, entity, member);
				reader.addObjectCommand(command);
			}
			else
			{
				throw new IllegalArgumentException("Multiple values for to-one relation");
			}
		}
		else
		{
			Collection<Object> coll = ListUtil.createCollectionOfType(member.getRealType(), newORIs.length);

			boolean useObjectFuture = false;
			ICommandBuilder commandBuilder = this.commandBuilder;
			ICommandTypeRegistry commandTypeRegistry = reader.getCommandTypeRegistry();
			for (IObjRef ori : newORIs)
			{
				if (!(ori instanceof IDirectObjRef))
				{
					IObjectFuture objectFuture = new ObjRefFuture(ori);
					;
					IObjectCommand command = commandBuilder.build(commandTypeRegistry, objectFuture, coll);
					reader.addObjectCommand(command);
					useObjectFuture = true;
					continue;
				}

				Object item = ((IDirectObjRef) ori).getDirect();
				if (useObjectFuture)
				{
					IObjectCommand command = commandBuilder.build(commandTypeRegistry, null, coll, item);
					reader.addObjectCommand(command);
				}
				else
				{
					coll.add(item);
				}
			}
		}
	}
}
