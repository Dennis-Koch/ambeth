using System;

namespace De.Osthus.Ambeth.Orm
{
    public interface ILinkConfig
    {
        String Source { get; }

        String Alias { get; }

        CascadeDeleteDirection CascadeDeleteDirection { get; }
    }

    public enum CascadeDeleteDirection
    {
        LEFT, RIGHT, BOTH, NONE
    }

    public enum EntityIdentifier
    {
        LEFT, RIGHT
    }
}
