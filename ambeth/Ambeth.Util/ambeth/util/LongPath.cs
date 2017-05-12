using Microsoft.Win32.SafeHandles;
using System;
using System.ComponentModel;
using System.IO;
using System.Runtime.InteropServices;

namespace De.Osthus.Ambeth.Util
{
    public class LongPath
    {
        [StructLayout(LayoutKind.Sequential)]
        public class SECURITY_ATTRIBUTES
        {
            public int nLength;
            public IntPtr pSecurityDescriptor;
            public int bInheritHandle;
        }

        [DllImport("kernel32.dll", CharSet = CharSet.Unicode, SetLastError = true)]
        public static extern bool CreateDirectory(String lpPathName, SECURITY_ATTRIBUTES lpSecurityAttributes);

        [DllImport("kernel32.dll", CharSet = CharSet.Unicode, SetLastError = true)]
        internal static extern SafeFileHandle CreateFile(String lpFileName, System.IO.FileAccess dwDesiredAccess,
            System.IO.FileShare dwShareMode,
            SECURITY_ATTRIBUTES lpSecurityAttributes,
            System.IO.FileMode creationDisposition,
            System.IO.FileAttributes dwFlagsAndAttributes,
            IntPtr hTemplateFile);

        [DllImport("kernel32.dll", CharSet = CharSet.Unicode, SetLastError = true)]
        internal static extern bool DeleteFile(String lpFileName);

        [DllImport("kernel32.dll", CharSet = CharSet.Unicode, SetLastError = true)]
        internal static extern System.IO.FileAttributes GetFileAttributes(String lpPathName);

        public static SafeFileHandle CreateFile(String fileName, System.IO.FileAccess fileAccess, System.IO.FileShare fileShare, System.IO.FileMode fileMode)
        {
            String prefix = @"\\?\";
            String formattedName = fileName;
            if (!fileName.StartsWith(prefix))
            {
                formattedName = prefix + fileName;
            }
            int fileNameIndex = formattedName.LastIndexOf(System.IO.Path.DirectorySeparatorChar);
            if (fileNameIndex >= 0)
            {
                String dirName = formattedName.Substring(0, fileNameIndex);
                CreateDir(dirName);
            }
            SafeFileHandle fileHandle = CreateFile(formattedName, fileAccess, fileShare, null, fileMode, 0, IntPtr.Zero);
            if (fileHandle.IsInvalid)
            {
                int lastWin32Error = Marshal.GetLastWin32Error();
                throw new Win32Exception(lastWin32Error);
            }
            return fileHandle;
        }

        public static void DeleteDir(String dir)
        {
            System.IO.FileAttributes fileAttributes = GetFileAttributes(dir);
            if (fileAttributes == 0)
            {
                return;
            }
            if (DeleteFile(dir))
            {
                return;
            }
        }

        public static String CreateDir(String dir)
        {
            dir = dir.Replace('/', Path.DirectorySeparatorChar);
            if (!dir.StartsWith(@"\\?\"))
            {
                dir = @"\\?\" + dir;
            }
            System.IO.FileAttributes fileAttributes = GetFileAttributes(dir);
            if (fileAttributes >= 0)
            {
                return dir;
            }
            if (CreateDirectory(dir, null))
            {
                return dir;
            }
            int fileNameIndex = dir.LastIndexOf(System.IO.Path.DirectorySeparatorChar);
            if (fileNameIndex >= 0)
            {
                String parentDir = dir.Substring(0, fileNameIndex);
                CreateDir(parentDir);
                CreateDirectory(dir, null);
            }
            return dir;
        }
    }
}
