using Microsoft.Office.Core;
using System;
using System.IO;
using System.Linq;
using System.Text.RegularExpressions;

namespace OfficeToImages
{
    public class Program
    {
        public static readonly Regex namePattern = new Regex(".+(?:\\\\|/)([^\\\\/]+)");

        protected static Microsoft.Office.Interop.Visio.Application visio;

        protected static Microsoft.Office.Interop.PowerPoint.Application powerpoint;

        static void Main(String[] args)
        {
            if (args.Length != 2)
            {
                Console.Error.WriteLine("Usage: OfficeToImages.exe <source-dir(s)> <target-dir>");
                Console.Error.WriteLine("source-dir(s): Semicolon (;) separated list of directories to scan for office documents");
                Console.Error.WriteLine("target-dir: Directory where to store the generated images");
                return;
            }
            String sourceArg = args[0];

            String target = args[1];

            Console.WriteLine("Saving all generated images to '" + target + "'");


            try
            {
                String[] sources = sourceArg.Split(';');
                foreach (String source in sources)
                {
                    Console.WriteLine("Scanning for office files in '" + source + "'");
                    DirectoryInfo di = new DirectoryInfo(source);
                    FileInfo[] directories = di.GetFiles("*", SearchOption.AllDirectories);

                    foreach (FileInfo fileSource in directories)
                    {
                        String file = fileSource.FullName;
                        if (file.Contains('~') || !File.Exists(file))
                        {
                            continue;
                        }

                        String name = namePattern.Match(file).Groups[1].Value;
                        String lowercaseName = name.ToLowerInvariant();

                        if (name.EndsWith("ppt") || name.EndsWith("pptx"))
                        {
                            Console.Write("Handling '" + name + "'...");
                            if (ParsePowerPoint(file, name, target))
                            {
                                Console.WriteLine("Done");
                            }
                            else
                            {
                                Console.WriteLine("Skipped (unchanged)");
                            }
                        }
                        else if (name.EndsWith("vsd"))
                        {
                            Console.Write("Handling '" + name + "'...");
                            if (ParseVisio(file, name, target))
                            {
                                Console.WriteLine("Done");
                            }
                            else
                            {
                                Console.WriteLine("Skipped (unchanged)");
                            }
                        }
                    }
                }
            }
            finally
            {
                if (powerpoint != null)
                {
                    powerpoint.Quit();
                    powerpoint = null;
                }
                if (visio != null)
                {
                    visio.Quit();
                    visio = null;
                }
            }
        }

        protected static bool IsTargetFileOutdated(String sourceFile, String targetFile)
        {
            if (!File.Exists(targetFile))
            {
                return true;
            }
            DateTime sourceTime = File.GetLastWriteTimeUtc(sourceFile);
            DateTime targetTime = File.GetLastWriteTimeUtc(targetFile);
            return targetTime < sourceTime;
        }

        protected static String BuildTargetFileName(String sourceFileName, String targetDir, int index)
        {
            return targetDir + "/" + sourceFileName + "-" + index + ".png";
        }

        protected static bool ParsePowerPoint(String file, String name, String targetDir)
        {
            int index = 1;
            if (!IsTargetFileOutdated(file, BuildTargetFileName(name, targetDir, index)))
            {
                // if the first slide of a file is not invalid all other slides are still valid, too
                return false;
            }
            if (powerpoint == null)
            {
                powerpoint = new Microsoft.Office.Interop.PowerPoint.Application();
            }
            Microsoft.Office.Interop.PowerPoint.Presentation ppt = powerpoint.Presentations.Open(file, MsoTriState.msoTrue,
                MsoTriState.msoTrue, MsoTriState.msoFalse);
            try
            {
                foreach (Microsoft.Office.Interop.PowerPoint.Slide slide in ppt.Slides)
                {
                    String targetFile = BuildTargetFileName(name, targetDir, index);
                    slide.Export(targetFile, "png", 1024, 768);
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

        protected static bool ParseVisio(String file, String name, String targetDir)
        {
            int index = 1;
            if (!IsTargetFileOutdated(file, BuildTargetFileName(name, targetDir, index)))
            {
                // if the first slide of a file is not invalid all other slides are still valid, too
                return false;
            }
            if (visio == null)
            {
                visio = new Microsoft.Office.Interop.Visio.Application();
                visio.Visible = false;
            }
            Microsoft.Office.Interop.Visio.Document vsd = visio.Documents.Open(file);
            try
            {
                foreach (Microsoft.Office.Interop.Visio.Page page in vsd.Pages)
                {
                    String targetFile = BuildTargetFileName(name, targetDir, index);
                    page.Export(targetFile);
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
