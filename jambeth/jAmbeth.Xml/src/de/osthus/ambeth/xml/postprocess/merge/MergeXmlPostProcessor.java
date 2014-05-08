package de.osthus.ambeth.xml.postprocess.merge;

import java.util.List;

import de.osthus.ambeth.cache.CacheFactoryDirective;
import de.osthus.ambeth.cache.ICacheFactory;
import de.osthus.ambeth.cache.IDisposableCache;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.collections.ISet;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.ioc.RegisterPhaseDelegate;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IMergeController;
import de.osthus.ambeth.merge.IObjRefHelper;
import de.osthus.ambeth.merge.MergeHandle;
import de.osthus.ambeth.merge.model.ICUDResult;
import de.osthus.ambeth.merge.model.IChangeContainer;
import de.osthus.ambeth.merge.model.IDirectObjRef;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.transfer.CUDResult;
import de.osthus.ambeth.merge.transfer.CreateContainer;
import de.osthus.ambeth.typeinfo.ITypeInfoItem;
import de.osthus.ambeth.typeinfo.ITypeInfoProvider;
import de.osthus.ambeth.util.ParamChecker;
import de.osthus.ambeth.xml.pending.ArraySetterCommand;
import de.osthus.ambeth.xml.pending.ICommandBuilder;
import de.osthus.ambeth.xml.pending.ICommandTypeExtendable;
import de.osthus.ambeth.xml.pending.ICommandTypeRegistry;
import de.osthus.ambeth.xml.pending.IObjectCommand;
import de.osthus.ambeth.xml.pending.IObjectFuture;
import de.osthus.ambeth.xml.pending.ObjRefFuture;
import de.osthus.ambeth.xml.postprocess.IPostProcessReader;
import de.osthus.ambeth.xml.postprocess.IPostProcessWriter;
import de.osthus.ambeth.xml.postprocess.IXmlPostProcessor;

public class MergeXmlPostProcessor implements IXmlPostProcessor, IInitializingBean, IStartingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected IServiceContext beanContext;

	protected ICacheFactory cacheFactory;

	protected ICommandBuilder commandBuilder;

	protected IMergeController mergeController;

	protected IObjRefHelper oriHelper;

	protected ITypeInfoProvider typeInfoProvider;

	protected ITypeInfoItem directObjRefDirectMember;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(beanContext, "beanContext");
		ParamChecker.assertNotNull(cacheFactory, "cacheFactory");
		ParamChecker.assertNotNull(commandBuilder, "commandBuilder");
		ParamChecker.assertNotNull(mergeController, "mergeController");
		ParamChecker.assertNotNull(oriHelper, "oriHelper");
		ParamChecker.assertNotNull(typeInfoProvider, "typeInfoProvider");
	}

	@Override
	public void afterStarted() throws Throwable
	{
		directObjRefDirectMember = typeInfoProvider.getHierarchicMember(IDirectObjRef.class, "Direct");
	}

	public void setBeanContext(IServiceContext beanContext)
	{
		this.beanContext = beanContext;
	}

	public void setCacheFactory(ICacheFactory cacheFactory)
	{
		this.cacheFactory = cacheFactory;
	}

	public void setCommandBuilder(ICommandBuilder commandBuilder)
	{
		this.commandBuilder = commandBuilder;
	}

	public void setMergeController(IMergeController mergeController)
	{
		this.mergeController = mergeController;
	}

	public void setOriHelper(IObjRefHelper oriHelper)
	{
		this.oriHelper = oriHelper;
	}

	public void setTypeInfoProvider(ITypeInfoProvider typeInfoProvider)
	{
		this.typeInfoProvider = typeInfoProvider;
	}

	@Override
	public Object processWrite(IPostProcessWriter writer)
	{
		ISet<Object> substitutedEntities = writer.getSubstitutedEntities();
		if (substitutedEntities.isEmpty())
		{
			return null;
		}

		final IDisposableCache childCache = cacheFactory.create(CacheFactoryDirective.NoDCE);
		IServiceContext mergeContext = beanContext.createService("mergeXml", new RegisterPhaseDelegate()
		{
			@Override
			public void invoke(IBeanContextFactory childContextFactory)
			{
				childContextFactory.registerAutowireableBean(MergeHandle.class, MergeHandle.class).propertyValue("Cache", childCache);
			}
		});
		try
		{
			IMap<Object, Integer> mutableToIdMap = writer.getMutableToIdMap();
			IObjRefHelper oriHelper = this.oriHelper;
			MergeHandle mergeHandle = mergeContext.getService(MergeHandle.class);
			IList<Object> toMerge = new ArrayList<Object>(substitutedEntities.size());
			for (Object entity : substitutedEntities)
			{
				toMerge.add(entity);
				IObjRef ori = oriHelper.entityToObjRef(entity);
				mergeHandle.getObjToOriDict().put(entity, ori);
				Integer id = mutableToIdMap.get(entity);
				mutableToIdMap.put(ori, id);
			}
			ICUDResult cudResult = mergeController.mergeDeep(toMerge, mergeHandle);
			if (!cudResult.getAllChanges().isEmpty())
			{
				return cudResult;
			}
			else
			{
				return null;
			}
		}
		finally
		{
			mergeContext.dispose();
		}
	}

	@Override
	public void processRead(IPostProcessReader reader)
	{
		reader.nextTag();

		ICommandTypeRegistry commandTypeRegistry = reader.getCommandTypeRegistry();
		ICommandTypeExtendable commandTypeExtendable = reader.getCommandTypeExtendable();
		commandTypeExtendable.registerOverridingCommandType(MergeArraySetterCommand.class, ArraySetterCommand.class);
		Object result = reader.readObject();
		commandTypeExtendable.unregisterOverridingCommandType(MergeArraySetterCommand.class, ArraySetterCommand.class);

		if (!(result instanceof CUDResult))
		{
			throw new IllegalArgumentException("Can only handle results of type '" + CUDResult.class.getName() + "'. Result of type '"
					+ result.getClass().getName() + "' given.");
		}

		ICommandBuilder commandBuilder = this.commandBuilder;
		CUDResult cudResult = (CUDResult) result;
		List<IChangeContainer> changes = cudResult.getAllChanges();
		for (int i = 0, size = changes.size(); i < size; i++)
		{
			IChangeContainer changeContainer = changes.get(i);
			if (!(changeContainer instanceof CreateContainer))
			{
				continue;
			}

			IObjRef ori = changeContainer.getReference();
			if (ori == null)
			{
				continue;
			}
			else if (ori instanceof IDirectObjRef)
			{
				IObjectFuture objectFuture = new ObjRefFuture(ori);
				IObjectCommand setterCommand = commandBuilder.build(commandTypeRegistry, objectFuture, ori, directObjRefDirectMember);
				reader.addObjectCommand(setterCommand);
				IObjectCommand mergeCommand = commandBuilder.build(commandTypeRegistry, objectFuture, changeContainer);
				reader.addObjectCommand(mergeCommand);
			}
			else
			{
				throw new IllegalStateException("Not implemented yet");
			}
		}
	}
}
