using System;
using De.Osthus.Ambeth.Ioc.Config;

namespace De.Osthus.Ambeth.Ioc.Link
{
    /// <summary>
    /// Last step of the fluent interface for a link configuration at runtime.
    /// </summary>
    public interface ILinkRuntimeFinish
    {
        /// <summary>
        /// Finishes the link configuration and executes it.
        /// </summary>
        void FinishLink();
    }
}