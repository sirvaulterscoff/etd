# Enhanced thread dump
Oracle Java includes great utilities for getting/analyzing thread dumps, starting with jstack, ending with Java mission control.
Most of the utilities *requires* either JDK to be installed on server side, or on client side along with remote JMX connection.
When you don't have access to those utilities or you want some extended thread dump data browser-available to you - enhaced thread dump (ETD) can do this.

##Main features##
* Zero dependencies. Everything is packed in one jar file, no side dependencies
* View thread dump in your browser.
* Customized thread stack item coloring and collapsing so you only see important data
* Update inidivual thread stack
* See CPU/wait/contention time for each thread 
* Download one or several thread dumps for offline analysis [in progress]
* Information about locks, deadlocked threads [in progress]
* Reports on hot-spots - see which method occur frequently or leads to a blocked thread [in progress]
* Easy to use REST-Api for retrieving thread details

##Setup##

1. Web fragments [in progress]
##2. Manual setup##
Just drop .jar file in your project, and add following lines to your web.xml:
```xml
<servlet>
		<servlet-name>thread-dump</servlet-name>
		<servlet-class>etd.ThreadDumpServlet</servlet-class>
<servlet>
<servlet-mapping>
		<servlet-name>thread-dump</servlet-name>
		<url-pattern>/thread-dump/*</url-pattern>
</servlet-mapping>
```
Optional - define welcome file list
```xml
<welcome-file-list>
		<welcome-file>index.html</welcome-file>
</welcome-file-list>
```
Next - launch your application and navigate to http://your_application_host:port/thread-dump/ and see it in action

##Configuration##
Enahanced thread dump servlet has several parameters, which lets you:
- Define base package names. Regexps are OK. This option is used to define package pattern, so that ETD knows which stack lines belongs to your application
- Define libs packages. Regexp are OK. This option allows you to define pacakage patterns and map them to certain names like "org.springframework"=>Spring framework

Configuration is performed using init-param tag for servlet description
Configuration example
```xml
<servlet>
		<servlet-name>thread-dump</servlet-name>
		<servlet-class>etd.ThreadDumpServlet</servlet-class>
		<init-param>
			<param-name>etd_our_packages</param-name>
			<param-value>my.base.package;my.another.pacakge;</param-value>
		</init-param>
		<init-param>
			<param-name>etd_libs_mapping</param-name>
			<param-value>
				jetty=org.eclipse.jetty;
				Spring=org.springframework;
				Java core=sun.misc,^java.;
			</param-value>
		</init-param>
</servlet>
```
* *etd_our_packages* - defines application package pattern. Should follow the syntax of pacakge1,package2;
* *etd_libs_mapping* - defines library names pattern. Should follow syntax of library_name=pacakge1,package2;library_name2=pacakge3;

##Examples## [todo]

##Building##
Building is simple - download sources and run
```bash
mvn install
```
By default js/css files are minified using samaxes/minify-maven-plugin
To disable minification run
```
mvn install -Pnomini
```
Also you can wath ETD in action without putting it in your application typing
```bash
mvn jetty:run
```
and navigating to http://localhost:8080/thread-dump/
Demo also includes several threads that are either deadlocked or performing some contented operations.
