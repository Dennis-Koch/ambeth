using System.Windows.Controls;
using System.Windows.Input;

namespace De.Osthus.Minerva.Command
{
    public interface ICommandBindingHelper
    {
        void AddCommandBinding(UserControl control, ICommand command);

        void CreateCommandBindings(UserControl control);
    }
}
