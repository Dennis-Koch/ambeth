package com.koch.ambeth.xml;

import java.io.EOFException;
import java.util.Map.Entry;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.koch.ambeth.ioc.extendable.IMapExtendableContainer;
import com.koch.ambeth.ioc.extendable.MapExtendableContainer;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.ILinkedMap;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.ISet;
import com.koch.ambeth.util.collections.IntKeyMap;
import com.koch.ambeth.util.collections.LinkedHashMap;
import com.koch.ambeth.util.collections.LinkedHashSet;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.xml.pending.ICommandTypeExtendable;
import com.koch.ambeth.xml.pending.ICommandTypeRegistry;
import com.koch.ambeth.xml.pending.IObjectCommand;
import com.koch.ambeth.xml.pending.IObjectFuture;
import com.koch.ambeth.xml.pending.IObjectFutureHandler;
import com.koch.ambeth.xml.pending.IObjectFutureHandlerRegistry;
import com.koch.ambeth.xml.pending.MergeCommand;
import com.koch.ambeth.xml.postprocess.IPostProcessReader;

public class DefaultXmlReader implements IReader, IPostProcessReader, ICommandTypeRegistry, ICommandTypeExtendable
{
	protected final IntKeyMap<Object> idToObjectMap = new IntKeyMap<Object>();

	protected final HashMap<Class<?>, SpecifiedMember[]> typeToMemberMap = new HashMap<Class<?>, SpecifiedMember[]>();

	protected final IList<IObjectCommand> objectCommands = new ArrayList<IObjectCommand>();

	protected final IMapExtendableContainer<Class<? extends IObjectCommand>, Class<? extends IObjectCommand>> commandTypeExtendable = new MapExtendableContainer<Class<? extends IObjectCommand>, Class<? extends IObjectCommand>>(
			"Overriding command type", "Original command type");

	protected final XmlPullParser pullParser;

	protected final ICyclicXmlController xmlController;

	protected final IObjectFutureHandlerRegistry objectFutureHandlerRegistry;

	public DefaultXmlReader(XmlPullParser pullParser, ICyclicXmlController xmlController, IObjectFutureHandlerRegistry objectFutureHandlerRegistry)
	{
		this.pullParser = pullParser;
		this.xmlController = xmlController;
		this.objectFutureHandlerRegistry = objectFutureHandlerRegistry;
	}

	@Override
	public String getAttributeValue(String attributeName)
	{
		return pullParser.getAttributeValue(null, attributeName);
	}

	@Override
	public Object readObject()
	{
		Object object = xmlController.readObject(this);
		return object;
	}

	@Override
	public Object readObject(Class<?> returnType)
	{
		Object object = xmlController.readObject(returnType, this);
		return object;
	}

	@Override
	public String getElementName()
	{
		return pullParser.getName();
	}

	@Override
	public boolean nextToken()
	{
		try
		{
			pullParser.nextToken();
			return true;
		}
		catch (EOFException e)
		{
			return false;
		}
		catch (Exception e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public String getElementValue()
	{
		try
		{
			return pullParser.getText();
		}
		catch (Exception e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public boolean isEmptyElement()
	{
		try
		{
			return pullParser.isEmptyElementTag();
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public void moveOverElementEnd()
	{
		try
		{
			if (pullParser.getEventType() == XmlPullParser.END_TAG)
			{
				pullParser.nextTag();
			}
			else if (pullParser.isEmptyElementTag())
			{
				pullParser.nextTag();
				pullParser.nextTag();
			}
		}
		catch (XmlPullParserException e)
		{
			if (e.getMessage().startsWith("expected START_TAG or END_TAG not END_DOCUMENT"))
			{
				return;
			}
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public boolean nextTag()
	{
		try
		{
			pullParser.nextTag();
			return true;
		}
		catch (EOFException e)
		{
			return false;
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public boolean isStartTag()
	{
		try
		{
			return pullParser.getEventType() == XmlPullParser.START_TAG;
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public Object getObjectById(int id)
	{
		return getObjectById(id, true);
	}

	@Override
	public Object getObjectById(int id, boolean checkExistence)
	{
		Object object = idToObjectMap.get(id);
		if (object == null && checkExistence)
		{
			throw new IllegalStateException("No object found in xml with id " + id);
		}
		return object;
	}

	@Override
	public void putObjectWithId(Object obj, int id)
	{
		Object existingObj = idToObjectMap.get(id);
		if (existingObj != null && existingObj != obj)
		{
			throw new IllegalStateException("Already mapped object to id " + id + " found");
		}
		idToObjectMap.put(id, obj);
	}

	@Override
	public void putMembersOfType(Class<?> type, SpecifiedMember[] members)
	{
		if (!typeToMemberMap.putIfNotExists(type, members))
		{
			throw new IllegalStateException("Already mapped type '" + type + "'");
		}
	}

	@Override
	public SpecifiedMember[] getMembersOfType(Class<?> type)
	{
		return typeToMemberMap.get(type);
	}

	@Override
	public void addObjectCommand(IObjectCommand objectCommand)
	{
		objectCommands.add(objectCommand);
	}

	@Override
	public void executeObjectCommands()
	{
		while (!objectCommands.isEmpty())
		{
			IList<IObjectCommand> commandSnapShot = new ArrayList<IObjectCommand>(objectCommands);
			objectCommands.clear();

			resolveObjectFutures(commandSnapShot);

			// Commands have to be executed in-order (e.g. for CollectionSetterCommands)
			// except for MergeCommand which have to be last
			IList<IObjectCommand> mergeCommands = new ArrayList<IObjectCommand>(commandSnapShot.size());
			for (int i = 0, size = commandSnapShot.size(); i < size; i++)
			{
				IObjectCommand objectCommand = commandSnapShot.get(i);
				if (objectCommand instanceof MergeCommand)
				{
					mergeCommands.add(objectCommand);
					continue;
				}
				objectCommand.execute(this);
			}
			for (int i = 0, size = mergeCommands.size(); i < size; i++)
			{
				IObjectCommand objectCommand = mergeCommands.get(i);
				objectCommand.execute(this);
			}
		}
	}

	protected void resolveObjectFutures(IList<IObjectCommand> objectCommands)
	{
		IObjectFutureHandlerRegistry objectFutureHandlerRegistry = this.objectFutureHandlerRegistry;
		ILinkedMap<Class<? extends IObjectFuture>, ISet<IObjectFuture>> sortedObjectFutures = bucketSortObjectFutures(objectCommands);
		for (Entry<Class<? extends IObjectFuture>, ISet<IObjectFuture>> entry : sortedObjectFutures)
		{
			Class<? extends IObjectFuture> type = entry.getKey();
			ISet<IObjectFuture> objectFutures = entry.getValue();
			IObjectFutureHandler objectFutureHandler = objectFutureHandlerRegistry.getObjectFutureHandler(type);
			if (objectFutureHandler == null)
			{
				throw new UnsupportedOperationException("No handler found for " + IObjectFuture.class.getSimpleName() + "s of type '" + type.getName() + "'");
			}
			objectFutureHandler.handle(objectFutures.toList());
		}
	}

	protected ILinkedMap<Class<? extends IObjectFuture>, ISet<IObjectFuture>> bucketSortObjectFutures(IList<IObjectCommand> objectCommands)
	{
		ILinkedMap<Class<? extends IObjectFuture>, ISet<IObjectFuture>> sortedObjectFutures = new LinkedHashMap<Class<? extends IObjectFuture>, ISet<IObjectFuture>>(
				(int) (objectCommands.size() / 0.75));
		for (int i = 0, size = objectCommands.size(); i < size; i++)
		{
			IObjectCommand objectCommand = objectCommands.get(i);
			IObjectFuture objectFuture = objectCommand.getObjectFuture();
			if (objectFuture != null)
			{
				Class<? extends IObjectFuture> type = objectFuture.getClass();
				ISet<IObjectFuture> objectFutures = sortedObjectFutures.get(type);
				if (objectFutures == null)
				{
					objectFutures = new LinkedHashSet<IObjectFuture>();
					sortedObjectFutures.put(type, objectFutures);
				}
				objectFutures.add(objectFuture);
			}
		}
		return sortedObjectFutures;
	}

	@Override
	public ICommandTypeRegistry getCommandTypeRegistry()
	{
		return this;
	}

	@Override
	public ICommandTypeExtendable getCommandTypeExtendable()
	{
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends IObjectCommand> Class<? extends T> getOverridingCommandType(Class<? extends T> commandType)
	{
		return (Class<? extends T>) commandTypeExtendable.getExtension(commandType);
	}

	@Override
	public void registerOverridingCommandType(Class<? extends IObjectCommand> overridingCommandType, Class<? extends IObjectCommand> commandType)
	{
		commandTypeExtendable.register(overridingCommandType, commandType);
	}

	@Override
	public void unregisterOverridingCommandType(Class<? extends IObjectCommand> overridingCommandType, Class<? extends IObjectCommand> commandType)
	{
		commandTypeExtendable.unregister(overridingCommandType, commandType);
	}

	@Override
	public String toString()
	{
		if (isStartTag())
		{
			if (isEmptyElement())
			{
				return getElementName() + "|EMPTY";
			}
			return getElementName() + "|START";
		}
		return getElementName() + "|END";
	}
}
