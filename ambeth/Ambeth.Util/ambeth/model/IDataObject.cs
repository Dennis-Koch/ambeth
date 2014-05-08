using System;
using System.Net;

namespace De.Osthus.Ambeth.Model
{
    public interface IDataObject
    {
        bool ToBeDeleted { get; set; }

        bool ToBeUpdated { get; set; }

        bool ToBeCreated { get; }

        bool HasPendingChanges { get; }
    }
}