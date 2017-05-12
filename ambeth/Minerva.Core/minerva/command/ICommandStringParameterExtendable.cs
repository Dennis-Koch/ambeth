using System;
using System.Windows.Input;

namespace De.Osthus.Minerva.Command
{
    public interface ICommandStringParameterExtendable
    {
        void RegisterCommandWithStringParameter(ICommand command, String parameterString, int priority);

        void UnregisterCommandWithStringParameter(ICommand command, String parameterString, int priority);

        void RegisterCommandWithStringParameter(ICommand command, String parameterString);

        void UnregisterCommandWithStringParameter(ICommand command, String parameterString);
    }
}
