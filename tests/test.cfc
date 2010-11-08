<cfcomponent displayname="MyComponentTest"  extends="base">
  <cffunction name="testAdd" access="public" returntype="void">
 
  <cfscript>
    mycomponent = createObject("component","MyComponent");
    expected = 2;
    actual = mycomponent.add(1,1);
    assertEquals(expected,actual);
  </cfscript>
 
   </cffunction>
</cfcomponent>
