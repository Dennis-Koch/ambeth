using De.Osthus.Ambeth.Annotation;
using System;

namespace De.Osthus.Ambeth.Config
{
    [ConfigurationConstants]
    public sealed class ServiceConfigurationConstants
    {
        public const String WrapAllInteractions = "ioc.logged";

        public const String LogShortNames = "log.shortnames";

        public const String NetworkClientMode = "network.clientmode";

        public const String SlaveMode = "service.slavemode";

        public const String GenericTransferMapping = "tranfermapping.generic";

        public const String IndependentMetaData = "metadata.independent";

        public const String ProcessServiceBeanActive = "process.service.active";

        public const String ServiceBaseUrl = "service.baseurl";

        public const String ServiceProtocol = "service.protocol";

        public const String ServiceHostName = "service.host";

        public const String ServiceHostPort = "service.port";

        public const String ServicePrefix = "service.prefix";
        
	    public const String TypeInfoProviderType = "service.typeinfoprovider.type";

        public const String ServiceRemoteInterceptorType = "service.remoteinterceptor.type";

        public const String UserName = "username";

        public const String Password = "password";

        public const String MappingFile = "mapping.file";

        public const String ValueObjectFile = "valueobject.file";

        public const String OfflineMode = "network.offlinemode";

        public const String OfflineModeSupported = "network.offlinemode.supported";

	    public const String ToOneDefaultCascadeLoadMode = "cache.cascadeload.toone";

        public const String ToManyDefaultCascadeLoadMode = "cache.cascadeload.tomany";

        private ServiceConfigurationConstants()
        {
            // intended blank
        }
    }
}