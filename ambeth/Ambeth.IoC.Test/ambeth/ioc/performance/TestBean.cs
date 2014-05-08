using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Util;
using System;

namespace De.Osthus.Ambeth.Ioc.Performance
{
    public class TestBean : IInitializingBean
    {
        [LogInstance]
	    public ILogger Log { private get; set; }

        public String Value { protected get; set; }

	    public void AfterPropertiesSet()
	    {
		    ParamChecker.AssertNotNull(Value, "value");
	    }
    }
}