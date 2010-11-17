package net.cftracker;

import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

import coldfusion.runtime.Array;
import coldfusion.runtime.Struct;

public class Threads {
	public Threads() {
		
	}
	
	public static Array getThreads() {
		ThreadMXBean threadMxBean = java.lang.management.ManagementFactory.getThreadMXBean();
		ThreadInfo[] threads = threadMxBean.getThreadInfo(threadMxBean.getAllThreadIds());
		Array result = new Array();
		for (ThreadInfo item : threads) {
			Struct info = new Struct();
			info.put("blockedCount",	item.getBlockedCount());
			if (info.get("blockedCount") == null) {
				info.put("blockedCount",	"");
			}
			info.put("blockedTime",		item.getBlockedTime());
			if (info.get("blockedTime") == null) {
				info.put("blockedTime",		0);
			}
			info.put("lockName",		item.getLockName());
			if (info.get("lockName") == null) {
				info.put("lockName",		"");
			}
			info.put("lockOwnerId",		item.getLockOwnerId());
			if (info.get("lockOwnerId") == null) {
				info.put("lockOwnerId",		0);
			}
			info.put("threadId",		item.getThreadId());
			if (info.get("threadId") == null) {
				info.put("threadId",		0);
			}
			info.put("threadName",		item.getThreadName());
			if (info.get("threadName") == null) {
				info.put("threadName",		"");
			}
			info.put("threadState",		item.getThreadState());
			if (info.get("threadState") == null) {
				info.put("threadState",		"");
			}
			info.put("waitedCount",		item.getWaitedCount());
			if (info.get("waitedCount") == null) {
				info.put("waitedCount",		0);
			}
			info.put("waitedTime",		item.getWaitedTime());
			if (info.get("waitedTime") == null) {
				info.put("waitedTime",		0);
			}
			info.put("isInNative",		item.isInNative());
			if (info.get("isInNative") == null) {
				info.put("isInNative",		false);
			}
			info.put("isSuspended",		item.isSuspended());
			if (info.get("isSuspended") == null) {
				info.put("isSuspended",		false);
			}
			info.put("cpuTime",			threadMxBean.getThreadCpuTime(item.getThreadId()));
			if (info.get("cpuTime") == null) {
				info.put("cpuTime",			0);
			}
			info.put("userTime",		threadMxBean.getThreadUserTime(item.getThreadId()));
			if (info.get("userTime") == null) {
				info.put("userTime",		0);
			}
			Array trace = new Array();
			Array cfTrace = new Array();
			StackTraceElement[] stackTrace = item.getStackTrace();
			for (StackTraceElement sItem : stackTrace) {
				Struct stInfo = new Struct();
				stInfo.put("className",		sItem.getClassName());
				stInfo.put("fileName",		sItem.getFileName());
				stInfo.put("lineNumber",	sItem.getLineNumber());
				stInfo.put("methodName",	sItem.getMethodName());
				stInfo.put("nativeMethod",	sItem.isNativeMethod());
				if (stInfo.get("fileName") == null) {
					stInfo.put("fileName",	"");
				}
				trace.add(stInfo);
				if (((String) stInfo.get("fileName")).matches("\\.(cfc|CFC|cfm|CFM)$")) {
					cfTrace.add(sItem.getFileName() + "@" + Integer.toString(sItem.getLineNumber()));
				}
			}
			info.put("stackTrace",	trace);
			info.put("cfTrace",		cfTrace);
			result.add(info);
		}
		return result;
	}
	
	public static Struct getCountPerGroup() {
		Struct result = new Struct();
		java.lang.Thread currentThread = java.lang.Thread.currentThread();
		ThreadGroup tGroup = currentThread.getThreadGroup();
		while (tGroup.getParent() != null) {
			tGroup = tGroup.getParent();
		}
		ThreadGroup[] groups = (ThreadGroup[]) java.lang.reflect.Array.newInstance(tGroup.getClass(), tGroup.activeGroupCount());
		tGroup.enumerate(groups);
		for (ThreadGroup gItem : groups) {
			result.put(gItem.getName(), gItem.activeCount());
		}
		return result;
	}
}
