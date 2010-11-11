/**
 * 
 */
package net.cftracker;

import coldfusion.runtime.*;

import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
/**
 * @author David Boyer
 *
 */
public class Applications {

	public Applications() {
	
	}

	/*
	 * Total number of active applications on this server / instance.
	 */
	public static int getCount() {
		Enumeration<?> appNames = ApplicationScopeTracker.getApplicationKeys();
		int count = 0;
		while (appNames.hasMoreElements()) {
			count++;
		}
		return count; 
	}

	/*
	 * Number of applications active that contain the supplied key/value within it's scope.
	 */
	public static int getCount(String key, String value) {
		int count = 0;
		ApplicationScope scope;
		Enumeration<?> apps = ApplicationScopeTracker.getApplicationKeys();
		while (apps.hasMoreElements()) {
			String appName = (String) apps.nextElement();
			scope = ApplicationScopeTracker.getApplicationScope(appName);
			if (scope != null) {
				if (scope.containsKey(key)) {
					String value2 = (String)scope.get(key);
					if (value2 == value) {
						count++;
					}
				}
			}
		}
		return count;
	}
	
	public static Struct getApps() {
		Array apps = new Array();
		ApplicationScope scope;
		Enumeration<?> appKeys = ApplicationScopeTracker.getApplicationKeys();
		while (appKeys.hasMoreElements()) {
			String appName = (String) appKeys.nextElement();
			scope = (ApplicationScope) ApplicationScopeTracker.getApplicationScope(appName);
			if (scope != null) {
				apps.add(appName);
			}
		}
		Struct result = new Struct();
		result.put("Adobe", apps);
		return result;
	}
	
	public static Struct getAppsByKey(String key, String value) {
		Array apps = new Array();
		ApplicationScope scope;
		Enumeration<?> appKeys = ApplicationScopeTracker.getApplicationKeys();
		while (appKeys.hasMoreElements()) {
			String appName = (String) appKeys.nextElement();
			scope = (ApplicationScope) ApplicationScopeTracker.getApplicationScope(appName);
			if (scope != null) {
				String value2 = (String) scope.getValueWIthoutChange(key);
				if (value2 == value) {
					// Key matches
					apps.add(appName);
				}
			}
		}
		Struct result = new Struct();
		result.put("Adobe", apps);
		return result;
	}
	
	public static ApplicationScope getScope(String appName) {
		return ApplicationScopeTracker.getApplicationScope(appName);
	}
	
	public static Array getScopeKeys(String appName) {
		ApplicationScope scope = ApplicationScopeTracker.getApplicationScope(appName);
		return Struct.StructKeyArray(scope);
	}
	
	public static Struct getSettings(String appName) {
		return (Struct) ApplicationScopeTracker.getApplicationScope(appName).getApplicationSettings();
	}
	
	public static Boolean stop(String appName) {
		ApplicationScope scope = ApplicationScopeTracker.getApplicationScope(appName);
		if (scope != null) {
			ApplicationScopeTracker.cleanUp(scope);
			return true;
		} else {
			return false;
		}
	}
	
	public static Boolean restart(String appName) {
		ApplicationScope scope = ApplicationScopeTracker.getApplicationScope(appName);
		if (scope != null) {
			scope.setIsInited(false);
			return true;
		} else {
			return false;
		}
	}
	
	public static Boolean touch(String appName) {
		ApplicationScope scope = ApplicationScopeTracker.getApplicationScope(appName);
		if (scope != null) {
			scope.setLastAccess();
			return true;
		} else {
			return false;
		}
	}
	
	public static Struct getInfo(Struct activeApps) {
		Struct results = new Struct();
		Array names = (Array) activeApps.get("Adobe");
		Iterator<?> iNames = names.valuesIterator();
		while (iNames.hasNext()) {
			String appName = (String) iNames.next();
			ApplicationScope app = ApplicationScopeTracker.getApplicationScope(appName);
			Struct info = new Struct();
			if (app == null) {
				info.put("exists",			false);
			} else {
				Long now = new Date().getTime();
				info.put("exists",			true);
				info.put("isInited",		app.isInited());
				info.put("timeAlive",		new Date(now - app.getElapsedTime()));
				info.put("lastAccessed",	new Date(now - app.getTimeSinceLastAccess()));
				info.put("idleTimeout",		new Date(now - app.getTimeSinceLastAccess() + app.getMaxInactiveInterval()));
				info.put("idleTimespan",	app.getMaxInactiveInterval());
				info.put("expired",			app.expired());
				info.put("idlePercent",		((Long) app.getTimeSinceLastAccess()).doubleValue()
											/ ((Long) app.getMaxInactiveInterval()).doubleValue()
											* 100);
				Scope sessions = SessionTracker.getSessionCollection(appName);
				info.put("sessionCount",	sessions.size());
			}
			results.put(appName, info);
		}
		return results;
	}
	
	public static Struct getAppsInfoByKey(String key, String value) {
		Struct activeApps = Applications.getAppsByKey(key, value);
		return Applications.getInfo(activeApps);
	}
	
	public static Struct getAppsInfo() {
		Struct activeApps = Applications.getApps();
		return Applications.getInfo(activeApps);
	}
	
	/*
	 * Get the time since the application was last accessed (ms)
	 */
	public static Date getLastAccessed(String appName) {
		return new Date(new Date().getTime() - ApplicationScopeTracker.getApplicationScope(appName).getTimeSinceLastAccess());
	}

	public static Date getElapsedTime(String appName) {
		return new Date(new Date().getTime() - ApplicationScopeTracker.getApplicationScope(appName).getElapsedTime());
	}

	public static Boolean getIsInited(String appName) {
		return ApplicationScopeTracker.getApplicationScope(appName).isInited();
	}

	public static Date getIdleTimeout(String appName) {
		ApplicationScope app = ApplicationScopeTracker.getApplicationScope(appName);
		return new Date(new Date().getTime() - app.getTimeSinceLastAccess() + app.getMaxInactiveInterval());
	}

	public static Long getIdleTimespan(String appName) {
		return ApplicationScopeTracker.getApplicationScope(appName).getMaxInactiveInterval();
	}
	
	public static Double getIdlePercent(String appName) {
		ApplicationScope app = ApplicationScopeTracker.getApplicationScope(appName);
		return ((Long) app.getTimeSinceLastAccess()).doubleValue()
				/ ((Long) app.getMaxInactiveInterval()).doubleValue()
				* 100;
	}
	
	public static Boolean getExpired(String appName) {
		return ApplicationScopeTracker.getApplicationScope(appName).expired();
	}
	
	public static int getSessionCount(String appName) {
		Scope sessions = SessionTracker.getSessionCollection(appName);
		return sessions.size();
	}
}