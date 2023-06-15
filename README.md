# JMX over SSL

This repository contains example code and explanations about
[JMX](https://docs.oracle.com/en/java/javase/11/management/monitoring-and-management-using-jmx-technology.html)
in general as well specific considerations for usage with SSL/TLS.


## JMX over RMI protocol

The standard protocol implementation for JMX is RMI. The RMI communication is
based in TCP/IP and and requires two server side ports.

  * RMI Registry (default port 1099)
  * JMX Server (arbitrary port)

Both ports can be configured to a different fixed port. When a RMI service
(like) is started it first registers itself to the RMI registry, which may run
in the same process or in a different process.

Note that this involves a server to server connection in a first step to register
the JMX endpoint in the registry. If the registry requires authentication or SSL
certificates this becomes important.

Also the JMX service by default registers itself with the canonical host name
how the host sees itself. In an environment where ports are mapped (e.g. docker
container) the outside view may have a different host name or port. In this case
use the `java.naming.provider.url` property.


## JMX Connection URLs

The connections URLs for JMX have the following format

    service:jmx:rmi:///jndi/rmi://<host>:<port>/<servicename>

Note that the server and port refers to the RMI registry. Example:

    service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi

If like in the example above the JNDI name is `jmxrmi` (default for JMX) the URL
for the `JConsole` client can be abbreviated to 

    localhost:1099


## Creating a JMX Server Programmatically

Java comes with a out-of-the-box JMX server for the JVM, backed by the
[platform mbeanserver](https://docs.oracle.com/en/java/javase/11/docs/api/java.management/java/lang/management/ManagementFactory.html#getPlatformMBeanServer()).

You can create your own mbeanserver and setup your custom JMXConnectorServer for it.
This is demonstrated in [JmxServerProcess.java](jmx-over-ssl/src/jmxoverssl/JmxServerProcess.java).


## JMX over SSL

Setting up JMS over SSL sockets is quite tricky and requires additional configuration.
In the example in this repository we work with a self-signed certificate for the
server and another self-signed certificate for the client. To build a trust
relationship the other certificates are added to the server respectively the client
trust store. As we've seen above starting up the JMX server requires a server side
connection. Therefore the server needs to trust itself. The script
[generate_keys.sh](generate_keys.sh) creates the key and trust stores for the
client and server.

The implementation of a JMX server with SSL encryption is here [JmxServerProcessSSL.java](jmx-over-ssl/src/jmxoverssl/JmxServerProcessSSL.java)


## Connecting to a JMX Server with SSL

There is no dedicated protocol or port that denote the fact that SSL is used
to secure a connection. It is up to the client to know whether to use SSL or
plain TCP/IP sockets. Beside the ubiquitous message
"Secure connection failed. Retry insecurely?" when opening a connection with
JConsole the tools seems not to support SSL out of the box
([JDK-8020207](https://bugs.openjdk.org/browse/JDK-8020207)).

As a workaround you need to provide a custom `JMXConnectorProvider` with SSL support
like the [one in this repository](jmx-over-ssl/src/jmxoverssl/rmi/ClientProvider.java).
This implementation needs to be on the class path at a specific name (`...rmi.ClientProvider`)
and addressed with the system property `jmx.remote.protocol.provider.pkgs` set to the parent package.


## Troubeshooting

Exceptions and error messages are very confusing and often hide the actual cause.
The only chance in such cases is to debug the JMX implementation and find the
root cause this way. Here are some common issue

### Get Debug Output for JConsole

Add `-debug` as a parameter to `jconsole` to see exceptions stack traces when something
fails.

### Error message "non-JRMP server at remote endpoint"

This exception message is used, when a connection can be opened but the communication
partner does not respond with a RMI protocol header. This is typically the case
when you connect with a non SSL-enabled client (like standard JConsole) to an SSL
secured end point.

### Verifying SSL

If you're unsure whether a server actually runs SSL you can always use `openssl`
to check the presence of the secure socket layer. In this case the following
command should print the server's certificate:

     openssl s_client -connect <host>:<port>
 