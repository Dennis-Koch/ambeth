using System.Linq;
#if WPF
using System.Windows.Input;
using System.Windows.Controls;
using Telerik.Windows.Controls;
using CommandBinding = Telerik.Windows.Controls.CommandBinding;
using CommandBindingCollection = Telerik.Windows.Controls.CommandBindingCollection;
using CommandManager = Telerik.Windows.Controls.CommandManager;
using ExecutedRoutedEventArgs = Telerik.Windows.Controls.ExecutedRoutedEventArgs;
using CanExecuteRoutedEventArgs = Telerik.Windows.Controls.CanExecuteRoutedEventArgs;
#else
#if SILVERLIGHT
using System.Windows.Input;
using System.Windows.Controls;
using Telerik.Windows.Controls;
#else
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
#endif
#endif
using De.Osthus.Ambeth.Ioc;

namespace De.Osthus.Minerva.Command
{
    public class CommandBindingHelper : ICommandBindingHelper, IInitializingBean, IDisposableBean
    {
        public virtual void AfterPropertiesSet()
        {
            // Intended blank
        }

        public virtual void Destroy()
        {
            // Intended blank
        }

        public void AddCommandBinding(UserControl control, ICommand command)
        {
            CommandBindingCollection bindings = CommandManager.GetCommandBindings(control);
            if (bindings == null)
            {
                bindings = new CommandBindingCollection();
                CommandManager.SetCommandBindings(control, bindings);
            }
            if (bindings.Where(p => p.Command == command).FirstOrDefault() == null)
            {
                bindings.Add(new CommandBinding(command, CommandExecuted, CommandCanExecute));
            }
        }

        public void CreateCommandBindings(UserControl control)
        {
            foreach (ICommand command in control.Resources.Values.OfType<ICommand>())
            {
                AddCommandBinding(control, command);
            }
            //TODO: generates exception, ask CO
            //foreach (ICommandSource commandSource in control.ChildrenOfType<UIElement>().OfType<ICommandSource>().Where(p => p.Command != null))
            //{
            //    AddCommandBinding(control, commandSource.Command);
            //}
        }

        #region event handlers

        private void CommandExecuted(object sender, ExecutedRoutedEventArgs args)
        {
            args.Command.Execute(args.Parameter);
            //reevaluate CanExecute in order to enable/disable commands
            CommandManager.InvalidateRequerySuggested();
        }

        private void CommandCanExecute(object sender, CanExecuteRoutedEventArgs args)
        {
            args.CanExecute = args.Command.CanExecute(args.Parameter);
        }

        #endregion
    }
}
