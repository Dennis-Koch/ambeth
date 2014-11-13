package de.osthus.ambeth.init;

import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IMergeController;
import de.osthus.ambeth.merge.MergeHandle;
import de.osthus.ambeth.merge.model.ICUDResult;
import de.osthus.ambeth.merge.model.IOriCollection;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.persistence.ILink;
import de.osthus.ambeth.persistence.ITable;
import de.osthus.ambeth.service.IMergeService;
import de.osthus.ambeth.util.ParamChecker;

public class DefaultDatabaseInit<D extends IDatabase> implements IDatabaseInit<D>, IInitializingBean
{

	@LogInstance(DefaultDatabaseInit.class)
	private ILogger log;

	protected IMergeController mergeController;
	protected IMergeService mergeService;
	protected IServiceContext serviceContext;

	@Override
	public void afterPropertiesSet()
	{
		ParamChecker.assertNotNull(mergeController, "MergeController");
		ParamChecker.assertNotNull(mergeService, "MergeService");
		ParamChecker.assertNotNull(serviceContext, "ServiceProvider");
	}

	@Override
	public void init(D database)
	{
		if (log.isInfoEnabled())
		{
			log.info("Backup of database finished");
			log.info("Start initialization of database. This may take several minutes...");
		}

		for (ILink link : database.getLinks())
		{
			if (log.isInfoEnabled())
			{
				log.info("Removing all entries from link: " + link.getName());
			}
			link.unlinkAllIds();
		}
		for (ITable table : database.getTables())
		{
			if (log.isInfoEnabled())
			{
				log.info("Removing all entries from table: " + table.getName());
			}
			table.deleteAll();
		}

		initCustom(database);

		MergeHandle mergeHandle = serviceContext.registerBean(MergeHandle.class).finish();
		ICUDResult cudResult = mergeController.mergeDeep(CreateHelper.getAndClearEntityQueue(), mergeHandle);

		IOriCollection oriCollection = mergeService.merge(cudResult, null);

		mergeController.applyChangesToOriginals(cudResult.getOriginalRefs(), oriCollection.getAllChangeORIs(), oriCollection.getChangedOn(),
				oriCollection.getChangedBy());

		if (log.isInfoEnabled())
		{
			log.info("Initialization of database finished. Please restart this application server without INIT-Argument");
		}
	}

	protected void initCustom(D database)
	{
		// Intended blank
	}

	public void setMergeController(IMergeController mergeController)
	{
		this.mergeController = mergeController;
	}

	public void setMergeService(IMergeService mergeService)
	{
		this.mergeService = mergeService;
	}

	public void setServiceContext(IServiceContext serviceContext)
	{
		this.serviceContext = serviceContext;
	}

}
