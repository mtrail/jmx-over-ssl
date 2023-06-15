package jmxoverssl;

import sun.tools.jconsole.JConsole;

/**
 * Run this with the following command line options:
 * 
 * <pre>
 * -Djavax.net.ssl.keyStore=keys/client-keystore.pkcs12
 * -Djavax.net.ssl.keyStorePassword=password
 * -Djavax.net.ssl.keyStoreType=PKCS12
 * -Djavax.net.ssl.trustStore=keys/client-truststore.pkcs12
 * -Djavax.net.ssl.trustStorePassword=password
 * -Djavax.net.ssl.keyStoreType=PKCS12
 * -Djmx.remote.protocol.provider.pkgs=jmxoverssl
 * </pre>
 */
public class JConsoleLauncher {

	public static void main(String[] args) throws Exception {
		JConsole.main(args);
	}

}
