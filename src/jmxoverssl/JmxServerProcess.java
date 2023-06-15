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
import javax.naming.Context;

/**
 * Connection URLs:
 * 
 * <pre>
 * service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi
 * localhost:1099
 * </pre>
 */
public class JmxServerProcess {

	String rmiHost;
	int rmiRegistryPort = 1099;
	int rmiDataPort = 2099;
	String rmiName = "jmxrmi";

	JmxServerProcess() throws UnknownHostException {
		rmiHost = InetAddress.getLocalHost().getCanonicalHostName();
	}

	void start() throws IOException {
		startRegistry();
		var mbeanserver = createMBeanServer();
		startRmiServer(mbeanserver);
	}

	void startRegistry() throws RemoteException {
		LocateRegistry.createRegistry(rmiRegistryPort);
		System.out.println("Started RMI Registry at " + rmiHost + ":" + rmiRegistryPort);
	}

	MBeanServer createMBeanServer() {
		// Empty Server is enough, it will already contain the MBeanServerDelegate Bean
		return MBeanServerFactory.newMBeanServer();
	}

	void startRmiServer(MBeanServer mbeanserver) throws IOException {
		var env = new HashMap<String, Object>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.rmi.registry.RegistryContextFactory");
		env.put(Context.PROVIDER_URL, "rmi://" + rmiHost + ":" + rmiRegistryPort);

		var rmiAddress = new JMXServiceURL("rmi", "localhost", rmiDataPort, "/jndi/" + rmiName);
		var rmiServer = JMXConnectorServerFactory.newJMXConnectorServer(rmiAddress, env, mbeanserver);
		rmiServer.start();
		System.out.println("Started RMI Server at " + rmiHost + ":" + rmiDataPort);
	}

	public static void main(String[] args) throws InterruptedException, IOException {
		var proc = new JmxServerProcess();
		proc.start();

		// Keep the process running.
		while (true) {
			Thread.sleep(1000);
		}
	}

}
