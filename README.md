MailsterSMTP
============

A NIO SMTP server API written in Java

This is notably used by the Mailster project (https://github.com/edeoliveira/Mailster)

## Maven repository

```xml
<repositories>
	<repository>
		<id>bintray-edeoliveira-maven</id>
		<name>bintray</name>
		<url>http://dl.bintray.com/edeoliveira/maven</url>
	</repository>	   
</repositories>
 
<pluginRepositories>
	<pluginRepository>
		<id>bintray-edeoliveira-maven</id>
		<name>bintray-plugins</name>
		<url>http://dl.bintray.com/edeoliveira/maven</url>
	</pluginRepository>
</pluginRepositories> 
```
## Dependency

```xml
   <dependency>
        <groupId>org.mailster</groupId>
		<artifactId>smtp-core</artifactId>        
        <version>1.0.0-RC1</version>
    </dependency>
```
