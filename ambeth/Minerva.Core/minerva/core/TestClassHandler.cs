using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.ComponentModel;

namespace Ambeth.Bind.ambeth.bind
{
    public class TestClassHandler
    {
        public void HandleIt(object sender, PropertyChangedEventArgs e)
        {
            Console.WriteLine("HEY!");
        }
    }
}
