package com.koch.ambeth.util.threading;

/*-
 * #%L
 * jambeth-util
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ProcessIdHelper
{
	private static final Pattern cpuUsagePattern = Pattern.compile(" ");

	private static final Pattern hzPattern = Pattern.compile(".*#define *HZ *(\\d+).*", Pattern.DOTALL);

	private ProcessIdHelper()
	{
		// Intended blank
	}

	public static void main(String[] args)
	{
		long startCpuUsage = getCumulatedCpuUsage();
		int a = 1;
		long start = System.currentTimeMillis();
		while (System.currentTimeMillis() - start < 10000)
		{
			for (int b = 100000; b-- > 0;)
			{
				a = a + 1;
			}
		}
		long endCpuUsage = getCumulatedCpuUsage();
		System.out.println((endCpuUsage - startCpuUsage));
	}

	public static String getProcessId()
	{
		// Note: may fail in some JVM implementations
		// therefore fallback has to be provided

		// something like '<pid>@<hostname>', at least in SUN / Oracle JVMs
		final String jvmName = ManagementFactory.getRuntimeMXBean().getName();
		final int index = jvmName.indexOf('@');

		if (index < 1)
		{
			// part before '@' empty (index = 0) / '@' not found (index = -1)
			return "0";
		}

		try
		{
			return jvmName.substring(0, index);
		}
		catch (NumberFormatException e)
		{
			return "0";
		}
	}

	public static long getCumulatedCpuUsage()
	{
		byte[] statBytes;
		byte[] hzBytes;
		try
		{
			InputStream is = new FileInputStream("/proc/self/stat");
			try
			{
				statBytes = new byte[256];
				is.read(statBytes);
			}
			finally
			{
				is.close();
			}
			is = new FileInputStream("/usr/include/asm-generic/param.h");
			try
			{
				hzBytes = new byte[256];
				is.read(hzBytes);
			}
			finally
			{
				is.close();
			}
		}
		catch (IOException e)
		{
			return -1;
		}
		String stat = new String(statBytes, Charset.forName("UTF-8"));
		String hzFileContent = new String(hzBytes, Charset.forName("UTF-8"));

		Matcher hzMatcher = hzPattern.matcher(hzFileContent);
		if (!hzMatcher.matches())
		{
			return -1;
		}
		double hzValue = Double.parseDouble(hzMatcher.group(1));
		String[] statValues = cpuUsagePattern.split(stat);

		long userModeCpuTime = (long) (Double.parseDouble(statValues[13]) / hzValue * 1000);
		long systemModeCpuTime = (long) (Double.parseDouble(statValues[14]) / hzValue * 1000);

		return userModeCpuTime + systemModeCpuTime;
		// res = sprintf(buffer, "%d (%s) %c %d %d %d %d %d %u %lu \
		// %lu %lu %lu %lu %lu %ld %ld %ld %ld %d 0 %llu %lu %ld %lu %lu %lu %lu %lu \
		// %lu %lu %lu %lu %lu %lu %lu %lu %d %d %u %u %llu\n",
		// task->pid,
		// tcomm,
		// state,
		// ppid,
		// pgid,
		// sid,
		// tty_nr,
		// tty_pgrp,
		// task->flags,
		// min_flt,
		// cmin_flt,
		// maj_flt,
		// cmaj_flt,
		// cputime_to_clock_t(utime),
		// cputime_to_clock_t(stime),
		// cputime_to_clock_t(cutime),
		// cputime_to_clock_t(cstime),
		// priority,
		// nice,
		// num_threads,
		// start_time,
		// vsize,
		// mm ? get_mm_rss(mm) : 0,
		// rsslim,
		// mm ? mm->start_code : 0,
		// mm ? mm->end_code : 0,
		// mm ? mm->start_stack : 0,
		// esp,
		// eip,
		// /* The signal information here is obsolete.
		// * It must be decimal for Linux 2.0 compatibility.
		// * Use /proc/#/status for real-time signals.
		// */
		// task->pending.signal.sig[0] & 0x7fffffffUL,
		// task->blocked.sig[0] & 0x7fffffffUL,
		// sigign .sig[0] & 0x7fffffffUL,
		// sigcatch .sig[0] & 0x7fffffffUL,
		// wchan,
		// 0UL,
		// 0UL,
		// task->exit_signal,
		// task_cpu(task),
		// task->rt_priority,
		// task->policy,
		// (unsigned long long)delayacct_blkio_ticks(task));
		// if (mm)
		// mmput(mm);
	}
}
