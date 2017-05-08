using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Factory;
using Microsoft.Office.Core;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Security.Cryptography;
using System.Text.RegularExpressions;
using System.Threading;

namespace OfficeToImages
{
    public class Program
    {
        static void Main(String[] args)
        {
            Properties.Application.FillWithCommandLineArgs(args);
            Properties.LoadBootstrapPropertyFile();

            Properties props = Properties.Application;
            
            IServiceContext bootstrapContext = BeanContextFactory.CreateBootstrap(props, typeof(IocModule));
		    try
		    {
                bootstrapContext.CreateService(typeof(ParserModule));
		    }
		    finally
		    {
			    bootstrapContext.Dispose();
		    }
        }
    }
}
