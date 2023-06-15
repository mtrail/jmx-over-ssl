#!/bin/sh

rm -rf keys
mkdir keys

# Create server certificate:
keytool -genkey -keyalg RSA -dname CN=jmxserver -validity 360 -keysize 2048 -alias jmxserver -storepass password -deststoretype pkcs12 -keystore keys/server-keystore.pkcs12
# Create client certificate:
keytool -genkey -keyalg RSA -dname CN=jmxclient -validity 360 -keysize 2048 -alias jmxclient -storepass password -deststoretype pkcs12 -keystore keys/client-keystore.pkcs12


# Client trusts server:
keytool -exportcert -alias jmxserver -storepass password -keystore keys/server-keystore.pkcs12 | keytool -importcert -alias jmxserver -storepass password -noprompt -keystore keys/client-truststore.pkcs12
# Server trusts client:
keytool -exportcert -alias jmxclient -storepass password -keystore keys/client-keystore.pkcs12 | keytool -importcert -alias jmxclient -storepass password -noprompt -keystore keys/server-truststore.pkcs12
# Server trusts itself, to connect to JMX registry:
keytool -exportcert -alias jmxserver -storepass password -keystore keys/server-keystore.pkcs12 | keytool -importcert -alias jmxserver -storepass password -noprompt -keystore keys/server-truststore.pkcs12
