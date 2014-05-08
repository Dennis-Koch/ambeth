using System;
using System.Collections.Generic;
using Castle.DynamicProxy;
using System.Reflection.Emit;
using System.Runtime.Serialization;
using System.Reflection;
using De.Osthus.Ambeth.Proxy;
using System.ServiceModel;

namespace De.Osthus.Ambeth.Proxy
{
    public class WCFProxyOptionsFactory : IProxyOptionsFactory
    {
        protected IEnumerable<CustomAttributeBuilder> attributeBuilders;

        public WCFProxyOptionsFactory()
        {
             attributeBuilders = CreateAttributeBuilders();
        }

        public virtual ProxyGenerationOptions CreateProxyGenerationOptions()
        {
            ProxyGenerationOptions options = new ProxyGenerationOptions();
            foreach (CustomAttributeBuilder attributeBuilder in attributeBuilders)
            {
                options.AdditionalAttributes.Add(attributeBuilder);
            }
            return options;
        }

        protected virtual IEnumerable<CustomAttributeBuilder> CreateAttributeBuilders()
        {
            return new CustomAttributeBuilder[] {
#if !SILVERLIGHT
                CreateSerializableAttributeBuilder(),
                CreateServiceBehaviorAttributeBuilder()
#else
#endif
            };
        }

#if !SILVERLIGHT
        protected virtual CustomAttributeBuilder CreateSerializableAttributeBuilder()
        {
            Type attributeType = typeof(SerializableAttribute);

            return new CustomAttributeBuilder(attributeType.GetConstructor(new Type[0]), new Object[0]);
        }

        protected virtual CustomAttributeBuilder CreateServiceBehaviorAttributeBuilder()
        {
            Type attributeType = typeof(ServiceBehaviorAttribute);
            return new CustomAttributeBuilder(
                attributeType.GetConstructor(new Type[0]), new object[0],
                new PropertyInfo[] { attributeType.GetProperty("InstanceContextMode"), attributeType.GetProperty("ConcurrencyMode") },
                new Object[] { InstanceContextMode.Single, ConcurrencyMode.Multiple },
                new FieldInfo[0],
                new Object[0]);
    //            [ServiceContract(Name="ISyncService", Namespace="http://schemas.osthus.de/Ambeth.Sync")]
    //[ServiceKnownType("RegisterKnownTypes", typeof(SyncServiceModelProvider))]


            //return new CustomAttributeBuilder(attributeType.GetConstructor(new Type[0]), new Object[0]);

            //return new CustomAttributeBuilder(
            //    attributeType.GetConstructor(new Type[0]), new object[0],
            //    new PropertyInfo[] { attributeType.GetProperty("IsReference"), attributeType.GetProperty("Namespace") },
            //    new Object[] { true, "http://schemas.osthus.de/Ambeth.Proxy" },
            //    new FieldInfo[0],
            //    new Object[0]);
        }
#else
#endif
    }
}
