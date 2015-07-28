using System;

namespace De.Osthus.Ambeth.Ioc.Link
{
    /// <summary>
    /// Interface for a finished but configurable link configuration.
    /// </summary>
    public interface ILinkConfigWithOptional : ILinkConfigOptional
    {
        /// <summary>
        /// Adds key(s) to the configuration the bean will be registered for.
        /// </summary>
        /// <param name="arguments">Key(s) for the bean.</param>
        /// <returns>This configuration.</returns>
        ILinkConfigOptional With(params Object[] arguments);
    }
}