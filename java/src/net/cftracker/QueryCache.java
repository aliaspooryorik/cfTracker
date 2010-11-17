package net.cftracker;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import coldfusion.monitor.sql.QueryStat;
import coldfusion.runtime.Array;
import coldfusion.runtime.Struct;
import coldfusion.server.ServiceFactory;
import coldfusion.sql.CachedQuery;
import coldfusion.sql.InParameter;
import coldfusion.sql.ParameterList;
import coldfusion.sql.Table;

public class QueryCache {
	
	public QueryCache() {
		
	}
	
	public static Struct getQueries() {
		List<?> queries = ServiceFactory.getDataSourceService().getCachedQueries();
		Iterator<?> iQuery = queries.iterator();
		Struct result = new Struct();
		while (iQuery.hasNext()) {
			CachedQuery query = (CachedQuery) iQuery.next();
			result.put(query.hashCode(), QueryCache.getInfo(query));
		}
		return result;
	}
	
	public static Struct getQueries(int start, int amount) {
		return QueryCache.getQueries(start, amount, "data");
	}
	
	public static Struct getQueries(int start, int amount, String dataName) {
		return null;
	}
	
	public static CachedQuery getQuery(int hashCode) {
		List<?> queries = ServiceFactory.getDataSourceService().getCachedQueries();
		CachedQuery query = null;
		Iterator<?> iQuery = queries.iterator();
		while (iQuery.hasNext()) {
			query = (CachedQuery) iQuery.next();
			if (query.hashCode() == hashCode) {
				return query;
			}
		}
		return query;
	}
	
	public static Table getResult(int hashCode) {
		CachedQuery query = QueryCache.getQuery(hashCode);
		if (query != null) {
			return query.getResult();
		}
		return null;
	}
	
	public static Struct getInfo(int hashCode) {
		CachedQuery query = QueryCache.getQuery(hashCode);
		if (query != null) {
			return QueryCache.getInfo(query);
		}
		return new Struct();
	}
	
	public static boolean purge(int hashCode) {
		CachedQuery query = QueryCache.getQuery(hashCode);
		if (query != null) {
			ServiceFactory.getDataSourceService().removeCachedQuery(query.getKey());
			return true;
		}
		return false;
	}
	
	public static boolean purge() {
		ServiceFactory.getDataSourceService().purgeQueryCache();
		return true;
	}
	
	public static int getCount() {
		return ServiceFactory.getDataSourceService().getCachedQueries().size();
	}
	
	public static double getHitRatio() {
		return ServiceFactory.getDataSourceService().getCacheHitRatio();
	}
	
	public static int getMaxCount() {
		return ServiceFactory.getDataSourceService().getMaxQueryCount().intValue();
	}
	
	public static boolean refresh(int hashCode) {
		CachedQuery query = QueryCache.getQuery(hashCode);
		if (query != null) {
			query.refresh();
		}
		return false;
	}
	
	public static Struct getInfo(CachedQuery query) {
		Struct result = new Struct();
		result.put("creation",	new Date(query.getCreationTime()));
		result.put("dsn",		query.getKey().getDsname());
		result.put("queryName", query.getKey().getName());
		result.put("sql",		query.getKey().getSql());
		result.put("hashcode",	query.hashCode());
		Array aParams = new Array();
		ParameterList params = query.getKey().getParamList();
		if (params != null) {
			Iterator<?> iParam = params.getAllParameters().iterator();
			while (iParam.hasNext()) {
				InParameter param = (InParameter) iParam.next();
				Struct stParam = new Struct();
				stParam.put("scale",		param.getScale());
				stParam.put("type",			param.getSqltypeName());
				stParam.put("statement",	param.getStatement());
				stParam.put("value",		param.getObject().toString());
				aParams.add(stParam);
			}
		}
		result.put("params",	aParams);
		QueryStat stats = query.getStats();
		if (stats != null) {
			result.put("executionCount",	stats.getExecutionCount());
			result.put("executionTime",		stats.getExecutionTime());
			result.put("functionName",		stats.getFunctionName());
			result.put("hitCount",			stats.getHitCount());
			result.put("lineNo",			stats.getLineNo());
			result.put("size",				stats.getSize());
			result.put("templatePath",		stats.getTemplatePath());
			result.put("isCached",			stats.isCached());
			result.put("isStored",			stats.isStored());
		}
		return result;
	}
}
