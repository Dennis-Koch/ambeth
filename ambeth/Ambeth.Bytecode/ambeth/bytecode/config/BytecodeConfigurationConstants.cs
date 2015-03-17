using De.Osthus.Ambeth.Annotation;
using System;

namespace De.Osthus.Ambeth.Bytecode.Config
{
    [ConfigurationConstants]
    public sealed class BytecodeConfigurationConstants
    {
        /// <summary>
        /// If specified all bytecode enhancements will be written to this directory for debugging purpose
        /// </summary>
        public const String EnhancementTraceDirectory = "ambeth.bytecode.tracedir";

        private BytecodeConfigurationConstants()
        {
            // intended blank
        }
    }
}