using System.Collections.Generic;
using System.Collections;
using System;
using De.Osthus.Ambeth.Event;
using De.Osthus.Ambeth.Datachange.Transfer;
using De.Osthus.Ambeth.Datachange.Model;

namespace De.Osthus.Ambeth.Datachange
{
    public class TypeFilteredDataChangeListener : UnfilteredDataChangeListener
    {
        public static IDataChangeListener Create(IDataChangeListener dataChangeListener, params Type[] interestedTypes)
        {
            TypeFilteredDataChangeListener dcListener = new TypeFilteredDataChangeListener();
            dcListener.DataChangeListener = dataChangeListener;
            dcListener.InterestedTypes = interestedTypes;
            return dcListener;
        }

        public static IEventListener CreateEventListener(IDataChangeListener dataChangeListener, params Type[] interestedTypes)
        {
            TypeFilteredDataChangeListener dcListener = new TypeFilteredDataChangeListener();
            dcListener.DataChangeListener = dataChangeListener;
            dcListener.InterestedTypes = interestedTypes;
            return dcListener;
        }

        public virtual Type[] InterestedTypes { get; set; }

        public override void DataChanged(IDataChange dataChange, DateTime dispatchTime, long sequenceId)
        {
            if (dataChange.IsEmpty)
            {
                return;
            }
            dataChange = dataChange.Derive(InterestedTypes);
            if (dataChange.IsEmpty)
            {
                return;
            }
            DataChangeListener.DataChanged(dataChange, dispatchTime, sequenceId);
        }
    }
}
