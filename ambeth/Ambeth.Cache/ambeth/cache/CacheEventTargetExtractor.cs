using System;
using De.Osthus.Ambeth.Event;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Log;
namespace De.Osthus.Ambeth.Cache
{
public class CacheEventTargetExtractor : IEventTargetExtractor, IInitializingBean
{
    [LogInstance]
	public ILogger Log { private get; set; }

	public virtual void AfterPropertiesSet()
	{
		// Intended blank
	}

	public Object ExtractEventTarget(Object eventTarget)
	{
		Type targetType = eventTarget.GetType();
		if (typeof(ChildCache).Equals(targetType) || typeof(RootCache).Equals(targetType))
		{
			return eventTarget;
		}
		if (typeof(ICacheProvider).IsAssignableFrom(targetType))
		{
			ICacheProvider cacheProvider = (ICacheProvider) eventTarget;
			if (cacheProvider.IsNewInstanceOnCall)
			{
				return null;
			}
			return cacheProvider.GetCurrentCache();
		}
		return eventTarget;
	}
}
}
