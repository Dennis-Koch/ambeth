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

namespace De.Osthus.Minerva.Command
{
    public class CommandRegistry : ICommand, IAsyncCommand, ICommandExtendable, ICommandStringParameterExtendable, IInitializingBean, IDisposableBean, INotifyPropertyChanged
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        public event PropertyChangedEventHandler PropertyChanged;

        #region CommandKey class
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
        #endregion

        #region Implemented members
        protected int nAsyncCommandsRegistered = 0;

        protected IMapExtendableContainer<CommandKey, ICommandContainer> keyToCommandContainer = new MapExtendableContainer<CommandKey, ICommandContainer>("commandContainer", "commandKey");

        protected Object busyLock = new Object();
        protected int busyCount = 0;
        protected bool queuedCanExecutechanged = false;
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

        protected bool? canExecuteChangedState;

        protected ISet<long> acquiredIdsSet = new HashSet<long>();

        protected readonly Random random = new Random();
        #endregion

        #region Injected members
        public virtual bool AlwaysExecutable { get; set; }

        [Property(MinervaCoreConfigurationConstants.AllowConcurrentCommands, DefaultValue = "false")]
        public virtual bool AllowConcurrentExecution { get; set; }

        public virtual IServiceContext BeanContext { protected get; set; }

        public virtual IGuiThreadHelper GuiThreadHelper { protected get; set; }
        #endregion

        #region Lifecycle
        public CommandRegistry()
        {
            AlwaysExecutable = false;
            canExecuteChangedState = null;
        }

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(BeanContext, "BeanContext");
            ParamChecker.AssertNotNull(GuiThreadHelper, "GuiThreadHelper");
        }

        public virtual void Destroy()
        {
            if (keyToCommandContainer.GetExtensions().Count > 0)
            {
                throw new Exception("IOC may be buggy?");
            }
        }
        #endregion

        #region ICommand
        public event EventHandler CanExecuteChanged;

        public virtual bool CanExecute(Object parameter)
        {
            if (AlwaysExecutable)
            {
                return true;
            }
            DetermineCanExecuteChangedState();
            if (canExecuteChangedState == null || canExecuteChangedState == false)
            {
                return false;
            }
            return true;
        }

        public virtual void Execute(Object parameter)
        {
            Execute(parameter, null);
        }
        #endregion

        #region For backward compatibility
        public virtual bool CanExecute()
        {
            return CanExecute(null);
        }

        public virtual void Execute()
        {
            Execute(null);
        }
        #endregion

        #region IAsyncCommand
        // This execute is used, if the command itself is a CommandRegistry that has IAsyncCommands
        public virtual void Execute(Object parameter, INextCommandDelegate commandFinishedCallback)
        {
            if (!this.CanExecute())
            {
                return;
            }
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
                    GuiThreadHelper.InvokeInGuiAndWait(delegate()
                    {
                        IncreaseBusyCount();
                    });
                }
            }

            IList<ICommandContainer> commandContainers = GetOrderedCommandContainers();

            CommandRegistryFinishedCallback registryFinishedCallback = new CommandRegistryFinishedCallback(delegate(bool success)
            {
                DecreaseBusyCount();
                RaiseCanExecuteChanged();
                if (commandFinishedCallback != null)
                {
                    commandFinishedCallback.Invoke(success);
                }
            });
            if (commandContainers.Count < 1)
            {
                registryFinishedCallback(true);
                return;
            }
            Execute(commandContainers, false, registryFinishedCallback);
        }
        #endregion

        #region Non-public implementation of ICommand / IAsyncCommand
        protected virtual void DetermineCanExecuteChangedState()
        {
            lock (busyLock)
            {
                // If we do not allow concurrent execution and the registry is already busy, we must return false:
                if (busyCount > 0 && !AllowConcurrentExecution)
                {
                    canExecuteChangedState = false;
                    return;
                }
            }

            // We do not need to lock canExecuteChangedState, because CanExecuteChanged event are always handled in the UI thread:
            canExecuteChangedState = true;
            foreach (Entry<CommandKey, ICommandContainer> entry in keyToCommandContainer.GetExtensions())
            {
                ICommandContainer container = entry.Value;
                if (!container.Command.CanExecute(container.CommandParameter))
                {
                    canExecuteChangedState = false;
                    return;
                }
            }
        }

        protected virtual void OnCanExecuteChanged(Object sender, EventArgs e)
        {
            ICommandContainer sendingContainer = (ICommandContainer)sender;
            if (!GuiThreadHelper.IsInGuiThread())
            {
                String error = "The command " + sendingContainer.Command.GetType().FullName + " has fired CanExecuteChanged from a non-UI thread. This is illegal!";
                Log.Error(error);
                throw new Exception(error);
            }
            // CanExecuteChanged may be expensive methods for some registered commands
            // => avoid unnecessary evaluations
            if (canExecuteChangedState == null)
            {
                // State was never set => set it for the first time
                DetermineCanExecuteChangedState();
                RaiseCanExecuteChanged();
                return;
            }
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
                RaiseCanExecuteChanged();
                return;
            }
            // total state is false, but senders state has turned to true => check whether there is any
            // other command with state false => if not, change total state to true and fire event
            DetermineCanExecuteChangedState();
            if (canExecuteChangedState == true)
            {
                RaiseCanExecuteChanged();
            }
        }

        /// <summary>
        /// Executes a CommandChain from the beginning.
        /// </summary>
        /// <param name="commandChain"></param>
        /// <param name="onlyIFinallyCommands">indicates errors, if true only finally commands are executed. If an finally command throws an exceptions, remaining finally commands are still executed.</param>
        protected virtual void Execute(IList<ICommandContainer> commandChain, bool onlyIFinallyCommands, CommandRegistryFinishedCallback registryFinishedCallback = null)
        {
            if (commandChain.Count < 1)
            {
                String error = "It is not allowed to call execute with a command-chain of size zero.";
                Log.Error(error);
                throw new ArgumentException(error);
            }
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
        #endregion

        #region ICommandExtendable
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
        #endregion

        #region ICommandStringParameterExtendable
        public virtual void RegisterCommandWithStringParameter(ICommand command, String parameterString, int priority)
        {
            RegisterCommandIntern(command, parameterString, priority, parameterString);
        }

        public virtual void RegisterCommandWithStringParameter(ICommand command, String parameterString)
        {
            RegisterCommandIntern(command, parameterString, 0, parameterString);
        }

        public virtual void UnregisterCommandWithStringParameter(ICommand command, String parameterString, int priority)
        {
            UnregisterCommand(command, parameterString, priority);
        }

        public virtual void UnregisterCommandWithStringParameter(ICommand command, String parameterString)
        {
            UnregisterCommand(command, parameterString, 0);
        }
        #endregion

        #region Non-public implementation of ICommandExtendable / ICommandStringParameterExtendable
        protected virtual IList<ICommandContainer> GetOrderedCommandContainers()
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

        protected virtual void RegisterCommandIntern(ICommand command, Object parameter, int priority, String parameterKey)
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
        #endregion

        #region Other public methods
        // This method is public to enable using code to trigger CanExecuteChanged directly.
        // However, this should be avoided, hence the usual way should be to go over the
        // CanExecuteChanged of the individual commands!
        public virtual void RaiseCanExecuteChanged()
        {
            lock (busyLock)
            {
                // The old version of the CommandRegistry was derived from CommandBean and thus using the Telerik CommandManager.
                // There was a problem that the CommandManager not always fired CanExecuteChanged.
                // Now, without the CommandManager, we had the following strange behaviour:
                // - We opened a popup by RadWindow.ShowDialog via a CommandRegistry
                // - From this popup we executed another CommandRegistry opening a second popup via CommandRegistry.ShowDialog
                // - The second popup however opens behind the first and is thus disabled.
                // Now I have the theory that Command-bindings should not be updated while Controls are updating their Layout.
                // Therefore, I introduced the following code that queues the CanExecutechanged invocation until the registry
                // is unbusy. As a result our popup are now working as expected. However, we should keep this problem in mind
                // and maybe there will be a similar problem in the future that demands a rethinking of this problem.
                // We should also test whether a newer version of the Telerik controls still shows this strange behaviour.
                // Below, we will keep a commented version of this registry that still derives from CommandBean.
                if (busyCount > 0)
                {
                    queuedCanExecutechanged = true;
                }
                if (queuedCanExecutechanged)
                {
                    return;
                }
            }
            var localEventHandler = CanExecuteChanged;
            if (localEventHandler != null)
            {
                localEventHandler.Invoke(this, EventArgs.Empty);
            }
        }
        #endregion

        #region Other protected methods
        protected virtual void RaisePropertyChanged(String propertyName)
        {
            var localEventHandler = PropertyChanged;
            if (localEventHandler != null)
            {
                localEventHandler.Invoke(this, new PropertyChangedEventArgs(propertyName));
            }
        }

        protected virtual void IncreaseBusyCount()
        {
            // Always called from the UI-thread:
            lock (busyLock)
            {
                RaiseCanExecuteChanged();
                IsBusy = true;
                ++busyCount;
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
                    if (queuedCanExecutechanged)
                    {
                        queuedCanExecutechanged = false;
                        RaiseCanExecuteChanged();
                    }
                }
            }
        }
        #endregion
    }

    #region Old version derived from CommandBean (please read the comments in RaiseCanExecuteChanged() of the above version)
    //public class CommandRegistry : CommandBean<Object>, IAsyncCommand, ICommandExtendable, ICommandStringParameterExtendable, IInitializingBean, IDisposableBean, INotifyPropertyChanged
    //{
    //    [LogInstance]
    //    public ILogger Log { private get; set; }

    //    public event PropertyChangedEventHandler PropertyChanged;

    //    #region CommandKey class
    //    public class CommandKey : IComparable<CommandKey>
    //    {
    //        protected Object command;

    //        protected String parameterBean;

    //        protected int priority;

    //        public CommandKey(Object command, String parameterBean, int priority)
    //        {
    //            this.command = command;
    //            this.parameterBean = parameterBean;
    //            this.priority = priority;
    //        }

    //        public override int GetHashCode()
    //        {
    //            return command.GetHashCode() ^ parameterBean.GetHashCode();
    //        }

    //        public override bool Equals(object obj)
    //        {
    //            if (Object.ReferenceEquals(this, obj))
    //            {
    //                return true;
    //            }
    //            if (!(obj is CommandKey))
    //            {
    //                return false;
    //            }
    //            CommandKey other = (CommandKey)obj;
    //            return Object.ReferenceEquals(command, other.command)
    //                && Object.Equals(parameterBean, other.parameterBean)
    //                && Object.Equals(priority, other.priority);
    //        }

    //        public int CompareTo(CommandKey other)
    //        {
    //            int otherPriority = other.priority;
    //            if (priority == 0)
    //            {
    //                if (otherPriority == 0)
    //                {
    //                    // No ordering specified. Maintain current ordering
    //                    return 0;
    //                }
    //                // Other is always first if it has an ordering
    //                return 1;
    //            }
    //            if (otherPriority == 0)
    //            {
    //                // This is always first if it has an ordering
    //                return -1;
    //            }
    //            return priority > otherPriority ? 1 : priority < otherPriority ? -1 : 0;
    //        }
    //    }
    //    #endregion

    //    #region Implemented members
    //    protected int nAsyncCommandsRegistered = 0;

    //    protected IMapExtendableContainer<CommandKey, ICommandContainer> keyToCommandContainer = new MapExtendableContainer<CommandKey, ICommandContainer>("commandContainer", "commandKey");

    //    protected Object busyLock = new Object();
    //    protected int busyCount = 0;
    //    protected bool isBusy = false;
    //    public virtual bool IsBusy
    //    {
    //        get
    //        {
    //            return isBusy;
    //        }
    //        private set
    //        {
    //            if (Object.Equals(value, isBusy))
    //            {
    //                return;
    //            }
    //            isBusy = value;
    //            RaisePropertyChanged("IsBusy");
    //        }
    //    }

    //    public virtual bool IsAsync
    //    {
    //        get
    //        {
    //            return (nAsyncCommandsRegistered > 0);
    //        }
    //    }

    //    protected bool? canExecuteChangedState;

    //    protected ISet<long> acquiredIdsSet = new HashSet<long>();

    //    protected readonly Random random = new Random();
    //    #endregion

    //    #region Injected members
    //    public virtual bool AlwaysExecutable { get; set; }

    //    [Property(MinervaCoreConfigurationConstants.AllowConcurrentCommands, DefaultValue = "false")]
    //    public virtual bool AllowConcurrentExecution { get; set; }

    //    public virtual IServiceContext BeanContext { protected get; set; }

    //    public virtual IGuiThreadHelper GuiThreadHelper { protected get; set; }
    //    #endregion

    //    #region Lifecycle
    //    public CommandRegistry()
    //    {
    //        AlwaysExecutable = false;
    //        canExecuteChangedState = null;
    //    }

    //    public override void AfterPropertiesSet()
    //    {
    //        base.AfterPropertiesSet();
    //        ParamChecker.AssertNotNull(BeanContext, "BeanContext");
    //        ParamChecker.AssertNotNull(GuiThreadHelper, "GuiThreadHelper");
    //    }

    //    public virtual void Destroy()
    //    {
    //        if (keyToCommandContainer.GetExtensions().Count > 0)
    //        {
    //            throw new Exception("IOC may be buggy?");
    //        }
    //    }
    //    #endregion

    //    #region ICommand
    //    protected override void ExecuteIntern(object obj)
    //    {
    //        Execute(obj, null);
    //    }

    //    protected override bool CanExecuteIntern(Object obj)
    //    {
    //        if (AlwaysExecutable)
    //        {
    //            return true;
    //        }
    //        DetermineCanExecuteChangedState();
    //        if (canExecuteChangedState == null || canExecuteChangedState == false)
    //        {
    //            return false;
    //        }
    //        return true;
    //    }
    //    #endregion

    //    #region For backward compatibility
    //    public virtual bool CanExecute()
    //    {
    //        return CanExecute(null);
    //    }

    //    public virtual void Execute()
    //    {
    //        Execute(null);
    //    }
    //    #endregion

    //    #region IAsyncCommand
    //    // This execute is used, if the command itself is a CommandRegistry that has IAsyncCommands
    //    public virtual void Execute(Object parameter, INextCommandDelegate commandFinishedCallback)
    //    {
    //        lock (busyLock)
    //        {
    //            if (busyCount > 0)
    //            {
    //                if (!AllowConcurrentExecution)
    //                {
    //                    // The registry is already busy and concurrent execution is not allowed -> we are finished here:
    //                    if (commandFinishedCallback != null)
    //                    {
    //                        commandFinishedCallback.Invoke(false);
    //                    }
    //                    return;
    //                }
    //            }
    //            else
    //            {
    //                IncreaseBusyCount();
    //                RaiseCanExecuteChanged();
    //            }
    //        }

    //        IList<ICommandContainer> commandContainers = GetOrderedCommandContainers();

    //        CommandRegistryFinishedCallback registryFinishedCallback = new CommandRegistryFinishedCallback(delegate(bool success)
    //        {
    //            DecreaseBusyCount();
    //            RaiseCanExecuteChanged();
    //            if (commandFinishedCallback != null)
    //            {
    //                commandFinishedCallback.Invoke(success);
    //            }
    //        });
    //        Execute(commandContainers, false, registryFinishedCallback);
    //    }
    //    #endregion

    //    #region Non-public implementation of ICommand / IAsyncCommand
    //    protected virtual void DetermineCanExecuteChangedState()
    //    {
    //        lock (busyLock)
    //        {
    //            // If we do not allow concurrent execution and the registry is already busy, we must return false:
    //            if (busyCount > 0 && !AllowConcurrentExecution)
    //            {
    //                canExecuteChangedState = false;
    //                return;
    //            }
    //        }
    //        IDictionary<CommandKey, ICommandContainer> containers = keyToCommandContainer.GetExtensions();
    //        // We do not need to lock canExecuteChangedState, because CanExecuteChanged event are always handled in the UI thread:
    //        canExecuteChangedState = true;
    //        foreach (var container in containers.Values)
    //        {
    //            if (!container.Command.CanExecute(container.CommandParameter))
    //            {
    //                canExecuteChangedState = false;
    //                return;
    //            }
    //        }
    //    }

    //    protected virtual void OnCanExecuteChanged(Object sender, EventArgs e)
    //    {
    //        ICommandContainer sendingContainer = (ICommandContainer)sender;
    //        if (!GuiThreadHelper.IsInGuiThread())
    //        {
    //            String error = "The command " + sendingContainer.Command.GetType().FullName + " has fired CanExecuteChanged from a non-UI thread. This is illegal!";
    //            Log.Error(error);
    //            throw new Exception(error);
    //        }
    //        // CanExecuteChanged may be expensive methods for some registered commands
    //        // => avoid unnecessary evaluations
    //        if (canExecuteChangedState == null)
    //        {
    //            // State was never set => set it for the first time
    //            DetermineCanExecuteChangedState();
    //            RaiseCanExecuteChanged();
    //            return;
    //        }
    //        bool senderState = sendingContainer.Command.CanExecute(sendingContainer.CommandParameter);
    //        if (senderState == canExecuteChangedState)
    //        {
    //            // Senders state equals total state => no need to evaluate other states and no need to fire event
    //            return;
    //        }
    //        if (canExecuteChangedState == true)
    //        {
    //            // total state is true and senders state is false => set total to false and fire event
    //            canExecuteChangedState = false;
    //            RaiseCanExecuteChanged();
    //            return;
    //        }
    //        // total state is false, but senders state has turned to true => check whether there is any
    //        // other command with state false => if not, change total state to true and fire event
    //        DetermineCanExecuteChangedState();
    //        if (canExecuteChangedState == true)
    //        {
    //            RaiseCanExecuteChanged();
    //        }
    //    }

    //    /// <summary>
    //    /// Executes a CommandChain from the beginning.
    //    /// </summary>
    //    /// <param name="commandChain"></param>
    //    /// <param name="onlyIFinallyCommands">indicates errors, if true only finally commands are executed. If an finally command throws an exceptions, remaining finally commands are still executed.</param>
    //    protected virtual void Execute(IList<ICommandContainer> commandChain, bool onlyIFinallyCommands, CommandRegistryFinishedCallback registryFinishedCallback = null)
    //    {
    //        ICommandContainer commandContainer = commandChain[0];
    //        if (onlyIFinallyCommands)
    //        {
    //            if (!(commandContainer.Command is IFinallyCommand))
    //            {
    //                ExecuteRecursion(commandChain, onlyIFinallyCommands, registryFinishedCallback);
    //                return;
    //            }
    //        }
    //        ICommand command = commandContainer.Command;
    //        Object commandParameter = commandContainer.CommandParameter;
    //        if ((command is IAsyncCommand) || ((command is CommandRegistry) && ((CommandRegistry)command).IsAsync))
    //        {
    //            // The command works asynchronously, thus we must wait until all operations initiated by the command
    //            // are finished, before we can go on with the next command.
    //            // Therefore, we acquire a unique Id to identify the async command and we associate the index of the
    //            // following command with this index.
    //            IAsyncCommand asyncCommand = (IAsyncCommand)command;

    //            long processSequenceId = AcquireCommand();

    //            // The async command is called with the NextCommand delegate:
    //            asyncCommand.Execute(commandParameter, delegate(bool success)
    //            {
    //                //commandChain, processSequenceId and onlyIFinallyCommands are stored (closure).
    //                AsyncExecuteRecursion(commandChain, processSequenceId, success, onlyIFinallyCommands, registryFinishedCallback);
    //            });
    //            return;
    //        }

    //        // The command works synchronously, thus we can directly proceed with the next command:
    //        try
    //        {
    //            command.Execute(commandParameter);
    //        }
    //        catch (Exception ex)
    //        {
    //            Log.Warn("Execution of the command '" + command.GetType().Name + "' with parameter '" + commandParameter.GetType().Name + "' lead to the following exception: " + ex);
    //            onlyIFinallyCommands = true;
    //        }
    //        ExecuteRecursion(commandChain, onlyIFinallyCommands, registryFinishedCallback);
    //    }

    //    protected virtual void ExecuteRecursion(IList<ICommandContainer> commandChain, bool onlyIFinallyCommands, CommandRegistryFinishedCallback registryFinishedCallback)
    //    {
    //        IList<ICommandContainer> commandChainCpy = new List<ICommandContainer>(commandChain);
    //        commandChainCpy.RemoveAt(0);
    //        if (commandChainCpy.Count > 0)
    //        {
    //            Execute(commandChainCpy, onlyIFinallyCommands, registryFinishedCallback);
    //        }
    //        else
    //        {
    //            // Excecution of the command chain is finished
    //            if (registryFinishedCallback != null)
    //            {
    //                // This callback is used, if the registry itself is used as an IAsyncCommand within another command registry
    //                // New: It is generally used to call DecreasyBusyCount()
    //                registryFinishedCallback.Invoke(!onlyIFinallyCommands);
    //            }
    //        }
    //    }

    //    protected virtual void AsyncExecuteRecursion(IList<ICommandContainer> commandChain, long processSequenceId, bool success, bool onlyIFinallyCommands, CommandRegistryFinishedCallback registryFinishedCallback)
    //    {
    //        FreeCommand(processSequenceId);
    //        if (!success)
    //        {
    //            Log.Warn("Execution of the async command with Id " + processSequenceId + " was not successful (" + commandChain[0] + ")");
    //            onlyIFinallyCommands = true;
    //        }
    //        // With asychronous execution we have left the GUI thread, so we have to return
    //        // with the next command:
    //        GuiThreadHelper.InvokeInGui(delegate()
    //        {
    //            ExecuteRecursion(commandChain, onlyIFinallyCommands, registryFinishedCallback);
    //        });
    //    }

    //    protected virtual long AcquireCommand()
    //    {
    //        long processSequenceId;
    //        lock (acquiredIdsSet)
    //        {
    //            while (true)
    //            {
    //                //The ProcessId identifies the concrete execution of a command.
    //                processSequenceId = (long)(random.NextDouble() * long.MaxValue - 1) + 1;
    //                if (!acquiredIdsSet.Contains(processSequenceId))
    //                {
    //                    break;
    //                }
    //            }
    //            acquiredIdsSet.Add(processSequenceId);
    //        }
    //        return processSequenceId;
    //    }

    //    protected virtual void FreeCommand(long processSequenceId)
    //    {
    //        //TODO: please document: why acquire? What if an AsyncCommand calls an AsyncCommand?
    //        lock (acquiredIdsSet)
    //        {
    //            if (acquiredIdsSet.Contains(processSequenceId))
    //            {
    //                acquiredIdsSet.Remove(processSequenceId);
    //            }
    //        }
    //    }
    //    #endregion

    //    #region ICommandExtendable
    //    public virtual void RegisterCommand(ICommand command, String parameterBean, int priority)
    //    {
    //        if (String.IsNullOrEmpty(parameterBean))
    //        {
    //            throw new Exception("Empty beanname is not allowed. Use ICommandStringParameterExtendable instead.");
    //        }
    //        Object parameter = BeanContext.GetService(parameterBean);
    //        RegisterCommandIntern(command, parameter, priority, parameterBean);
    //    }

    //    public virtual void RegisterCommand(ICommand command, String parameterBean)
    //    {
    //        RegisterCommand(command, parameterBean, 0);
    //    }

    //    public virtual void UnregisterCommand(ICommand command, String parameterKey, int priority)
    //    {
    //        CommandKey commandKey = new CommandKey(command, parameterKey, priority);
    //        ICommandContainer commandContainer = keyToCommandContainer.GetExtension(commandKey);
    //        if (!AlwaysExecutable)
    //        {
    //            commandContainer.CanExecuteChanged -= OnCanExecuteChanged;
    //        }
    //        if ((command is IAsyncCommand) || ((command is CommandRegistry) && ((CommandRegistry)command).IsAsync))
    //        {
    //            --nAsyncCommandsRegistered;
    //        }
    //        keyToCommandContainer.Unregister(commandContainer, commandKey);
    //    }

    //    public virtual void UnregisterCommand(ICommand command, String parameterBean)
    //    {
    //        UnregisterCommand(command, parameterBean, 0);
    //    }
    //    #endregion

    //    #region ICommandStringParameterExtendable
    //    public virtual void RegisterCommandWithStringParameter(ICommand command, String parameterString, int priority)
    //    {
    //        RegisterCommandIntern(command, parameterString, priority, parameterString);
    //    }

    //    public virtual void RegisterCommandWithStringParameter(ICommand command, String parameterString)
    //    {
    //        RegisterCommandIntern(command, parameterString, 0, parameterString);
    //    }

    //    public virtual void UnregisterCommandWithStringParameter(ICommand command, String parameterString, int priority)
    //    {
    //        UnregisterCommand(command, parameterString, priority);
    //    }

    //    public virtual void UnregisterCommandWithStringParameter(ICommand command, String parameterString)
    //    {
    //        UnregisterCommand(command, parameterString, 0);
    //    }
    //    #endregion

    //    #region Non-public implementation of ICommandExtendable / ICommandStringParameterExtendable
    //    protected virtual IList<ICommandContainer> GetOrderedCommandContainers()
    //    {
    //        IDictionary<CommandKey, ICommandContainer> containers = keyToCommandContainer.GetExtensions();
    //        List<CommandKey> orderedKeys = new List<CommandKey>(containers.Keys);
    //        orderedKeys.Sort();

    //        IList<ICommandContainer> containerList = new List<ICommandContainer>();
    //        foreach (CommandKey commandKey in orderedKeys)
    //        {
    //            containerList.Add(containers[commandKey]);
    //        }
    //        // Now the result must be ordered corresponding to the command priorities:
    //        //IOrderedEnumerable<ICommandContainer> result = containerList.OrderBy(commandContainer => (commandContainer as ICommandContainer).Priority);
    //        return containerList;
    //    }

    //    protected virtual void RegisterCommandIntern(ICommand command, Object parameter, int priority, String parameterKey)
    //    {
    //        ICommandContainer commandContainer = new CommandContainer();
    //        commandContainer.Command = command;
    //        commandContainer.CommandParameter = parameter;
    //        commandContainer.Priority = priority;
    //        CommandKey commandKey = new CommandKey(command, parameterKey, priority);
    //        keyToCommandContainer.Register(commandContainer, commandKey);
    //        if ((command is IAsyncCommand) || ((command is CommandRegistry) && ((CommandRegistry)command).IsAsync))
    //        {
    //            ++nAsyncCommandsRegistered;
    //        }
    //        if (!AlwaysExecutable)
    //        {
    //            commandContainer.CanExecuteChanged += OnCanExecuteChanged;
    //            OnCanExecuteChanged(commandContainer, EventArgs.Empty);
    //        }
    //    }
    //    #endregion

    //    #region Other protected methods
    //    protected virtual void RaisePropertyChanged(String propertyName)
    //    {
    //        var localEventHandler = PropertyChanged;
    //        if (localEventHandler != null)
    //        {
    //            localEventHandler.Invoke(this, new PropertyChangedEventArgs(propertyName));
    //        }
    //    }

    //    protected virtual void IncreaseBusyCount()
    //    {
    //        lock (busyLock)
    //        {
    //            ++busyCount;
    //            GuiThreadHelper.InvokeInGui(delegate()
    //            {
    //                IsBusy = true;
    //            });
    //        }
    //    }

    //    protected virtual void DecreaseBusyCount()
    //    {
    //        lock (busyLock)
    //        {
    //            --busyCount;
    //            if (busyCount < 1)
    //            {
    //                busyCount = 0;
    //                GuiThreadHelper.InvokeInGui(delegate()
    //                {
    //                    IsBusy = false;
    //                });
    //            }
    //        }
    //    }
    //    #endregion
    //}
    #endregion
}
