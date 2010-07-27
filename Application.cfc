<cfcomponent extends="framework"><cfscript>
	this.name = 'cftracker-20100705';
	this.applicationTimeout = CreateTimeSpan(1, 0, 0, 0);
	this.sessionManagement = true;
	this.sessionTimeout = CreateTimeSpan(0, 0, 30, 0);
</cfscript>

<cffunction name="setupApplication" output="false">
	<cfset var settings = {} />
	<cfset var fake = {} />
	<cfset var temp = {} />
	<cfinclude template="config.cfm" />
	<cfset application.settings = settings />
	<cfset application.loginAttempts = 0 />
	<cfset application.loginDate = Now() />
	<cfif ReFindNoCase('^/cfide/administrator/', cgi.script_name)>
		<cfset application.cfide = true />
	<cfelse>
		<cfset application.cfide = false />
	</cfif>
	<cfif application.settings.demo>
		<cfinclude template="demodata.cfm" />
		<cfset application.data = fake />
	</cfif>
</cffunction>

<cffunction name="siPrefix" output="false" returntype="string">
	<cfargument name="value" required="true" type="numeric" />
	<cfargument name="base" required="false" type="numeric" default="1000" />
	<cfargument name="nonstandard" required="false" type="boolean" default="true" />
	<cfscript>
		var lc = {};
		if (arguments.value Eq 0) {
			return '0 ';
		}
		lc.prefixes = [];
		lc.scale = ['Y', 'Z', 'E', 'P', 'T', 'G', 'M', 'K', '', 'm', '�', 'n' ,'p', 'f', 'a' ,'z', 'y'];
		lc.power = 8;
		if (arguments.nonstandard) {
			ArrayPrepend(lc.scale, 'X');
			ArrayPrepend(lc.scale, 'W');
			ArrayPrepend(lc.scale, 'V');
			ArrayPrepend(lc.scale, 'U');
			ArrayAppend(lc.scale, 'x');
			ArrayAppend(lc.scale, 'w');
			ArrayAppend(lc.scale, 'v');
			ArrayAppend(lc.scale, 'u');
			lc.power = 12;
		}
		lc.sLen = ArrayLen(lc.scale);
		lc.finished = false;
		lc.output = '';
		for (lc.i = 1; lc.i Lte lc.sLen And Len(lc.output) Eq 0; lc.i++) {
			lc.pow = arguments.base ^ lc.power;
			if (arguments.value Gte lc.pow) {
				lc.output = NumberFormat(arguments.value / lc.pow, '.00') & ' ' & lc.scale[lc.i];
				if (arguments.base Eq '1024' And lc.pow Neq 1) {
					lc.output &= 'i';
				}
			}
			lc.power--;
		}
		if (Len(lc.output) Eq 0) {
			lc.output = arguments.value / lc.pow & ' ' & lc.scale[lc.sLen];
			if (arguments.base Eq '1024' And lc.pow Neq 1) {
				lc.output &= 'i';
			}
		}
		return lc.output;
	</cfscript>
</cffunction>

<cfscript>
	function setupSession() {
		if (Not application.cfide) {
			controller( 'security.session' );
		}
	}

	function setupRequest() {
		if (Not application.cfide) {
			controller( 'security.authorize' );
		}
	}
	
	function customizeViewOrLayoutPath( pathInfo, type, fullPath ) {
		var defaultPath = '#type#s/#pathInfo.path#.cfm';
		var skin = 'default';
		request.cfideAdminPath = '../CFIDE/administrator/';
		// Please enter your the path to cfide/administrator if CFTracker is located
		// elsewhere and you are using URL rewriting to put it in the admin
		if (application.cfide) {
			skin = 'cfide';
		}
		
		if (FileExists(ExpandPath(request.subsystembase & '/skins/' & skin & '/' & defaultPath))) {
			return request.subsystembase & 'skins/' & skin & '/' & defaultPath;
		} else {
			return request.subsystembase & 'skins/default/' & defaultPath;
		}
	}
</cfscript></cfcomponent>