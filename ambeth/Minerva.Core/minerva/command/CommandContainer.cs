using System;
using System.Windows.Input;

namespace De.Osthus.Minerva.Command
{
    public class CommandContainer : ICommandContainer, IDisposable
    {
        public virtual event EventHandler CanExecuteChanged;

        protected ICommand command;
        public virtual ICommand Command
        { 
            get
            {
                return command;
            }
            set
            {
                if (Object.ReferenceEquals(command, value))
                {
                    return;
                }
                if (command != null)
                {
                    command.CanExecuteChanged -= OnCanExecuteChanged;
                }
                command = value;
                command.CanExecuteChanged += OnCanExecuteChanged;
                OnCanExecuteChanged(this, EventArgs.Empty);
            }
        }

        public virtual Object CommandParameter { get; set; }

        public virtual int Priority { get; set; }

        public void OnCanExecuteChanged(Object sender, EventArgs e)
        {
            if (CanExecuteChanged != null)
            {
                CanExecuteChanged.Invoke(this, EventArgs.Empty);
            }
        }

        public void Dispose()
        {
            if (command != null)
            {
                command.CanExecuteChanged -= OnCanExecuteChanged;
            }
        }
    }
}
