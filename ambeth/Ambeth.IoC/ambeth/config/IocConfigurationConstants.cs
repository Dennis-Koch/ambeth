using De.Osthus.Ambeth.Annotation;
using System;

namespace De.Osthus.Ambeth.Config
{
    [ConfigurationConstants]
    public sealed class IocConfigurationConstants
    {
		/// <summary>
		/// Defines whether IoC classes (especially beans and properties) should save the stack trace of their declaration. If set to true the classes store a
		/// compact form of the stack trace during constructor invocation in a property, mostly called <code>declarationStackTrace</code>. Valid values: "true" or
		/// "false". Default is "false".
		/// </summary>
        public const String TrackDeclarationTrace = "ambeth.ioc.declaration.trace.active";

		/// <summary>
		/// Allows to monitor all IoC managed beans via JMX. Valid values: "true" or "false". Default is "true".
		/// </summary>
        public const String MonitorBeansActive = "ambeth.ioc.monitoring.active";

		/// <summary>
		/// Allows to run the IoC container in debug mode. This e.g. disables several security behaviors to ease debugging. Valid values: "true" or "false". Default is "false".
		/// </summary>
        public const String DebugModeActive = "ambeth.ioc.debug.active";

		/// <summary>
		/// Allows Ambeth to transparently fork several algorithms to reduce execution time. This is very beneficial e.g. for large JDBC SELECT usecases or Ambeth
		/// Entity Prefetch logic. Valid values: "true" or "false". Default is "true". is "false".
		/// </summary>
		public const String TransparentParallelizationActive = "ambeth.transparent.parallel.active";

        private IocConfigurationConstants()
        {
        }
    }
}