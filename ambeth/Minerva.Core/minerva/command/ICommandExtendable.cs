using System;
using System.Windows.Input;

namespace De.Osthus.Minerva.Command
{
    public interface ICommandExtendable
    {
        void RegisterCommand(ICommand command, String parameterBean, int priority);

        void UnregisterCommand(ICommand command, String parameterBean, int priority);

        void RegisterCommand(ICommand command, String parameterBean);

        void UnregisterCommand(ICommand command, String parameterBean);
    }
}
