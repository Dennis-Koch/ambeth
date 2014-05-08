using System.Collections.Generic;
using System.Collections;
using De.Osthus.Ambeth.Event;
using System;
using De.Osthus.Ambeth.Datachange.Transfer;
using De.Osthus.Ambeth.Datachange.Model;

namespace De.Osthus.Ambeth.Datachange
{
    public class IdFilteredDataChangeListener : UnfilteredDataChangeListener
    {
        public static IDataChangeListener Create(IDataChangeListener dataChangeListener, params Object[] interestedIds)
        {
            IdFilteredDataChangeListener dcListener = new IdFilteredDataChangeListener();
            dcListener.DataChangeListener = dataChangeListener;
            dcListener.InterestedIds = interestedIds;
            return dcListener;
        }

        public static IEventListener CreateEventListener(IDataChangeListener dataChangeListener, params Object[] interestedIds)
        {
            IdFilteredDataChangeListener dcListener = new IdFilteredDataChangeListener();
            dcListener.DataChangeListener = dataChangeListener;
            dcListener.InterestedIds = interestedIds;
            return dcListener;
        }

        public virtual Object[] InterestedIds { get; set; }
        
        public override void DataChanged(IDataChange dataChange, DateTime dispatchTime, long sequenceId)
        {
            if (dataChange.IsEmpty)
            {
                return;
            }
            dataChange = dataChange.Derive(InterestedIds);
            if (dataChange.IsEmpty)
            {
                return;
            }
            DataChangeListener.DataChanged(dataChange, dispatchTime, sequenceId);
        }
    }
}
