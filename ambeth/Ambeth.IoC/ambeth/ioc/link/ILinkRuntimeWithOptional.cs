using System;
using De.Osthus.Ambeth.Ioc.Config;
namespace De.Osthus.Ambeth.Ioc.Link
{
    /// <summary>
    /// Interface for a finished but configurable link configuration.
    /// </summary>
    public interface ILinkRuntimeWithOptional : ILinkRuntimeOptional, ILinkRuntimeFinish
    {
        /// <summary>
        /// Adds key(s) to the configuration the bean will be registered for.
        /// </summary>
        /// <param name="arguments">Key(s) for the bean.</param>
        /// <returns>This configuration.</returns>
        ILinkRuntimeOptional With(params Object[] arguments);
    }
}