﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace De.Osthus.Ambeth.ambeth.util.setup
{
    public interface IDataSetup
    {
        IList<Object> ExecuteDatasetBuilders();
    }
}
