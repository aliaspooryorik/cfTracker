package net.cftracker;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.CompilationMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import com.sun.management.GcInfo;

import coldfusion.runtime.Array;
import coldfusion.runtime.Struct;
import coldfusion.server.j2ee.sql.pool.JDBCManager;
import coldfusion.server.j2ee.sql.pool.JDBCPool;
import coldfusion.server.j2ee.sql.pool.JDBCPoolMetaData;

@SuppressWarnings("restriction")
public class Stats {
	public Stats() {
		
	}
	
	public static long getCompilationTime() {
		CompilationMXBean compBean = java.lang.management.ManagementFactory.getCompilationMXBean();
		if (compBean.isCompilationTimeMonitoringSupported()) {
			return compBean.getTotalCompilationTime();
		}
		return 0;
	}
	
	public static Struct getClassLoading() {
		ClassLoadingMXBean classBean = java.lang.management.ManagementFactory.getClassLoadingMXBean();
		Struct result = new Struct();
		result.put("current",	classBean.getLoadedClassCount());
		result.put("total",		classBean.getTotalLoadedClassCount());
		result.put("unloaded",	classBean.getUnloadedClassCount());
		return result;
	}
	
	public static long getProcessCpuTime() {
		try {
			com.sun.management.OperatingSystemMXBean bean = (com.sun.management.OperatingSystemMXBean) java.lang.management.ManagementFactory.getOperatingSystemMXBean();
			return bean.getProcessCpuTime();
		} catch (Exception e) {
			return 0;
		}
	}
	
	public static Struct getMemoryOs() {
		Struct result = new Struct();
		try {
			com.sun.management.OperatingSystemMXBean bean = (com.sun.management.OperatingSystemMXBean) java.lang.management.ManagementFactory.getOperatingSystemMXBean();
			result.put("vmCommitted",	bean.getCommittedVirtualMemorySize());
			result.put("phyiscalFree",	bean.getFreePhysicalMemorySize());
			result.put("physicalTotal",	bean.getTotalPhysicalMemorySize());
			result.put("phyiscalUsed",	bean.getTotalPhysicalMemorySize() - bean.getFreePhysicalMemorySize());
			result.put("swapFree",		bean.getFreeSwapSpaceSize());
			result.put("swapTotal",		bean.getTotalSwapSpaceSize());
			result.put("swapUsed",		bean.getTotalSwapSpaceSize() - bean.getFreeSwapSpaceSize());
		} catch (Exception e) {
			result.put("vmCommitted",	0);
			result.put("phyiscalFree",	0);
			result.put("physicalTotal",	0);
			result.put("phyiscalUsed",	0);
			result.put("swapFree",		0);
			result.put("swapTotal",		0);
			result.put("swapUsed",		0);
		}
		return result;
	}
	
	public static Struct getMemoryJvm() {
		MemoryMXBean memBean = java.lang.management.ManagementFactory.getMemoryMXBean();
		List<MemoryPoolMXBean> poolBean = java.lang.management.ManagementFactory.getMemoryPoolMXBeans();
		MemoryUsage memUsage = null;
		memUsage = memBean.getHeapMemoryUsage();
		Struct hUsage = new Struct();
		hUsage.put("committed",		0);
		hUsage.put("initial",		memUsage.getInit());
		hUsage.put("max",			memUsage.getMax());
		hUsage.put("used",			0);
		hUsage.put("free",			0);
		Struct hpUsage = new Struct();
		hpUsage.put("committed",		0);
		hpUsage.put("initial",		memUsage.getInit());
		hpUsage.put("max",			memUsage.getMax());
		hpUsage.put("used",			0);
		hpUsage.put("free",			0);
		memUsage = memBean.getNonHeapMemoryUsage();
		Struct nhUsage = new Struct();
		nhUsage.put("committed",		0);
		nhUsage.put("initial",		memUsage.getInit());
		nhUsage.put("max",			memUsage.getMax());
		nhUsage.put("used",			0);
		nhUsage.put("free",			0);
		Struct nhpUsage = new Struct();
		nhpUsage.put("committed",		0);
		nhpUsage.put("initial",		memUsage.getInit());
		nhpUsage.put("max",			memUsage.getMax());
		nhpUsage.put("used",			0);
		nhpUsage.put("free",			0);
		// Pools
		Struct hPools = new Struct();
		Struct nhPools = new Struct();
		Iterator<MemoryPoolMXBean> iPool = poolBean.iterator();
		while (iPool.hasNext()) {
			MemoryPoolMXBean pool = iPool.next();
			memUsage = pool.getUsage();
			Struct infoUsage = new Struct();
			infoUsage.put("committed",	memUsage.getCommitted());
			infoUsage.put("initial",	memUsage.getInit());
			infoUsage.put("used",		memUsage.getUsed());
			infoUsage.put("max",		memUsage.getMax());
			infoUsage.put("free",		memUsage.getMax() - memUsage.getUsed());
			memUsage = pool.getPeakUsage();
			Struct infoPUsage = new Struct();
			infoPUsage.put("committed",	memUsage.getCommitted());
			infoPUsage.put("initial",	memUsage.getInit());
			infoPUsage.put("used",		memUsage.getUsed());
			infoPUsage.put("max",		memUsage.getMax());
			infoPUsage.put("free",		memUsage.getMax() - memUsage.getUsed());
			Struct stPool = new Struct();
			stPool.put("name",				pool.getName());
			stPool.put("usage",				infoUsage);
			stPool.put("peakUsage",			infoPUsage);
			stPool.put("garbageCollectors",	pool.getMemoryManagerNames());
			if (pool.getType().toString() == "Heap memory") {
				hPools.put(pool.getName(), stPool);
				hUsage.put("committed", 	(Long) hpUsage.get("committed") + (Long) infoUsage.get("committed"));
				hUsage.put("used", 			(Long) hpUsage.get("used") + (Long) infoUsage.get("used"));
				hUsage.put("free", 			(Long) hpUsage.get("free") + (Long) infoUsage.get("free"));
				hpUsage.put("committed", 	(Long) hpUsage.get("committed") + (Long) infoPUsage.get("committed"));
				hpUsage.put("used", 		(Long) hpUsage.get("used") + (Long) infoPUsage.get("used"));
				hpUsage.put("free", 		(Long) hpUsage.get("free") + (Long) infoPUsage.get("free"));
			} else {
				nhPools.put(pool.getName(), stPool);
				nhUsage.put("committed", 	(Long) nhpUsage.get("committed") + (Long) infoUsage.get("committed"));
				nhUsage.put("used", 		(Long) nhpUsage.get("used") + (Long) infoUsage.get("used"));
				nhUsage.put("free", 		(Long) nhpUsage.get("free") + (Long) infoUsage.get("free"));
				nhpUsage.put("committed", 	(Long) nhpUsage.get("committed") + (Long) infoPUsage.get("committed"));
				nhpUsage.put("used", 		(Long) nhpUsage.get("used") + (Long) infoPUsage.get("used"));
				nhpUsage.put("free", 		(Long) nhpUsage.get("free") + (Long) infoPUsage.get("free"));
			}
		}
		Struct result = new Struct();
		Struct heap = new Struct();
		Struct nHeap = new Struct();
		heap.put("usage",		hUsage);
		heap.put("peakUsage",	hpUsage);
		heap.put("pools",		hPools);
		nHeap.put("usage",		nhUsage);
		nHeap.put("peakUsage",	nhpUsage);
		nHeap.put("pools",		nhPools);
		result.put("heap",		heap);
		result.put("nonHeap",	nHeap);
		return result;
	}
	
	public static Array getGarbage() {
		Array result = new Array();
		Iterator<GarbageCollectorMXBean> iBin = java.lang.management.ManagementFactory.getGarbageCollectorMXBeans().iterator();
		while (iBin.hasNext()) {
			Struct stInfo = new Struct();
			GarbageCollectorMXBean bin = iBin.next();
			try {
				com.sun.management.GarbageCollectorMXBean bigBin = (com.sun.management.GarbageCollectorMXBean) bin;
				GcInfo lastGc = bigBin.getLastGcInfo();
				Struct snapshots = new Struct();
				MemoryUsage before = (MemoryUsage) lastGc.getMemoryUsageBeforeGc();
				MemoryUsage after = (MemoryUsage) lastGc.getMemoryUsageAfterGc();
				snapshots.put("before",	before);
				snapshots.put("after",	after);

				stInfo.put("startTime",	new Date(lastGc.getStartTime()));
				stInfo.put("duration",	lastGc.getDuration());
				stInfo.put("endTime",	new Date(lastGc.getEndTime()));
				
				Struct usage = new Struct();
				String[] poolNames = bin.getMemoryPoolNames();
				String[] whens = {"before", "after"};
				for (String pName : poolNames) {
					Struct sUsage = new Struct();
					for (String when : whens) {
						Struct poolUsage = new Struct();
						poolUsage.put("committed",	((MemoryUsage) snapshots.get(when)).getCommitted());
						poolUsage.put("initial",	((MemoryUsage) snapshots.get(when)).getInit());
						poolUsage.put("used",		((MemoryUsage) snapshots.get(when)).getUsed());
						poolUsage.put("max",		((MemoryUsage) snapshots.get(when)).getMax());
						poolUsage.put("free",		(Long) poolUsage.get("max") - (Long) poolUsage.get("used"));
						sUsage.put(when, poolUsage);
					}
					usage.put(pName, sUsage);
				}
				stInfo.put("usage", usage);
			} catch (Exception e) {
			}
			stInfo.put("collections",	bin.getCollectionCount());
			stInfo.put("totalDuration",	bin.getCollectionTime());
			stInfo.put("name",			bin.getName());
			stInfo.put("pools",			bin.getMemoryPoolNames());
			stInfo.put("valid",			bin.isValid());
			result.add(stInfo);
		}
		return result;
	}
	
	public static Struct getJdbcStats() {
		Enumeration<?> pools = JDBCManager.getInstance().getPools();
		Struct result = new Struct();
		while (pools.hasMoreElements()) {
			JDBCPool pool = (JDBCPool) pools.nextElement();
			JDBCPoolMetaData meta = pool.getJDBCPoolMetaData();
			Struct info = new Struct();
			info.put("open",		pool.getCheckedOutCount());
			info.put("total",		pool.getPoolCount());
			info.put("database",	meta.getDbname());
			info.put("description",	meta.getDescription());
			result.put(pool.getPoolname(), info);
		}
		return result;
	}
	
	public static Date getUptime() {
		return new Date(ManagementFactory.getRuntimeMXBean().getStartTime());
	}
	
	public static 
}
