package de.osthus.ambeth.change;

import java.util.List;

import de.osthus.ambeth.cache.CacheDirective;
import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.IObjRefHelper;
import de.osthus.ambeth.merge.config.MergeConfigurationConstants;
import de.osthus.ambeth.merge.model.IDirectObjRef;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.model.IPrimitiveUpdateItem;
import de.osthus.ambeth.merge.transfer.CreateContainer;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.persistence.IDirectedLinkMetaData;
import de.osthus.ambeth.persistence.IFieldMetaData;
import de.osthus.ambeth.persistence.ITable;
import de.osthus.ambeth.proxy.IEntityMetaDataHolder;
import de.osthus.ambeth.service.IChangeAggregator;
import de.osthus.ambeth.util.IConversionHelper;
import de.osthus.ambeth.util.ParamHolder;

/**
 * Change collector for entity tables
 */
public class TableChange extends AbstractTableChange
{
	protected final HashMap<IObjRef, IRowCommand> rowCommands = new HashMap<IObjRef, IRowCommand>();

	@Autowired
	protected ICache cache;

	@Autowired
	protected IConversionHelper conversionHelper;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IObjRefHelper oriHelper;

	@Property(name = MergeConfigurationConstants.DeleteDataChangesByAlternateIds, defaultValue = "false")
	protected boolean deleteDataChangesByAlternateIds;

	public IMap<IObjRef, IRowCommand> getRowCommands()
	{
		return rowCommands;
	}

	@Override
	public void addChangeCommand(IChangeCommand command)
	{
		if (!(command instanceof ILinkChangeCommand))
		{
			IObjRef key = command.getReference();

			IRowCommand row = rowCommands.get(key);
			if (row == null)
			{
				row = new RowCommand();
				rowCommands.put(key, row);
			}

			row.addCommand(command);
		}
		else
		{
			addChangeCommand((ILinkChangeCommand) command);
		}
	}

	@Override
	public void addChangeCommand(ILinkChangeCommand command)
	{
		IDirectedLinkMetaData link = command.getDirectedLink().getMetaData();
		IFieldMetaData localField;
		IList<IObjRef> refs = new ArrayList<IObjRef>();
		Object foreignKey = null;
		IObjRef reference = command.getReference();
		if (table.getMetaData().getEntityType() == reference.getRealType())
		{
			refs.add(reference);
			localField = link.getFromField();
			if (!command.getRefsToLink().isEmpty())
			{
				// Foreign key link has to have exactly one id or has to be null
				IFieldMetaData foreignField = link.getToField();
				IObjRef ref = command.getRefsToLink().get(0);
				if (!foreignField.isAlternateId() || foreignField.getIdIndex() == ref.getIdNameIndex())
				{
					foreignKey = ref.getId();
				}
				else if (ref instanceof IDirectObjRef)
				{
					IDirectObjRef directRef = (IDirectObjRef) ref;
					Object container = directRef.getDirect();
					String keyMemberName = foreignField.getMember().getName();
					foreignKey = getAlternateIdValue(container, keyMemberName);
				}
				if (foreignKey == null)
				{
					throw new IllegalArgumentException("Missing the required ID value (is: idIndex " + ref.getIdNameIndex() + ", req: idIndex "
							+ foreignField.getIdIndex() + ")");
				}
			}
			else
			{
				foreignKey = null;
			}
		}
		else
		{
			localField = link.getToField();
			IFieldMetaData foreignField = link.getFromField();
			byte neededIdIndex = foreignField.getIdIndex();
			if (reference instanceof IDirectObjRef)
			{
				// IdIndex of ObjRef does not help here. We have to extract the necessary IdIndex by ourselves
				IDirectObjRef directRef = (IDirectObjRef) reference;
				if (foreignField.getIdIndex() != ObjRef.PRIMARY_KEY_INDEX)
				{
					Object container = directRef.getDirect();
					String keyMemberName = foreignField.getMember().getName();
					foreignKey = getAlternateIdValue(container, keyMemberName);
				}
				else
				{
					foreignKey = directRef.getId();
				}
			}
			else if (neededIdIndex == reference.getIdNameIndex())
			{
				foreignKey = reference.getId();
			}
			else
			{
				throw new IllegalStateException("Attempt to update a foreign key column without knowing the needed key value");
			}
			if (foreignKey == null)
			{
				throw new IllegalArgumentException("Missing the required ID value (req: idIndex " + foreignField.getIdIndex() + ")");
			}
			List<IObjRef> toProcess = command.getRefsToLink();
			if (!toProcess.isEmpty())
			{
				for (int i = toProcess.size(); i-- > 0;)
				{
					refs.add(toProcess.get(i));
				}
			}
			else
			{
				toProcess = command.getRefsToUnlink();
				if (!toProcess.isEmpty())
				{
					foreignKey = null;
					for (int i = toProcess.size(); i-- > 0;)
					{
						refs.add(toProcess.get(i));
					}
				}
			}
		}
		for (int i = refs.size(); i-- > 0;)
		{
			IObjRef objRef = refs.get(i);
			UpdateCommand updateCommand = new UpdateCommand(objRef);
			updateCommand.put(localField, foreignKey);
			addChangeCommand(updateCommand);
		}
	}

	@Override
	public void execute(IChangeAggregator changeAggregator)
	{
		IConversionHelper conversionHelper = this.conversionHelper;
		ITable table = this.table;
		IFieldMetaData versionField = table.getMetaData().getVersionField();
		Class<?> versionTypeOfObject = versionField != null ? versionField.getMember().getElementType() : null;
		ArrayList<IObjRef> toDelete = new ArrayList<IObjRef>();
		IList<IRowCommand> commands = rowCommands.values();
		table.startBatch();
		try
		{
			for (int i = commands.size(); i-- > 0;)
			{
				IRowCommand rowCommand = commands.get(i);
				IChangeCommand changeCommand = rowCommand.getCommand();
				IObjRef reference = changeCommand.getReference();
				if (changeCommand instanceof ICreateCommand)
				{
					ICreateCommand command = (ICreateCommand) changeCommand;

					ParamHolder<Object> newId = new ParamHolder<Object>();
					Object version = table.insert(reference.getId(), newId, command.getItems());
					if (reference instanceof IDirectObjRef)
					{
						((IDirectObjRef) reference).setDirect(null);
					}
					if (versionTypeOfObject != null)
					{
						version = conversionHelper.convertValueToType(versionTypeOfObject, version);
						reference.setVersion(version);
					}
					else
					{
						reference.setVersion(null);
					}
					reference.setId(newId.getValue());
					changeAggregator.dataChangeInsert(reference);
				}
				else if (changeCommand instanceof IUpdateCommand)
				{
					IUpdateCommand command = (IUpdateCommand) changeCommand;

					Object version = table.update(reference.getId(), reference.getVersion(), command.getItems());
					if (versionTypeOfObject != null)
					{
						version = conversionHelper.convertValueToType(versionTypeOfObject, version);
						reference.setVersion(version);
					}
					else
					{
						reference.setVersion(null);
					}
					changeAggregator.dataChangeUpdate(reference);
				}
				else if (changeCommand instanceof IDeleteCommand)
				{
					toDelete.add(reference);
					changeAggregator.dataChangeDelete(reference);
				}
				else
				{
					throw new IllegalCommandException("Unknown command object: " + changeCommand.getClass().getSimpleName());
				}
			}
			if (!toDelete.isEmpty())
			{
				if (deleteDataChangesByAlternateIds)
				{
					IList<Object> objects = cache.getObjects(toDelete, CacheDirective.none());
					IEntityMetaData metaData = entityMetaDataProvider.getMetaData(objects.get(0).getClass());
					for (int i = objects.size(); i-- > 0;)
					{
						IList<IObjRef> allOris = oriHelper.entityToAllObjRefs(objects.get(i), metaData);
						for (int j = allOris.size(); j-- > 0;)
						{
							changeAggregator.dataChangeDelete(allOris.get(j));
						}
					}
				}
				table.delete(toDelete);
			}
			table.finishBatch();
		}
		finally
		{
			table.clearBatch();
		}
	}

	protected Object getAlternateIdValue(Object container, String memberName)
	{
		Object value = null;
		if (container instanceof CreateContainer)
		{
			IPrimitiveUpdateItem[] puis = ((CreateContainer) container).getPrimitives();
			if (puis != null)
			{
				for (int i = puis.length; i-- > 0;)
				{
					IPrimitiveUpdateItem pui = puis[i];
					if (pui.getMemberName().equals(memberName))
					{
						value = pui.getNewValue();
						break;
					}
				}
			}
		}
		else
		{
			IEntityMetaData metaData = ((IEntityMetaDataHolder) container).get__EntityMetaData();
			byte idIndex = metaData.getIdIndexByMemberName(memberName);
			value = metaData.getAlternateIdMembers()[idIndex].getValue(container);
		}
		return value;
	}
}
