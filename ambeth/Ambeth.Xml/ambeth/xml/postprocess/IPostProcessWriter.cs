using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Collections;

namespace De.Osthus.Ambeth.Xml.PostProcess
{
    public interface IPostProcessWriter : IWriter
    {
        IISet<Object> SubstitutedEntities { get; }

        IDictionary<Object, int> MutableToIdMap { get; }

        IDictionary<Object, int> ImmutableToIdMap { get; }
    }
}
