using System;
using System.ServiceModel;
using De.Osthus.Ambeth.Event.Transfer;

namespace De.Osthus.Ambeth.Service
{
    [ServiceContract(Name = "IEventService", Namespace = "http://schemas.osthus.de/Ambeth")]
    [ServiceKnownType("RegisterKnownTypes", typeof(EventServiceModelProvider))]
    public interface IEventServiceWCF
    {
        [OperationContract]
        EventItem[] PollEvents(long serverSession, long eventSequenceSince, TimeSpan requestedMaximumWaitTime);

        [OperationContract]
        long GetCurrentEventSequence();

        [OperationContract]
        long GetCurrentServerSession();

        [OperationContract]
        long FindEventSequenceNumber(long time);
    }

    [ServiceContract(Name = "IEventService", Namespace = "http://schemas.osthus.de/Ambeth")]
    [ServiceKnownType("RegisterKnownTypes", typeof(EventServiceModelProvider))]
    public interface IEventClient : ICommunicationObject
    {
        [OperationContract(AsyncPattern = true)]
        IAsyncResult BeginPollEvents(long serverSession, long eventSequenceSince, TimeSpan requestedMaximumWaitTime, AsyncCallback callback, object asyncState);
        EventItem[] EndPollEvents(IAsyncResult result);

        [OperationContract(AsyncPattern = true)]
        IAsyncResult BeginGetCurrentEventSequence(AsyncCallback callback, object asyncState);
        long EndGetCurrentEventSequence(IAsyncResult result);

        [OperationContract(AsyncPattern = true)]
        IAsyncResult BeginGetCurrentServerSession(AsyncCallback callback, object asyncState);
        long EndGetCurrentServerSession(IAsyncResult result);

        [OperationContract(AsyncPattern = true)]
        IAsyncResult BeginFindEventSequenceNumber(AsyncCallback callback, object asyncState);
        long EndFindEventSequenceNumber(IAsyncResult result);
    }
}
