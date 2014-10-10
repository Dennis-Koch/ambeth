using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Ioc.Extendable;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Collections;

namespace De.Osthus.Ambeth.ambeth.util.setup
{
    public class DataSetup : IDataSetup, IDatasetBuilderExtensionExtendable
    {
        [LogInstance]
	    private ILogger log;

	    protected IExtendableContainer<IDatasetBuilder> DatasetBuilderContainer = new DefaultExtendableContainer<IDatasetBuilder>("TestBedBuilders");

        public IList<object> ExecuteDatasetBuilders()
        {
            throw new NotImplementedException();
        }

        public void RegisterTestBedBuilderExtension(IDatasetBuilder testBedBuilder)
        {
            DatasetBuilderContainer.Register(testBedBuilder);
        }

        public void UnregisterTestBedBuilderExtension(IDatasetBuilder testBedBuilder)
        {
            DatasetBuilderContainer.Unregister(testBedBuilder);
        }

	    private IList<IDatasetBuilder> determineExecutionOrder(IExtendableContainer<IDatasetBuilder> datasetBuilderContainer)
	    {
		    IList<IDatasetBuilder> sortedBuilders = new List<IDatasetBuilder>();
		    CHashSet<Object> processedBuilders = new CHashSet<Object>();

		    IDatasetBuilder[] datasetBuilders = datasetBuilderContainer.GetExtensions();
            bool dependencyFound;
		    while (processedBuilders.Count() < datasetBuilders.Count())
		    {
                dependencyFound = false;
			    foreach (IDatasetBuilder datasetBuilder in datasetBuilders)
			    {
				    if (!processedBuilders.Contains(datasetBuilder.GetType())
						    && (datasetBuilder.getDependsOn() == null || processedBuilders.ContainsAll(datasetBuilder.getDependsOn())))
				    {
                        processedBuilders.Add(datasetBuilder.GetType());
                        sortedBuilders.Add(datasetBuilder);
                        dependencyFound = true;
                        break;
				    }
			    }
                if (!dependencyFound)
                {
                    log.Error("All Dataset Builders: " + datasetBuilders);
                    log.Error("Dataset Builders: " + processedBuilders);
                    throw new SystemException("Unable to fullfil DatasetBuilder dependencies!");
                }
		    }

		    return sortedBuilders;

	    }
    }
}
