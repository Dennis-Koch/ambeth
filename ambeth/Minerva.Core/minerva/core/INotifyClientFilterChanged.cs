using System.Collections.Generic;
using System.ComponentModel;
using System;

namespace De.Osthus.Minerva.Core
{
    public interface INotifyClientFilterChanged
    {
        event EventHandler ClientFilterChanged;
    }
}
