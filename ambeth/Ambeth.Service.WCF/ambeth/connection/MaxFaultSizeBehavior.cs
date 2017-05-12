using System.ServiceModel.Channels;
using System.ServiceModel.Description;
using System.ServiceModel.Dispatcher;

namespace De.Osthus.Ambeth.Connection
{
    public class MaxFaultSizeBehavior : IEndpointBehavior
    {
        private int size;

        public MaxFaultSizeBehavior(int s)
        {
            size = s;
        }

        public void AddBindingParameters(ServiceEndpoint endpoint, BindingParameterCollection bindingParameters)
        {
            // intended blank
        }

        public void ApplyClientBehavior(ServiceEndpoint endpoint, ClientRuntime clientRuntime)
        {
            clientRuntime.MaxFaultSize = size;
        }

        public void ApplyDispatchBehavior(ServiceEndpoint endpoint, EndpointDispatcher endpointDispatcher)
        {
            // intended blank
        }

        public void Validate(ServiceEndpoint endpoint)
        {
            // intended blank
        }
    }
}
