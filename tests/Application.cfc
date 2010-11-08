<cfcomponent output="false">
	<cfscript>
		this.name = 'CfTracker-Tests';
		this.applicationTimeout = CreateTimeSpan(0, 0, 30, 0);
		this.clientManagement = false;
		this.sessionManagement = false;
		this.base = GetDirectoryFromPath(GetCurrentTemplatePath());
		this.mappings['/mxunit'] = this.base & '../libraries/mxunit';
	</cfscript>

	<cffunction name="onError" returnType="void" output="false">
		<cfargument name="exception" required="true" />
		<cfargument name="eventname" type="string" required="true" />
		<cfdump var="#arguments#" /><cfabort />
	</cffunction>
</cfcomponent>