using System;
using De.Osthus.Ambeth.Ioc.Config;

namespace De.Osthus.Ambeth.Ioc.Link
{
    /// <summary>
    /// Last step of the fluent interface for a link configuration at runtime.
    /// </summary>
    public interface ILinkRuntimeOptional
    {
        /// <summary>
        /// Sets the linking as optional. It may be omitted if the registry cannot be found.
        /// </summary>
        /// <returns>This configuration.</returns>
        ILinkRuntimeFinish Optional();
    }
}