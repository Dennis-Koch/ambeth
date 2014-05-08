using System.ComponentModel;
using De.Osthus.Ambeth.Threading;

namespace De.Osthus.Minerva.Busy
{
    public interface IBusyController : INotifyPropertyChanged
    {
        bool IsBusy { get; }

        int BusyCount { get; }

        void ExecuteBusy(IBackgroundWorkerDelegate busyDelegate);

        R ExecuteBusy<R>(IResultingBackgroundWorkerDelegate<R> busyDelegate);

        IBusyToken AcquireBusyToken();
    }
}
