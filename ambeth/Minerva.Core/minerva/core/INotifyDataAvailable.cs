using System;

namespace De.Osthus.Minerva.Core
{
    public interface INotifyDataAvailable
    {
        event EventHandler DataAvailable;
    }
}
