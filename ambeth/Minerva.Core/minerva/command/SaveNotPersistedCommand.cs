using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Reflection;
using System.Windows.Input;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Util;
using De.Osthus.Minerva.Core;
using AmbethIDataObject = De.Osthus.Ambeth.Model.IDataObject;
using De.Osthus.Ambeth.Log;

namespace De.Osthus.Minerva.Command
{
    public class SaveNotPersistedCommand : ControllerServiceCommand
    {
        [LogInstance]
		public new ILogger Log { private get; set; }

        // Persist all Changes:
        // (Note that a call to the Save method also deletes entities with ToBeDeleted flag!)
        public override void Execute(Object parameter)
        {
            base.Execute(parameter);
        }
    }
}
