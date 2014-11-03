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
    public class PowerpointParser : AbstractFileParser
    {
        protected readonly ThreadLocal<Microsoft.Office.Interop.PowerPoint.Application> powerpointTL = new ThreadLocal<Microsoft.Office.Interop.PowerPoint.Application>();

		public override void PostParse()
		{
            base.PostParse();

            Microsoft.Office.Interop.PowerPoint.Application powerpoint = powerpointTL.Value;
			if (powerpoint != null)
            {
                powerpointTL.Value = null;

				powerpoint.Quit();
                powerpoint = null;
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
            Microsoft.Office.Interop.PowerPoint.Application powerpoint = powerpointTL.Value;
            if (powerpoint == null)
            {
                powerpoint = new Microsoft.Office.Interop.PowerPoint.Application();

                powerpointTL.Value = powerpoint;
            }
            Microsoft.Office.Interop.PowerPoint.Presentation ppt = powerpointTL.Value.Presentations.Open(sourceFile.FullName, MsoTriState.msoTrue,
                MsoTriState.msoTrue, MsoTriState.msoFalse);
            try
            {
                foreach (Microsoft.Office.Interop.PowerPoint.Slide slide in ppt.Slides)
                {
                    DoExport(sourceFile, targetDir, index, delegate(FileInfo targetFile)
                    {
                        slide.Export(targetFile.FullName, "png", 1024, 768);
                    });
                    index++;
                }
                return true;
            }
            finally
            {
                ppt.Saved = MsoTriState.msoTrue;
                ppt.Close();
            }
        }
    }
}