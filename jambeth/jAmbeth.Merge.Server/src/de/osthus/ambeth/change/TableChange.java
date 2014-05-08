package de.osthus.ambeth.change;

import java.util.List;

import de.osthus.ambeth.cache.CacheDirective;
import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.IObjRefHelper;
import de.osthus.ambeth.merge.config.MergeConfigurationConstants;
import de.osthus.ambeth.merge.model.IDirectObjRef;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.model.IPrimitiveUpdateItem;
import de.osthus.ambeth.merge.transfer.CreateContainer;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.persistence.IDirectedLink;
import de.osthus.ambeth.persistence.IField;
import de.osthus.ambeth.service.IChangeAggregator;
import de.osthus.ambeth.util.IConversionHelper;
import de.osthus.ambeth.util.ParamChecker;
import de.osthus.ambeth.util.ParamHolder;

/**
 * Change collector for entity tables
 */
public class TableChange extends AbstractTableChange
{
	protected final HashMap<IObjRef, IRowCommand> rowCommands = new HashMap<IObjRef, IRowCommand>();

	protected ICache cache;

	protected IConversionHelper conversionHelper;

	protected IEntityMetaDataProvider entityMetaDataProvider;

	protected IObjRefHelper oriHelper;

	private boolean deleteDataChangesByAlternateIds;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		ParamChecker.assertNotNull(cache, "cache");
		ParamChecker.assertNotNull(conversionHelper, "ConversionHelper");
		ParamChecker.assertNotNull(entityMetaDataProvider, "entityMetaDataProvider");
		ParamChecker.assertNotNull(oriHelper, "oriHelper");
	}

	public void setCache(ICache cache)
	{
		this.cache = cache;
	}

	public void setConversionHelper(IConversionHelper conversionHelper)
	{
		this.conversionHelper = conversionHelper;
	}

	public void setEntityMetaDataProvider(IEntityMetaDataProvider entityMetaDataProvider)
	{
		this.entityMetaDataProvider = entityMetaDataProvider;
	}

	public void setOriHelper(IObjRefHelper oriHelper)
	{
		this.oriHelper = oriHelper;
	}

	@Property(name = MergeConfigurationConstants.DeleteDataChangesByAlternateIds, defaultValue = "false")
	public void setDeleteDataChangesByAlternateIds(boolean deleteDataChangesByAlternateIds)
	{
		this.deleteDataChangesByAlternateIds = deleteDataChangesByAlternateIds;
	}

	@Override
	public void dispose()
	{
		if (table != null)
		{
			table = null;

			IList<IRowCommand> rowList = rowCommands.values();
			for (int i = rowList.size(); i-- > 0;)
			{
				rowList.get(i).dispose();
			}
			rowCommands.clear();
		}
		super.dispose();
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
		IDirectedLink link = command.getDirectedLink();
		String localField;
		IList<IObjRef> refs = new ArrayList<IObjRef>();
		Object foreignKey = null;
		try
		{
			IObjRef reference = command.getReference();
			if (table.getEntityType() == reference.getRealType())
			{
				refs.add(reference);
				localField = link.getFromField().getName();
				if (!command.getRefsToLink().isEmpty())
				{
					// Foreign key link has to have exactly one id or has to be null
					IField foreignField = link.getToField();
					IObjRef ref = command.getRefsToLink().get(0);
					if (!foreignField.isAlternateId() || foreignField.getIdIndex() == ref.getIdNameIndex())
					{
						foreignKey = ref.getId();
					}
					else if (ref instanceof IDirectObjRef)
					{
						IDirectObjRef directRef = (IDirectObjRef) ref;
						CreateContainer container = (CreateContainer) directRef.getDirect();
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
				localField = link.getToField().getName();
				IField foreignField = link.getFromField();
				byte neededIdIndex = foreignField.getIdIndex();
				if (reference instanceof IDirectObjRef)
				{
					// IdIndex of ObjRef does not help here. We have to extract the necessary IdIndex by ourselves
					IDirectObjRef directRef = (IDirectObjRef) reference;
					if (foreignField.getIdIndex() != ObjRef.PRIMARY_KEY_INDEX)
					{
						CreateContainer container = (CreateContainer) directRef.getDirect();
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
				UpdateCommand updateCommand = new UpdateCommand();
				updateCommand.setReference(refs.get(i));
				updateCommand.put(localField, foreignKey);
				addChangeCommand(updateCommand);
			}
		}
		finally
		{
			command.dispose();
		}
	}

	@Override
	public void execute(IChangeAggregator changeAggreagator)
	{
		IField versionField = table.getVersionField();
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
					changeAggreagator.dataChangeInsert(reference);
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
					changeAggreagator.dataChangeUpdate(reference);
				}
				else if (changeCommand instanceof IDeleteCommand)
				{
					toDelete.add(reference);
					changeAggreagator.dataChangeDelete(reference);
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
							changeAggreagator.dataChangeDelete(allOris.get(j));
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

	protected Object getAlternateIdValue(CreateContainer container, String memberName)
	{
		Object value = null;
		IPrimitiveUpdateItem[] puis = container.getPrimitives();
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
		return value;
	}
}
