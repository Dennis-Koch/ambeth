using System;

/// <summary>
/// Interface for a finished but configurable link configuration.
/// </summary>
namespace De.Osthus.Ambeth.Ioc.Link
{
    /// <summary>
    /// Sets the linking as optional. It may be omitted if the registry cannot be found.
    /// </summary>
    public interface ILinkConfigOptional
    {
        void Optional();
    }
}