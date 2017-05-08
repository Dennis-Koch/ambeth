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
    public class VisioParser : AbstractFileParser
    {
        protected readonly ThreadLocal<Microsoft.Office.Interop.Visio.Application> visioTL = new ThreadLocal<Microsoft.Office.Interop.Visio.Application>();
        		
		public override void PostParse()
		{
            base.PostParse();

            Microsoft.Office.Interop.Visio.Application visio = visioTL.Value;
            if (visio != null)
            {
                visioTL.Value = null;

				visio.Quit();
                visio = null;
			}
		}
        
        public override bool Parse(FileInfo sourceFile, DirectoryInfo targetDir)
        {
            int index = 1;
            if (!IsTargetFileOutdated(sourceFile, BuildTargetFileName(sourceFile, targetDir, index)))
            {
                // if the first slide of a file is not invalid all other slides are still valid, too
                return false;
            }
            Microsoft.Office.Interop.Visio.Application visio = visioTL.Value;
            if (visio == null)
            {
                visio = new Microsoft.Office.Interop.Visio.Application();
                visio.Visible = false;

                visioTL.Value = visio;
            }
            Microsoft.Office.Interop.Visio.Document vsd = visioTL.Value.Documents.Open(sourceFile.FullName);
            try
            {
                foreach (Microsoft.Office.Interop.Visio.Page page in vsd.Pages)
                {
                    DoExport(sourceFile, targetDir, index, delegate(FileInfo targetFile)
                    {
                        page.Export(targetFile.FullName);
                    });
                    index++;
                }
                return true;
            }
            finally
            {
                vsd.Saved = true;
                vsd.Close();
            }
        }
    }
}
