using De.Osthus.Ambeth.Log;
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
    public abstract class AbstractFileParser : IFileParser
    {
        public static readonly Regex namePattern = new Regex(".+(?:\\\\|/)([^\\\\/]+)");

        [LogInstance]
        public ILogger Log { private get; set; }

        private readonly List<FileInfoUpdateDelegate> fileInfoUpdateDelegates = new List<FileInfoUpdateDelegate>();

        public virtual void PreParse()
        {
            // intended blank
        }


        public virtual void PostParse()
        {
            // intended blank
        }

        public abstract bool Parse(FileInfo sourceFile, DirectoryInfo targetDir);
        
        protected bool IsTargetFileOutdated(FileInfo sourceFile, FileInfo targetFile)
        {
            if (!targetFile.Exists)
            {
                return true;
            }
            DateTime sourceTime = sourceFile.LastWriteTimeUtc;
            DateTime targetTime = targetFile.LastWriteTimeUtc;
            return targetTime < sourceTime;
        }

        protected FileInfo BuildTargetFileName(FileInfo sourceFile, DirectoryInfo targetDir, int index)
        {
            String simpleName = sourceFile.Name.Substring(0, sourceFile.Name.Length - sourceFile.Extension.Length );
            simpleName = simpleName.Replace(' ', '-').Replace('.','-').Replace('_','-');
            while (simpleName.Contains("--"))
            {
                simpleName = simpleName.Replace("--", "-");
            }
            return new FileInfo(targetDir.FullName + "\\" + simpleName + "-" + index + ".png");
        }

        protected FileInfo BuildTargetTempFileName(FileInfo sourceFile, DirectoryInfo targetDir, int index)
        {
            String simpleName = sourceFile.Name.Substring(1, sourceFile.Name.Length - sourceFile.Extension.Length);
            simpleName = simpleName.Replace(' ', '-').Replace('.', '-').Replace('_', '-');
            while (simpleName.Contains("--"))
            {
                simpleName = simpleName.Replace("--", "-");
            }
            return new FileInfo(targetDir.FullName + "\\" + simpleName + "-" + index + "-temp.png");
        }

        protected bool DoExport(FileInfo sourceFile, DirectoryInfo targetDir, int index, ExportDelegate exportDelegate)
        {
            FileInfo targetFile = BuildTargetFileName(sourceFile, targetDir, index);
            fileInfoUpdateDelegates.Add(delegate()
            {
                UpdateFileInfo(targetFile, sourceFile);
            });
            if (!targetFile.Exists)
            {
                exportDelegate(targetFile);
                Log.Info("\tCreated '" + targetFile + "'");
                return true;
            }
            FileInfo tempTargetFile = BuildTargetFileName(sourceFile, targetDir, index);
            exportDelegate(tempTargetFile);

            if (AreFilesEqual(tempTargetFile, targetFile))
            {
                Log.Info("\tSkipped '" + targetFile + "' (still current)");
                return false;
            }
            targetFile.Delete();
            tempTargetFile.MoveTo(targetFile.FullName);
            Log.Info("\tUpdated '" + targetFile + "'");
            return true;
        }

        protected void UpdateFileInfo(FileInfo targetFile, FileInfo baseFileInfo)
        {
            targetFile.CreationTimeUtc = baseFileInfo.CreationTimeUtc;
            targetFile.LastWriteTimeUtc = baseFileInfo.LastWriteTimeUtc;
            targetFile.LastAccessTimeUtc = baseFileInfo.LastAccessTimeUtc;
        }

        protected bool AreFilesEqual(FileInfo leftFile, FileInfo rightFile)
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
