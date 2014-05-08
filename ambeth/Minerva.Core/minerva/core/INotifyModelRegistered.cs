using System.ComponentModel;

namespace De.Osthus.Minerva.Core
{
    public interface INotifyModelRegistered
    {
        event PropertyChangedEventHandler ModelRegistered;
    }
}
