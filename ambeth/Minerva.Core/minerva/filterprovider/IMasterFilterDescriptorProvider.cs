namespace De.Osthus.Minerva.FilterProvider
{
    public interface IFilterDescriptorProviderExtendable
    {
        // It is possible to have different sources that provide filter criteria.
        // In such cases, one master provider is used for the VMDCC and all other
        // providers should be registered by the master.
        void RegisterProvider(IFilterDescriptorProvider provider);

        void UnregisterProvider(IFilterDescriptorProvider provider);
    }
}
