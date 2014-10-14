using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace De.Osthus.Ambeth.Util.Setup
{
    public interface IDatasetBuilder
    {
        IList<Object> BuildDataset();
    	IList<IDatasetBuilder> GetDependsOn();
    }
}
