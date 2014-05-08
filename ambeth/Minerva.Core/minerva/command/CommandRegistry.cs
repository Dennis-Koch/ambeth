using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Windows.Input;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Extendable;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Threading;
using De.Osthus.Ambeth.Util;
using De.Osthus.Minerva.Core.Config;
using De.Osthus.Ambeth.Collections;
#if SILVERLIGHT
using Telerik.Windows.Controls;
#endif

namespace De.Osthus.Minerva.Command
{
    public class CommandRegistry : CommandBean<Object>, ICommandExtendable, ICommandStringParameterExtendable, IInitializingBean, IDisposableBean, ICanExecuteStateProvider, IAsyncCommand, INotifyPropertyChanged
    {
        [LogInstance]
		public ILogger Log { private get; set; }

        public event PropertyChangedEventHandler PropertyChanged;

        private int nAsyncCommandsRegistered = 0;

        public class CommandKey : IComparable<CommandKey>
        {
            protected Object command;
            
            protected String parameterBean;

            protected int priority;

            public CommandKey(Object command, String parameterBean, int priority)
            {
                this.command = command;
                this.parameterBean = parameterBean;
                this.priority = priority;
            }

            public override int GetHashCode()
            {
                return command.GetHashCode() ^ parameterBean.GetHashCode();
            }

            public override bool Equals(object obj)
            {
                if (Object.ReferenceEquals(this, obj))
                {
                    return true;
                }
                if (!(obj is CommandKey))
                {
                    return false;
                }
                CommandKey other = (CommandKey)obj;
                return Object.ReferenceEquals(command, other.command)
                    && Object.Equals(parameterBean, other.parameterBean)
                    && Object.Equals(priority, other.priority);
            }

            public int CompareTo(CommandKey other)
            {
                int otherPriority = other.priority;
                if (priority == 0)
                {
                    if (otherPriority == 0)
                    {
                        // No ordering specified. Maintain current ordering
                        return 0;
                    }
                    // Other is always first if it has an ordering
                    return 1;
                }
                if (otherPriority == 0)
                {
                    // This is always first if it has an ordering
                    return -1;
                }
                return priority > otherPriority ? 1 : priority < otherPriority ? -1 : 0;
            }
        }

        protected IMapExtendableContainer<CommandKey, ICommandContainer> keyToCommandContainer = new MapExtendableContainer<CommandKey, ICommandContainer>("commandContainer", "commandKey");

        protected Object busyLock = new Object();
        protected int busyCount = 0;
        protected bool isBusy = false;
        public virtual bool IsBusy
        {
            get
            {
                return isBusy;
            }
            private set
            {
                if (Object.Equals(value, isBusy))
                {
                    return;
                }
                isBusy = value;
                RaisePropertyChanged("IsBusy");
            }
        }

        public virtual bool IsAsync
        {
            get
            {
                return (nAsyncCommandsRegistered > 0);
            }
        }

        public virtual bool AlwaysExecutable { get; set; }

        [Property(MinervaCoreConfigurationConstants.AllowConcurrentCommands, DefaultValue="false")]
        public virtual bool AllowConcurrentExecution { get; set; }

        public virtual IServiceContext BeanContext { protected get; set; }

        public virtual IGuiThreadHelper GuiThreadHelper { protected get; set; }

        protected bool? canExecuteChangedState;

        protected ISet<long> acquiredIdsSet = new HashSet<long>();

        protected readonly Random random = new Random();

        public CommandRegistry()
        {
            AlwaysExecutable = false;
            canExecuteChangedState = null;
        }

        public override void AfterPropertiesSet()
        {
            base.AfterPropertiesSet();
            ParamChecker.AssertNotNull(BeanContext, "BeanContext");
            ParamChecker.AssertNotNull(GuiThreadHelper, "GuiThreadHelper");
        }

        public override void Destroy()
        {
            if (keyToCommandContainer.GetExtensions().Count > 0)
            {
                throw new Exception("IOC may be buggy?");
            }
            base.Destroy();
        }

        protected virtual void IncreaseBusyCount()
        {
            lock (busyLock)
            {
                ++busyCount;
                GuiThreadHelper.InvokeInGui(delegate()
                {
                    IsBusy = true;
                });
            }
        }

        protected virtual void DecreaseBusyCount()
        {
            lock (busyLock)
            {
                --busyCount;
                if (busyCount < 1)
                {
                    busyCount = 0;
                    GuiThreadHelper.InvokeInGui(delegate()
                    {
                        IsBusy = false;
                    });
                }
            }
        }

        public virtual IList<ICommandContainer> GetOrderedCommandContainers()
        {
            ILinkedMap<CommandKey, ICommandContainer> containers = keyToCommandContainer.GetExtensions();
            List<CommandKey> orderedKeys = new List<CommandKey>(containers.KeySet());
            orderedKeys.Sort();

            IList<ICommandContainer> containerList = new List<ICommandContainer>();
            foreach (CommandKey commandKey in orderedKeys)
            {
                containerList.Add(containers.Get(commandKey));
            }
            // Now the result must be ordered corresponding to the command priorities:
            //IOrderedEnumerable<ICommandContainer> result = containerList.OrderBy(commandContainer => (commandContainer as ICommandContainer).Priority);
            return containerList;
        }

        public virtual void RegisterCommandIntern(ICommand command, Object parameter, int priority, String parameterKey)
        {
            ICommandContainer commandContainer = new CommandContainer();
            commandContainer.Command = command;
            commandContainer.CommandParameter = parameter;
            commandContainer.Priority = priority;
            CommandKey commandKey = new CommandKey(command, parameterKey, priority);
            keyToCommandContainer.Register(commandContainer, commandKey);
            if ((command is IAsyncCommand) || ((command is CommandRegistry) && ((CommandRegistry)command).IsAsync))
            {
                ++nAsyncCommandsRegistered;
            }
            if (!AlwaysExecutable)
            {
                commandContainer.CanExecuteChanged += OnCanExecuteChanged;
                OnCanExecuteChanged(commandContainer, EventArgs.Empty);
            }
        }

        public virtual void RegisterCommandWithStringParameter(ICommand command, String parameterString, int priority)
        {
            RegisterCommandIntern(command, parameterString, priority, parameterString);
        }

        public virtual void RegisterCommandWithStringParameter(ICommand command, String parameterString)
        {
            RegisterCommandIntern(command, parameterString, 0, parameterString);
        }

        public virtual void RegisterCommand(ICommand command, String parameterBean, int priority)
        {
            if (String.IsNullOrEmpty(parameterBean))
            {
                throw new Exception("Empty beanname is not allowed. Use ICommandStringParameterExtendable instead.");
            }
            Object parameter = BeanContext.GetService(parameterBean);
            RegisterCommandIntern(command, parameter, priority, parameterBean);
        }

        public virtual void RegisterCommand(ICommand command, String parameterBean)
        {
            RegisterCommand(command, parameterBean, 0);
        }

        public virtual void UnregisterCommand(ICommand command, String parameterKey, int priority)
        {
            CommandKey commandKey = new CommandKey(command, parameterKey, priority);
            ICommandContainer commandContainer = keyToCommandContainer.GetExtension(commandKey);
            if (!AlwaysExecutable)
            {
                commandContainer.CanExecuteChanged -= OnCanExecuteChanged;
            }
            if ((command is IAsyncCommand) || ((command is CommandRegistry) && ((CommandRegistry)command).IsAsync))
            {
                --nAsyncCommandsRegistered;
            }
            keyToCommandContainer.Unregister(commandContainer, commandKey);
        }

        public virtual void UnregisterCommand(ICommand command, String parameterBean)
        {
            UnregisterCommand(command, parameterBean, 0);
        }

        public virtual void UnregisterCommandWithStringParameter(ICommand command, String parameterString, int priority)
        {
            UnregisterCommand(command, parameterString, priority);
        }

        public virtual void UnregisterCommandWithStringParameter(ICommand command, String parameterString)
        {
            UnregisterCommand(command, parameterString, 0);
        }

        public virtual bool CanExecute()
        {
            return CanExecuteIntern(null);
        }

        protected override bool CanExecuteIntern(Object obj)
        {
            if (AlwaysExecutable || DetermineCanExecuteChangedState(null))
            {
                return true;
            }
            return false;
        }

        protected virtual bool DetermineCanExecuteChangedState(Object sender)
        {
            lock (busyLock)
            {
                // If we do not allow concurrent execution and the registry is already busy, we must return false:
                if (busyCount > 0 && !AllowConcurrentExecution)
                {
                    return false;
                }
            }

            bool canExecuteChangedState = true;
            foreach (Entry<CommandKey, ICommandContainer> entry in keyToCommandContainer.GetExtensions())            
            {
                ICommandContainer container = entry.Value;
                if (Object.ReferenceEquals(container.Command, sender))
                {
                    return true;
                }
                if (!container.Command.CanExecute(container.CommandParameter))
                {
                    canExecuteChangedState = false;
                    return false;
                }
                return true;
            }
            return canExecuteChangedState;
        }

        public virtual void OnCanExecuteChanged(Object sender, EventArgs e)
        {
            // CanExecuteChanged may be expensive methods for some registered commands
            // => avoid unnecessary evaluations
            if (canExecuteChangedState == null)
            {
                // State was never set => set it for the first time
                canExecuteChangedState = DetermineCanExecuteChangedState(null);
                SendEvent();
                return;
            }
            ICommandContainer sendingContainer = (ICommandContainer)sender;
            bool senderState = sendingContainer.Command.CanExecute(sendingContainer.CommandParameter);
            if (senderState == canExecuteChangedState)
            {
                // Senders state equals total state => no need to evaluate other states and no need to fire event
                return;
            }
            if (canExecuteChangedState == true)
            {
                // total state is true and senders state is false => set total to false and fire event
                canExecuteChangedState = false;
                SendEvent();
                return;
            }
            // total state is false, but senders state has turned to true => check whether there is any
            // other command with state false => if not, change total state to true and fire event
            canExecuteChangedState = DetermineCanExecuteChangedState(sender);
            if (canExecuteChangedState == true)
            {
                SendEvent();
            }
        }

        public virtual void SendEvent()
        {
            // Send event for listeners, which can then evaluate CanExecute
            //if (CanExecuteChanged != null)
            //{
            //    CanExecuteChanged.Invoke(this, EventArgs.Empty);
            //}
            CommandManager.InvalidateRequerySuggested();
        }

        public virtual void Execute()
        {
            Execute(null, null);
        }

        protected override void ExecuteIntern(Object obj)
        {
            Execute();
        }

        // This execute is used, if the command itself is a CommandRegistry that has IAsyncCommands
        public virtual void Execute(Object parameter, INextCommandDelegate commandFinishedCallback)
        {
            lock (busyLock)
            {
                if (busyCount > 0)
                {
                    if (!AllowConcurrentExecution)
                    {
                        // The registry is already busy and concurrent execution is not allowed -> we are finished here:
                        if (commandFinishedCallback != null)
                        {
                            commandFinishedCallback.Invoke(false);
                        }
                        return;
                    }
                }
                else
                {
                    IncreaseBusyCount();
                    SendEvent();
                }
            }

            IList<ICommandContainer> commandContainers = GetOrderedCommandContainers();

            CommandRegistryFinishedCallback registryFinishedCallback = new CommandRegistryFinishedCallback(delegate(bool success)
            {
                DecreaseBusyCount();
                SendEvent();
                if (commandFinishedCallback != null)
                {
                    commandFinishedCallback.Invoke(success);
                }
            });
            Execute(commandContainers, false, registryFinishedCallback);
        }

        /// <summary>
        /// Executes a CommandChain from the beginning.
        /// </summary>
        /// <param name="commandChain"></param>
        /// <param name="onlyIFinallyCommands">indicates errors, if true only finally commands are executed. If an finally command throws an exceptions, remaining finally commands are still executed.</param>
        protected virtual void Execute(IList<ICommandContainer> commandChain, bool onlyIFinallyCommands, CommandRegistryFinishedCallback registryFinishedCallback = null)
        {
            ICommandContainer commandContainer = commandChain[0];
            if (onlyIFinallyCommands)
            {
                if (!(commandContainer.Command is IFinallyCommand))
                {
                    ExecuteRecursion(commandChain, onlyIFinallyCommands, registryFinishedCallback);
                    return;
                }
            }
            ICommand command = commandContainer.Command;
            Object commandParameter = commandContainer.CommandParameter;
            if ((command is IAsyncCommand) || ((command is CommandRegistry) && ((CommandRegistry)command).IsAsync))
            {
                // The command works asynchronously, thus we must wait until all operations initiated by the command
                // are finished, before we can go on with the next command.
                // Therefore, we acquire a unique Id to identify the async command and we associate the index of the
                // following command with this index.
                IAsyncCommand asyncCommand = (IAsyncCommand)command;

                long processSequenceId = AcquireCommand();

                // The async command is called with the NextCommand delegate:
                asyncCommand.Execute(commandParameter, delegate(bool success)
                {
                    //commandChain, processSequenceId and onlyIFinallyCommands are stored (closure).
                    AsyncExecuteRecursion(commandChain, processSequenceId, success, onlyIFinallyCommands, registryFinishedCallback);
                });
                return;
            }

            // The command works synchronously, thus we can directly proceed with the next command:
            try
            {
                command.Execute(commandParameter);
            }
            catch (Exception ex)
            {
                Log.Warn("Execution of the command '" + command.GetType().Name + "' with parameter '" + commandParameter.GetType().Name + "' lead to the following exception: " + ex);
                onlyIFinallyCommands = true;
            }
            ExecuteRecursion(commandChain, onlyIFinallyCommands, registryFinishedCallback);
        }

        protected virtual void ExecuteRecursion(IList<ICommandContainer> commandChain, bool onlyIFinallyCommands, CommandRegistryFinishedCallback registryFinishedCallback)
        {
            IList<ICommandContainer> commandChainCpy = new List<ICommandContainer>(commandChain);
            commandChainCpy.RemoveAt(0);
            if (commandChainCpy.Count > 0)
            {
                Execute(commandChainCpy, onlyIFinallyCommands, registryFinishedCallback);
            }
            else
            {
                // Excecution of the command chain is finished
                if (registryFinishedCallback != null)
                {
                    // This callback is used, if the registry itself is used as an IAsyncCommand within another command registry
                    // New: It is generally used to call DecreasyBusyCount()
                    registryFinishedCallback.Invoke(!onlyIFinallyCommands);
                }
            }
        }

        protected virtual void AsyncExecuteRecursion(IList<ICommandContainer> commandChain, long processSequenceId, bool success, bool onlyIFinallyCommands, CommandRegistryFinishedCallback registryFinishedCallback)
        {
            FreeCommand(processSequenceId);
            if (!success)
            {
                Log.Warn("Execution of the async command with Id " + processSequenceId + " was not successful (" + commandChain[0] + ")");
                onlyIFinallyCommands = true;
            }
            // With asychronous execution we have left the GUI thread, so we have to return
            // with the next command:
            GuiThreadHelper.InvokeInGui(delegate()
            {
                ExecuteRecursion(commandChain, onlyIFinallyCommands, registryFinishedCallback);
            });
        }

        protected virtual long AcquireCommand()
        {
            long processSequenceId;
            lock (acquiredIdsSet)
            {
                while (true)
                {
                    //The ProcessId identifies the concrete execution of a command.
                    processSequenceId = (long)(random.NextDouble() * long.MaxValue - 1) + 1;
                    if (!acquiredIdsSet.Contains(processSequenceId))
                    {
                        break;
                    }
                }
                acquiredIdsSet.Add(processSequenceId);
            }
            return processSequenceId;
        }

        protected virtual void FreeCommand(long processSequenceId)
        {
            //TODO: please document: why acquire? What if an AsyncCommand calls an AsyncCommand?
            lock (acquiredIdsSet)
            {
                if (acquiredIdsSet.Contains(processSequenceId))
                {
                    acquiredIdsSet.Remove(processSequenceId);
                }
            }
        }

        protected virtual void RaisePropertyChanged(String propertyName)
        {
            var localEventHandler = PropertyChanged;
            if (localEventHandler != null)
            {
                localEventHandler.Invoke(this, new PropertyChangedEventArgs(propertyName));
            }
        }
    }
}
