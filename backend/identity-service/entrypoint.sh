#!/bin/sh
set -eu

if [ -n "${AD_CA_CERT_PATH:-}" ]; then
    if [ ! -f "$AD_CA_CERT_PATH" ] || [ ! -r "$AD_CA_CERT_PATH" ]; then
        echo "AD_CA_CERT_PATH must point to a readable mounted certificate file." >&2
        exit 1
    fi

    CACERTS="${JAVA_HOME}/lib/security/cacerts"
    CERT_ALIAS="reboot-active-directory-ca"

    if keytool -list -keystore "$CACERTS" -storepass changeit -alias "$CERT_ALIAS" >/dev/null 2>&1; then
        keytool -delete -keystore "$CACERTS" -storepass changeit -alias "$CERT_ALIAS"
    fi

    keytool -importcert -noprompt -trustcacerts \
        -alias "$CERT_ALIAS" \
        -file "$AD_CA_CERT_PATH" \
        -keystore "$CACERTS" \
        -storepass changeit
fi

exec java -jar /app/app.jar
