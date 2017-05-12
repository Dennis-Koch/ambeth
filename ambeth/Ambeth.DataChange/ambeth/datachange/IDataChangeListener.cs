using System;
using System.Net;
using De.Osthus.Ambeth.Datachange.Model;

namespace De.Osthus.Ambeth.Datachange
{
    public interface IDataChangeListener
    {
        void DataChanged(IDataChange dataChange, DateTime dispatchTime, long sequenceId);
    }
}
