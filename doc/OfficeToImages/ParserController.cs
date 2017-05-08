using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Extendable;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Threading;
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
    public class ParserController : IStartingBean, IFileParserExtendable
    {
        public static readonly Regex namePattern = new Regex(".+(?:\\\\|/)([^\\\\/]+)");

        [LogInstance]
        public ILogger Log { private get; set; }

        [Property("source-dir")]
        public String SourceDir { protected get; set; }

        [Property("target-dir", DefaultValue = ".")]
        public String TargetDir { protected get; set; }

        protected readonly ThreadLocal<IList<FileInfoUpdateDelegate>> fileInfoUpdateDelegatesTL = new ThreadLocal<IList<FileInfoUpdateDelegate>>();

        protected readonly MapExtendableContainer<String, IFileParser> fileParsers = new MapExtendableContainer<String, IFileParser>("fileParser", "extension");

        public void AfterStarted()
        {
            DirectoryInfo targetInfo = new DirectoryInfo(TargetDir);
            targetInfo.Create();

            Log.Info("Saving all generated images to '" + targetInfo.FullName + "'");
            
            String[] sources = SourceDir.Split(';');
            foreach (String source in sources)
            {
                Log.Info("Scanning for office files in '" + source + "'");
                DirectoryInfo di = new DirectoryInfo(source);
                FileInfo[] directories = di.GetFiles("*", SearchOption.AllDirectories);

                IdentityHashMap<IFileParser, List<FileInfo>> queuedFilesMap = new IdentityHashMap<IFileParser, List<FileInfo>>();

                foreach (FileInfo sourceFile in directories)
                {
                    if (sourceFile.Name.Contains('~') || !sourceFile.Exists)
                    {
                        continue;
                    }
                    String lowercaseExtensionName = sourceFile.Extension.ToLowerInvariant();
                    if (lowercaseExtensionName.StartsWith("."))
                    {
                        lowercaseExtensionName = lowercaseExtensionName.Substring(1);
                    }
                    IFileParser fileParser = fileParsers.GetExtension(lowercaseExtensionName);
                    if (fileParser == null)
                    {
                        Log.Debug("Skipping '" + sourceFile.FullName + "': no parser configured for '" + lowercaseExtensionName + "'");
                        continue;
                    }
                    List<FileInfo> queuedFiles = queuedFilesMap.Get(fileParser);
                    if (queuedFiles == null)
                    {
                        queuedFiles = new List<FileInfo>();
                        queuedFilesMap.Put(fileParser, queuedFiles);
                    }
                    queuedFiles.Add(sourceFile);
                }
                List<Thread> threads = new List<Thread>();
                CountDownLatch latch = new CountDownLatch(queuedFilesMap.Count);

                foreach (Entry<IFileParser, List<FileInfo>> entry in queuedFilesMap)
                {
                    IFileParser fileParser = entry.Key;
                    IList<FileInfo> sourceFiles = entry.Value;
                    Thread thread = new Thread(new ThreadStart(delegate()
                        {
                            ParseFiles(fileParser, sourceFiles, targetInfo, latch);
                        }));
                    thread.Name = fileParser.GetType().Name;
                    thread.IsBackground = true;
                    threads.Add(thread);
                }
                foreach (Thread thread in threads)
                {
                    thread.Start();
                }
                latch.Await(TimeSpan.FromMinutes(5)); // throw exception after some minutes
            }
        }

        protected void ParseFiles(IFileParser fileParser, IList<FileInfo> sourceFiles, DirectoryInfo targetInfo, CountDownLatch latch)
        {
            Log.Debug("Worker started to process " + sourceFiles.Count + " file(s)");
            try
            {
                fileInfoUpdateDelegatesTL.Value = new List<FileInfoUpdateDelegate>();
                try
                {
                    fileParser.PreParse();
                    DateTime start = DateTime.Now;
                    List<CountDownLatch> forkLatches = new List<CountDownLatch>();
                    try
                    {
                        for (int currentIndex = 0; currentIndex < sourceFiles.Count; currentIndex++)
                        {
                            FileInfo sourceFile = sourceFiles[currentIndex];
                            if (!fileParser.Parse(sourceFile, targetInfo))
                            {
                                Log.Debug("Skipping '" + sourceFile.FullName + "': still current");
                            }
                            CheckFork(currentIndex, start, fileParser, sourceFiles, targetInfo, forkLatches);
                        }
                        foreach (FileInfoUpdateDelegate fiuDelegate in fileInfoUpdateDelegatesTL.Value)
                        {
                            fiuDelegate();
                        }
                    }
                    finally
                    {
                        foreach (CountDownLatch forkLatch in forkLatches)
                        {
                            forkLatch.Await();
                        }
                    }
                }
                finally
                {
                    fileInfoUpdateDelegatesTL.Value = null;
                    fileParser.PostParse();
                }
            }
            finally
            {
                latch.CountDown();
                Log.Debug("Worker finished");
            }
        }

        protected void CheckFork(int currentIndex, DateTime start, IFileParser fileParser, IList<FileInfo> sourceFiles, DirectoryInfo targetInfo, IList<CountDownLatch> forkLatches)
        {
            int remainingFiles = sourceFiles.Count - currentIndex - 1;
            if (remainingFiles < 2)
            {
                // not enough data to check for a fork
                return;
            }
            DateTime curr = DateTime.Now;
            TimeSpan estimatedRemainingTime = TimeSpan.FromMilliseconds((sourceFiles.Count / (double)(currentIndex + 1)) * (curr - start).TotalMilliseconds);
            if (estimatedRemainingTime.TotalMilliseconds < 10000)
            {
                // seems to be fast enough - no fork needed
                return;
            }
            Log.Debug("Estimated time: " + (int)estimatedRemainingTime.TotalMilliseconds + "ms. Processing fork...");
            int splittedRemainingFiles = remainingFiles / 2; // split half rounded down
            List<FileInfo> forkedSourceFiles = new List<FileInfo>();
            while (splittedRemainingFiles > 0)
            {
                FileInfo forkedSourceFile = sourceFiles[sourceFiles.Count - 1];
                forkedSourceFiles.Add(forkedSourceFile);

                sourceFiles.RemoveAt(sourceFiles.Count - 1);
                splittedRemainingFiles--;
            }
            splittedRemainingFiles = forkedSourceFiles.Count;
            CountDownLatch forkLatch = new CountDownLatch(1);
            forkLatches.Add(forkLatch);
            Thread thread = new Thread(new ThreadStart(delegate()
            {
                ParseFiles(fileParser, forkedSourceFiles, targetInfo, forkLatch);
            }));
            thread.Name = fileParser.GetType().Name + "-" + forkLatches.Count;
            thread.IsBackground = true;
            thread.Start();
            Log.Debug("Forked " + splittedRemainingFiles + " item(s)");
        }

        public void RegisterFileParser(IFileParser fileParser, String fileExtensionName)
        {
            fileParsers.Register(fileParser, fileExtensionName);
        }

        public void UnregisterFileParser(IFileParser fileParser, String fileExtensionName)
        {
            fileParsers.Unregister(fileParser, fileExtensionName);
        }
    }
}
