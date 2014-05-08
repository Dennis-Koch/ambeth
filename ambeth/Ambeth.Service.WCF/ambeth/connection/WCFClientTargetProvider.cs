using System;
using De.Osthus.Ambeth.Ioc;
using System.ServiceModel;
using System.ServiceModel.Channels;
using De.Osthus.Ambeth.Config;
using System.ComponentModel;
#if !SILVERLIGHT
using Castle.DynamicProxy;
#else

#endif
using System.Threading;
using De.Osthus.Ambeth.Remote;
using De.Osthus.Ambeth.Service;
using De.Osthus.Ambeth.Log;
using System.ServiceModel.Description;

namespace De.Osthus.Ambeth.Connection
{
    public class WCFClientTargetProvider<I> : IInitializingBean, IRemoteTargetProvider, IOfflineListener where I : class
    {
        public static readonly String CLIENT_PROPERTY = "ClientChannelFactory";

        public static Binding CreateDefaultBinding()
        {
            BasicHttpBinding binding = new BasicHttpBinding();
#if !SILVERLIGHT
            binding.Security.Transport.ClientCredentialType = HttpClientCredentialType.Ntlm;
            binding.ReaderQuotas = System.Xml.XmlDictionaryReaderQuotas.Max;
            binding.Security.Message.ClientCredentialType = BasicHttpMessageCredentialType.UserName;
            binding.Security.Transport.ProxyCredentialType = HttpProxyCredentialType.Ntlm;
#endif
            binding.Security.Mode = BasicHttpSecurityMode.TransportCredentialOnly;
            //binding.HostNameComparisonMode = HostNameComparisonMode.WeakWildcard;
            binding.OpenTimeout = new TimeSpan(1, 0, 0);
            binding.CloseTimeout = binding.OpenTimeout;
            binding.MaxReceivedMessageSize = long.MaxValue;
            binding.MaxBufferSize = int.MaxValue;
            binding.SendTimeout = binding.OpenTimeout;
            binding.ReceiveTimeout = binding.OpenTimeout;
            binding.TransferMode = TransferMode.StreamedResponse;

            return binding;
            //CustomBinding customBinding = new CustomBinding();
            
            //CompressionMessageEncodingBindingElement compressionBindingElement = new CompressionMessageEncodingBindingElement();
            //customBinding.Elements.Add(compressionBindingElement);

            //HttpTransportBindingElement httpBindingElement = new HttpTransportBindingElement();
            //httpBindingElement.AuthenticationScheme = System.Net.AuthenticationSchemes.Ntlm;
            //httpBindingElement.ProxyAuthenticationScheme = System.Net.AuthenticationSchemes.Ntlm;
            //httpBindingElement.DecompressionEnabled = true;
            //httpBindingElement.KeepAliveEnabled = true;
            //httpBindingElement.MaxReceivedMessageSize = int.MaxValue;
            //httpBindingElement.TransferMode = TransferMode.Streamed;
            //httpBindingElement.HostNameComparisonMode = HostNameComparisonMode.WeakWildcard;

            //customBinding.Elements.Add(httpBindingElement);

            //return customBinding;
        }

        public static String GetServiceName(String name)
        {
            if (name.EndsWith("Client"))
            {
                name = name.Substring(0, name.Length - 6) + "Service";
            }
            else if (name.EndsWith("WCF"))
            {
                name = name.Substring(0, name.Length - 3);
            }
            if (name.StartsWith("I"))
            {
                return name.Substring(1);
            }
            return name;
        }

        [LogInstance]
        public ILogger Log { private get; set; }
        
        protected readonly Object connectLock = new Object();

        protected ChannelFactory<I> channelFactory;

        protected ChannelFactory<I> ChannelFactory
        {
            get
            {
                if (channelFactory == null)
                {
                    OpenConnection();
                }
                return channelFactory;
            }
            private set
            {
                if (channelFactory != null && channelFactory.State == CommunicationState.Opened)
                {
                    channelFactory.Close();
                }
                channelFactory = value;
                client = null;
                if (value != null)
                {
                    value.Opened += new EventHandler(Client_OpenCompleted);
                    value.Closed += new EventHandler(Client_CloseCompleted);
                    value.Faulted += new EventHandler(Client_Faulted);
                }
                OnPropertyChanged(CLIENT_PROPERTY);
            }
        }

        public IServiceContext BeanContext { protected get; set; }

        public IBindingFactory BindingFactory { protected get; set; }

        public bool LoggingActive { protected get; set; }

        public String ServiceName { get; set; }

        public bool SyncMode { get; private set; }
        
        [Property(ServiceConfigurationConstants.ServiceBaseUrl)]
        public String ServiceBaseUrl { protected get; set; }

        public bool ReconnectOnOfflineChange { get; set; }

        protected readonly Object clientLock = new Object();

        protected I client;

        protected bool onlineChangePending;

        public virtual void AfterPropertiesSet()
        {
            ReconnectOnOfflineChange = ServiceName == null || !ServiceName.Equals("SyncService");

            SyncMode = !typeof(ICommunicationObject).IsAssignableFrom(typeof(I));

            LoggingActive = true;
        }

        void Client_OpenCompleted(object sender, EventArgs e)
        {
        }

        void Client_CloseCompleted(object sender, EventArgs e)
        {
            ChannelFactory = null;
        }

        void Client_Faulted(object sender, EventArgs e)
        {
            ChannelFactory = null;
        }

        protected String GetHostString()
        {
            return ServiceBaseUrl;
        }

        protected virtual void OpenConnection()
        {
            String hostUrl = GetHostString();

            Binding binding;
            if (BindingFactory != null)
            {
                binding = BindingFactory.CreateBinding(typeof(I), ServiceName, hostUrl);
            }
            else
            {
                binding = CreateDefaultBinding();
            }

            if (ServiceName != null)
            {
                hostUrl += "/" + ServiceName;
            }

            ChannelFactory = ExtendChannelFactory(new ChannelFactory<I>(binding, new EndpointAddress(hostUrl)));
        }

        public Object GetTarget()
        {
            lock (clientLock)
            {
                if (client == null)
                {
                    client = ChannelFactory.CreateChannel();
                }
                return client;
            }
        }

        protected ChannelFactory<T> ExtendChannelFactory<T>(ChannelFactory<T> channelFactory)
        {
#if !SILVERLIGHT
            foreach (OperationDescription op in channelFactory.Endpoint.Contract.Operations)
            {
                DataContractSerializerOperationBehavior dataContractBehavior = (DataContractSerializerOperationBehavior)op.Behaviors.Find<DataContractSerializerOperationBehavior>();
                if (dataContractBehavior == null)
                {
                    dataContractBehavior = new DataContractSerializerOperationBehavior(op);
                    op.Behaviors.Add(dataContractBehavior);
                }
                dataContractBehavior.MaxItemsInObjectGraph = int.MaxValue;
            }
#endif
            //ZipClientMessageInspector compressBehavior = (ZipClientMessageInspector)channelFactory.Endpoint.Behaviors.Find<ZipClientMessageInspector>();
            //if (compressBehavior == null)
            //{
            //    compressBehavior = new ZipClientMessageInspector();
            //    channelFactory.Endpoint.Behaviors.Add(compressBehavior);
            //}

            MaxFaultSizeBehavior maxfaultSizeBehavior = new MaxFaultSizeBehavior(int.MaxValue);
            channelFactory.Endpoint.Behaviors.Add(maxfaultSizeBehavior);

            return channelFactory;
        }

        bool IsError(AsyncCompletedEventArgs e)
        {
            if (e.Error == null)
            {
                return false;
            }
            if (e.Error is FaultException<ExceptionDetail>)
            {
                FaultException<ExceptionDetail> fault = e.Error as FaultException<ExceptionDetail>;
                // fault.Detail.Type contains the server exception type.
                // fault.Detail.Message contains the server exception message.
                // fault.Detail.StackTrace contains the server stack trace.
                Log.Error("IsError", fault.Detail);
            }
            else
            {
                Log.Error("IsError", e.Error);
            }
            return true;
        }

        #region INotifyPropertyChanged Members

        public event PropertyChangedEventHandler PropertyChanged;

        protected virtual void OnPropertyChanged(string propertyName)
        {
            if (PropertyChanged != null)
            {
                PropertyChanged(this, new PropertyChangedEventArgs(propertyName));
            }
        }

        #endregion

        public void BeginOnline()
        {
            onlineChangePending = true;
            if (ReconnectOnOfflineChange)
            {
                this.ChannelFactory = null;
            }
        }

        public void HandleOnline()
        {
            // Intended blank
        }

        public void EndOnline()
        {
            lock (clientLock)
            {
                onlineChangePending = false;
                Monitor.PulseAll(clientLock);
            }
        }

        public void BeginOffline()
        {
            onlineChangePending = true;
            if (ReconnectOnOfflineChange)
            {
                this.ChannelFactory = null;
            }
        }

        public void HandleOffline()
        {
            // Intended blank
        }

        public void EndOffline()
        {
            lock (clientLock)
            {
                onlineChangePending = false;
                Monitor.PulseAll(clientLock);
            }
        }
    }
}
