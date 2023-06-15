package jmxoverssl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.HashMap;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnectorServer;
import javax.naming.Context;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

/**
 * Confirm that the ports actually use SSL:
 * 
 * <pre>
 * openssl s_client -connect localhost:1099
 * openssl s_client -connect localhost:2099
 * </pre>
 * 
 * Connection URLs:
 * 
 * <pre>
 * service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi
 * localhost:1099
 * </pre>
 */
public class JmxServerProcessSSL {

	String rmiHost;
	int rmiRegistryPort = 1099;
	int rmiDataPort = 2099;
	String rmiName = "jmxrmi";

	private SslRMIClientSocketFactory clientSocketFactory;
	private SslRMIServerSocketFactory serverSocketFactory;

	JmxServerProcessSSL() throws UnknownHostException {
		rmiHost = InetAddress.getLocalHost().getCanonicalHostName();
	}

	void start() throws IOException {
		createSSLContexts();
		startRegistry();
		var mbeanserver = createMBeanServer();
		startRmiServer(mbeanserver);
	}

	void createSSLContexts() {
		// TODO: Create SSLContext instead of using global system properties
		System.setProperty("javax.net.ssl.keyStore", "keys/server-keystore.pkcs12");
		System.setProperty("javax.net.ssl.keyStorePassword", "password");
		System.setProperty("javax.net.ssl.trustStore", "keys/server-truststore.pkcs12");
		System.setProperty("javax.net.ssl.trustStorePassword", "password");

		clientSocketFactory = new SslRMIClientSocketFactory();
		serverSocketFactory = new SslRMIServerSocketFactory();
	}

	void startRegistry() throws RemoteException {
		LocateRegistry.createRegistry(rmiRegistryPort, clientSocketFactory, serverSocketFactory);
		System.out.println("Started RMI Registry at " + rmiHost + ":" + rmiRegistryPort);
	}

	MBeanServer createMBeanServer() {
		// Empty Server is enough, it will already contain the MBeanServerDelegate Bean
		return MBeanServerFactory.newMBeanServer();
	}

	void startRmiServer(MBeanServer mbeanserver) throws IOException {

		System.setProperty("com.sun.management.jmxremote.ssl", "true");
		System.setProperty("com.sun.management.jmxremote.registry.ssl", "true");

		var env = new HashMap<String, Object>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.rmi.registry.RegistryContextFactory");
		env.put(Context.PROVIDER_URL, "rmi://" + rmiHost + ":" + rmiRegistryPort);
		env.put(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE, clientSocketFactory);
		env.put(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE, serverSocketFactory);
		// We need to also set this key to enable SSL for the RegistryContextFactory
		env.put("com.sun.jndi.rmi.factory.socket", clientSocketFactory);

		var rmiAddress = new JMXServiceURL("rmi", "localhost", rmiDataPort, "/jndi/" + rmiName);
		var rmiServer = JMXConnectorServerFactory.newJMXConnectorServer(rmiAddress, env, mbeanserver);
		rmiServer.start();
		System.out.println("Started RMI Server at " + rmiHost + ":" + rmiDataPort);
	}

	public static void main(String[] args) throws InterruptedException, IOException {
		var proc = new JmxServerProcessSSL();
		proc.start();

		// Keep the process running.
		while (true) {
			Thread.sleep(1000);
		}
	}

}
