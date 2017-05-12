using De.Osthus.Ambeth.Annotation;
using System;
using System.ServiceModel;

namespace De.Osthus.Ambeth.Log
{
    [ConfigurationConstants]
    public sealed class LogConfigurationConstants
    {
		/// <summary>
		/// The path to the file to which Ambeth should write the log statements. No default value, if not set Ambeth will not log to file.
		/// </summary>
        public const String LogFile = "ambeth.log.file";

        private LogConfigurationConstants()
        {
            // intended blank
        }
    }
}
