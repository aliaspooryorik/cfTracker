package net.cftracker;

import coldfusion.runtime.*;

import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;

public class Sessions {
	public Sessions() {
		
	}
	
	/*
		Number of sessions active across the whole server
	*/
	public static int getCount() {
		return SessionTracker.getSessionCount();
	}

	/*
		Number of sessions active for the specified application
	*/
	public static int getCount(String appName) {
		Scope sessions = SessionTracker.getSessionCollection(appName);
		return sessions.size();
	}
	
	/*
		Number of sessions active for applications with the specified scope key/value
	*/
	public static int getCount(String key, String value) {
		int count = 0;
		ApplicationScope scope;
		Enumeration<?> apps = ApplicationScopeTracker.getApplicationKeys();
		while (apps.hasMoreElements()) {
			String appName = (String)apps.nextElement();
			scope = ApplicationScopeTracker.getApplicationScope(appName);
			if (scope != null) {
				if (scope.containsKey(key)) {
					String value2 = (String)scope.get(key);
					if (value2 == value) {
						Scope sessions = SessionTracker.getSessionCollection(appName);
						count += sessions.size();
					}
				}
			}
		}
		return count;
	}
	
	public static Struct getSessions() {
		Struct apps = new Struct();
		ApplicationScope scope;
		Enumeration<?> appKeys = ApplicationScopeTracker.getApplicationKeys();
		while (appKeys.hasMoreElements()) {
			String appName = (String) appKeys.nextElement();
			scope = (ApplicationScope) ApplicationScopeTracker.getApplicationScope(appName);
			if (scope != null) {
				Scope sessions = SessionTracker.getSessionCollection(appName);
				apps.put(appName, Struct.StructKeyArray(sessions));
			}
		}
		Struct result = new Struct();
		result.put("Adobe", apps);
		return result;
	}

	public static Struct getSessions(String appName) {
		Struct apps = new Struct();
		Scope sessions = SessionTracker.getSessionCollection(appName);
		apps.put(appName, Struct.StructKeyArray(sessions));
		Struct result = new Struct();
		result.put("Adobe", apps);
		return result;
	}

	public static Struct getSessions(String key, String value) {
		Struct apps = new Struct();
		ApplicationScope scope;
		Enumeration<?> appKeys = ApplicationScopeTracker.getApplicationKeys();
		while (appKeys.hasMoreElements()) {
			String appName = (String) appKeys.nextElement();
			scope = (ApplicationScope) ApplicationScopeTracker.getApplicationScope(appName);
			if (scope != null) {
				String value2 = (String) scope.getValueWIthoutChange(key);
				if (value2 == value) {
					// Key matches
					Scope sessions = SessionTracker.getSessionCollection(appName);
					apps.put(appName, Struct.StructKeyArray(sessions));
				}
			}
		}
		Struct result = new Struct();
		result.put("Adobe", apps);
		return result;
	}

	public static SessionScope getScope(String sessionId) {
		return SessionTracker.getSession(sessionId);
	}
	
	public static Array getScopeKeys(String sessionId) {
		Scope scope = SessionTracker.getSession(sessionId);
		return Struct.StructKeyArray(scope);
	}
	
	/*
	public static SessionScope getScopeValues(String sessionId, Array keys) {
		Scope scope = SessionTracker.getSession(sessionId);
		Struct result = new Struct();
		
	}
	*/
	
	public static Boolean stop(String sessionId) {
		SessionScope scope = Sessions.getScope(sessionId);
		if (scope != null) {
			String appName = sessionId.replaceAll("^(.*)_[^_]+_[^_]+$", "\1");
			String sid = sessionId.replaceAll("^.*([^_]+_[^_]+)$", "\1");
			ApplicationScope appScope = ApplicationScopeTracker.getApplicationScope(appName);
			if (appScope != null) {
				Object[] args = new Object[] {scope, appScope};
				try {
					appScope.getEventInvoker().onSessionEnd(args);
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			SessionTracker.cleanUp(appName, sid);
			return true;
		} else {
			return false;
		}
	}
	
	public static Boolean stopByApp(String appName) {
		Array sessions = Struct.StructKeyArray(SessionTracker.getSessionCollection(appName));
		Iterator<?> iSess = sessions.valuesIterator();
		Boolean result = false;
		while (iSess.hasNext()) {
			String sessionId = (String) iSess.next();
			result = result || Sessions.stop(sessionId);
		}
		return result;
	}
	
	public static Boolean touch(String sessionId) {
		SessionScope scope = Sessions.getScope(sessionId);
		if (scope != null) {
			scope.setLastAccess();
			return true;
		} else {
			return false;
		}
	}
	
	public static Struct getInfo() {
		Struct activeSessions = Sessions.getSessions();
		return Sessions.getInfo(activeSessions);
	}
	
	public static Struct getInfo(String sessionId) {
		String appName = sessionId.replaceAll("^(.*)_[^_]+_[^_]+$", "\1");
		String sid = sessionId.replaceAll("^.*([^_]+_[^_]+)$", "\1");
		Array session = new Array();
		session.add(sid);
		Struct app = new Struct();
		app.put(appName, session);
		Struct activeSession = new Struct();
		activeSession.put("Adobe", app);
		return Sessions.getInfo(activeSession);
	}
	
	public static Struct getInfoByApp(String appName) {
		Struct activeSessions = Sessions.getSessions(appName);
		return Sessions.getInfo(activeSessions);
	}
	
	public static Struct getInfoByAppKey(String key, String value) {
		Struct activeSessions = Sessions.getSessions(key, value);
		return Sessions.getInfo(activeSessions);
	}
	
	public static Struct getInfo(Struct activeSessions) {
		Struct appInfo = new Struct();
		Struct wc = (Struct) activeSessions.get("Adobe");
		Iterator<?> iApps = wc.keySet().iterator();
		while (iApps.hasNext()) {
			String appName = (String) iApps.next();
			Array sessionIds = (Array) wc.get(appName);
			if (sessionIds != null) {
				Struct sessionsInfo = new Struct();
				Iterator<?> iSess = sessionIds.valuesIterator();
				while (iSess.hasNext()) {
					String sessId = (String) iSess.next();
					SessionScope scope = SessionTracker.getSession(sessId);
					Struct info = new Struct();
					if (scope == null) {
						info.put("exists",			false);
					} else {
						Long now = new Date().getTime();
						info.put("exists",			true);
						info.put("isNew",			scope.isNew());
						info.put("idFromURL",		scope.isIdFromURL());
						info.put("isJ2eeSession",	scope.IsJ2eeSession());
						info.put("clientIp",		scope.getClientIp());
						info.put("timeAlive",		new Date(now - scope.getElapsedTime()));
						info.put("lastAccessed",	new Date(now - scope.getTimeSinceLastAccess()));
						info.put("idleTimeout",		new Date(now - scope.getTimeSinceLastAccess() + scope.getMaxInactiveInterval() * 1000));
						info.put("idleTimespan",	scope.getMaxInactiveInterval() * 1000);
						info.put("expired",			scope.expired());
						info.put("idlePercent",		((Long) scope.getTimeSinceLastAccess()).doubleValue()
													/ (((Long) scope.getMaxInactiveInterval()).doubleValue() * 1000)
													* 100);
					}
					sessionsInfo.put(sessId, info);
				}
				appInfo.put(appName, sessionsInfo);
			}
		}
		Struct results = new Struct();
		results.put("Adobe", appInfo);
		return results;
	}
	
	public static Date getTimeAlive(String sessionId) {
		return new Date(new Date().getTime() - Sessions.getScope(sessionId).getElapsedTime());
	}
	
	public static Date getLastAccessed(String sessionId) {
		return new Date(new Date().getTime() - Sessions.getScope(sessionId).getTimeSinceLastAccess());
	}

	public static Date getIdleTimeout(String sessionId) {
		SessionScope scope = Sessions.getScope(sessionId);
		return new Date(new Date().getTime() - scope.getTimeSinceLastAccess() + scope.getMaxInactiveInterval() * 1000);
	}
	
	public static Long getIdleTimespan(String sessionId) {
		return Sessions.getScope(sessionId).getMaxInactiveInterval() * 1000;
	}
	
	public static Boolean getExpired(String sessionId) {
		return Sessions.getScope(sessionId).expired();
	}
	
	public static Double getIdlePercent(String sessionId) {
		SessionScope scope = Sessions.getScope(sessionId);
		return (((Long) scope.getTimeSinceLastAccess()).doubleValue()
				/ (((Long) scope.getMaxInactiveInterval()).doubleValue() * 1000)
				* 100);
	}
	
	public static String getClientIp(String sessionId) {
		return Sessions.getScope(sessionId).getClientIp();
	}
	
	public static Boolean getIsNew(String sessionId) {
		return Sessions.getScope(sessionId).isNew();
	}
	
	public static Boolean getIsIdFromUrl(String sessionId) {
		return Sessions.getScope(sessionId).isIdFromURL();
	}
	
	public static Boolean getIsJ2eeSession(String sessionId) {
		return Sessions.getScope(sessionId).IsJ2eeSession();
	}
}