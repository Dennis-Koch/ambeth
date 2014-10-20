using Microsoft.Office.Core;
using System;
using System.IO;
using System.Linq;
using System.Security.Cryptography;
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
            if (args.Length != 2 && args.Length != 1)
            {
                Console.Error.WriteLine("Usage: OfficeToImages.exe <source-dir(s)> [<target-dir>]");
                Console.Error.WriteLine("source-dir(s): Semicolon (;) separated list of directories to scan for office documents");
                Console.Error.WriteLine("target-dir: Directory where to store the generated images. If omitted the current directory will be used");
                return;
            }
            String sourceArg = args[0];

            String target = args.Length > 1 ? args[1] : ".";
            DirectoryInfo targetInfo = new DirectoryInfo(target);
            targetInfo.Create();

            Console.WriteLine("Saving all generated images to '" + targetInfo.FullName + "'");
            
            try
            {
                String[] sources = sourceArg.Split(';');
                foreach (String source in sources)
                {
                    Console.WriteLine("Scanning for office files in '" + source + "'");
                    DirectoryInfo di = new DirectoryInfo(source);
                    FileInfo[] directories = di.GetFiles("*", SearchOption.AllDirectories);

                    foreach (FileInfo sourceFile in directories)
                    {
                        if (sourceFile.Name.Contains('~') || !sourceFile.Exists)
                        {
                            continue;
                        }
                        String lowercaseName = sourceFile.Extension.ToLowerInvariant();

                        if (lowercaseName.EndsWith("ppt") || lowercaseName.EndsWith("pptx"))
                        {
                            Console.WriteLine("Handling '" + sourceFile.Name + "'...");
                            if (ParsePowerPoint(sourceFile, targetInfo))
                            {
                                Console.WriteLine("Done");
                            }
                            else
                            {
                                Console.WriteLine("Skipped (unchanged)");
                            }
                        }
                        else if (lowercaseName.EndsWith("vsd"))
                        {
                            Console.WriteLine("Handling '" + sourceFile.Name + "'...");
                            if (ParseVisio(sourceFile, targetInfo))
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

        protected static bool IsTargetFileOutdated(FileInfo sourceFile, FileInfo targetFile)
        {
            if (!targetFile.Exists)
            {
                return true;
            }
            DateTime sourceTime = sourceFile.LastWriteTimeUtc;
            DateTime targetTime = targetFile.LastWriteTimeUtc;
            return targetTime < sourceTime;
        }

        protected static FileInfo BuildTargetFileName(FileInfo sourceFile, DirectoryInfo targetDir, int index)
        {
            String simpleName = sourceFile.Name.Substring(0, sourceFile.Name.Length - sourceFile.Extension.Length );
            simpleName = simpleName.Replace(' ', '-').Replace('.','-').Replace('_','-');
            while (simpleName.Contains("--"))
            {
                simpleName = simpleName.Replace("--", "-");
            }
            return new FileInfo(targetDir.FullName + "\\" + simpleName + "-" + index + ".png");
        }

        protected static FileInfo BuildTargetTempFileName(FileInfo sourceFile, DirectoryInfo targetDir, int index)
        {
            String simpleName = sourceFile.Name.Substring(1, sourceFile.Name.Length - sourceFile.Extension.Length);
            simpleName = simpleName.Replace(' ', '-').Replace('.', '-').Replace('_', '-');
            while (simpleName.Contains("--"))
            {
                simpleName = simpleName.Replace("--", "-");
            }
            return new FileInfo(targetDir.FullName + "\\" + simpleName + "-" + index + "-temp.png");
        }

        protected static bool ParsePowerPoint(FileInfo sourceFile, DirectoryInfo targetDir)
        {
            int index = 1;
            if (!IsTargetFileOutdated(sourceFile, BuildTargetFileName(sourceFile, targetDir, index)))
            {
                // if the first slide of a file is not invalid all other slides are still valid, too
                return false;
            }
            if (powerpoint == null)
            {
                powerpoint = new Microsoft.Office.Interop.PowerPoint.Application();
            }
            Microsoft.Office.Interop.PowerPoint.Presentation ppt = powerpoint.Presentations.Open(sourceFile.FullName, MsoTriState.msoTrue,
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

        protected static bool ParseVisio(FileInfo sourceFile, DirectoryInfo targetDir)
        {
            int index = 1;
            if (!IsTargetFileOutdated(sourceFile, BuildTargetFileName(sourceFile, targetDir, index)))
            {
                // if the first slide of a file is not invalid all other slides are still valid, too
                return false;
            }
            if (visio == null)
            {
                visio = new Microsoft.Office.Interop.Visio.Application();
                visio.Visible = false;
            }
            Microsoft.Office.Interop.Visio.Document vsd = visio.Documents.Open(sourceFile.FullName);
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

        protected static bool DoExport(FileInfo sourceFile, DirectoryInfo targetDir, int index, ExportDelegate exportDelegate)
        {
            FileInfo targetFile = BuildTargetFileName(sourceFile, targetDir, index);
            if (!targetFile.Exists)
            {
                exportDelegate(targetFile);
                UpdateFileInfo(targetFile, sourceFile);
                Console.WriteLine("\tCreated '" + targetFile + "'");
                return true;
            }
            FileInfo tempTargetFile = BuildTargetFileName(sourceFile, targetDir, index);
            exportDelegate(tempTargetFile);

            if (AreFilesEqual(tempTargetFile, targetFile))
            {
                Console.WriteLine("\tSkipped '" + targetFile + "' (still current)");
                UpdateFileInfo(targetFile, sourceFile);
                return false;
            }
            targetFile.Delete();
            tempTargetFile.MoveTo(targetFile.FullName);
            UpdateFileInfo(targetFile, sourceFile);
            Console.WriteLine("\tUpdated '" + targetFile + "'");
            return true;
        }

        protected static void UpdateFileInfo(FileInfo targetFile, FileInfo baseFileInfo)
        {
            targetFile.CreationTimeUtc = baseFileInfo.CreationTimeUtc;
            targetFile.LastWriteTimeUtc = baseFileInfo.LastWriteTimeUtc;
            targetFile.LastAccessTimeUtc = baseFileInfo.LastAccessTimeUtc;
        }

        protected static bool AreFilesEqual(FileInfo leftFile, FileInfo rightFile)
        {
            // first check size
            if (leftFile.Length != rightFile.Length)
            {
                return false;
            }
            // then check checksum
            byte[] newChecksum = Checksum(leftFile);
            byte[] oldChecksum = Checksum(rightFile);

            if (newChecksum.Length != oldChecksum.Length)
            {
                return false;
            }
            for (int a = newChecksum.Length; a-- > 0; )
            {
                if (newChecksum[a] != oldChecksum[a])
                {
                    return false;
                }
            }
            return true;
        }

        protected static byte[] Checksum(FileInfo file)
        {
            using (FileStream stream = File.OpenRead(file.FullName))
            {
                HashAlgorithm sha = new SHA256Managed();
                return sha.ComputeHash(stream);
            }
        }
    }
}
