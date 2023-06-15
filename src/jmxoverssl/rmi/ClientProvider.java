package jmxoverssl.rmi;

import java.io.IOException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.util.HashMap;
import java.util.Map;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXConnectorProvider;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnector;
import javax.management.remote.rmi.RMIConnectorServer;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

/**
 * It is important that this class is in a package called "rmi", see
 * {@link JMXConnectorFactory}.
 */
public class ClientProvider implements JMXConnectorProvider {

	@Override
	public JMXConnector newJMXConnector(final JMXServiceURL serviceURL, final Map<String, ?> environment)
			throws IOException {
		return new SSLConnector(serviceURL, environment);
	}

	static class SSLConnector extends RMIConnector {

		private static final long serialVersionUID = 1L;

		public SSLConnector(final JMXServiceURL url, final Map<String, ?> environment) {
			super(url, environment);
		}

		@Override
		public synchronized void connect(final Map<String, ?> environment) throws IOException {
			HashMap<String, Object> hashMap = new HashMap<>(environment);
			hashMap.put("com.sun.jndi.rmi.factory.socket", new SslRMIClientSocketFactory());
			RMIClientSocketFactory csf = new SslRMIClientSocketFactory();
			RMIServerSocketFactory ssf = new SslRMIServerSocketFactory();

			hashMap.put(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE, csf);
			hashMap.put(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE, ssf);
			super.connect(hashMap);
		}

	}

}