using System;

namespace De.Osthus.Ambeth.Bytecode.Config
{
    public sealed class BytecodeConfigurationConstants
    {
        private BytecodeConfigurationConstants()
        {
            // intended blank
        }


        /// <summary>
        /// If specified all bytecode enhancements will be written to this directory for debugging purpose
        /// </summary>
        public const String EnhancementTraceDirectory = "ambeth.bytecode.tracedir";
    }
}