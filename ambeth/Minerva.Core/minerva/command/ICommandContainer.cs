using System;
using System.Windows.Input;

namespace De.Osthus.Minerva.Command
{
    public interface ICommandContainer
    {
        event EventHandler CanExecuteChanged;

        ICommand Command { get; set; }

        Object CommandParameter { get; set; }

        int Priority { get; set; }
    }
}
