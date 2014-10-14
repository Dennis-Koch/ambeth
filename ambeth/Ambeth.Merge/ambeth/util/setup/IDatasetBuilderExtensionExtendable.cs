using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace De.Osthus.Ambeth.Util.Setup
{
    public interface IDatasetBuilderExtensionExtendable
    {
        void RegisterTestBedBuilderExtension(IDatasetBuilder testBedBuilder);

        void UnregisterTestBedBuilderExtension(IDatasetBuilder testBedBuilder);
    }
}
