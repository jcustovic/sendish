package com.sendish.push.notification;

import com.notnoop.apns.ApnsServiceBuilder;
import com.notnoop.exceptions.InvalidSSLConfig;
import org.springframework.util.Assert;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;

public class JKSApnsServiceBuilder extends ApnsServiceBuilder {

    private static final String KEYSTORE_TYPE = "JKS";
    private static final String KEY_ALGORITHM = "sunx509";

    @Override
    public final ApnsServiceBuilder withCert(final InputStream p_stream, final String p_password) {
        Assert.notNull(p_password, "Passwords must be specified. Oracle Java SDK does not support passwordless p12 certificates");

        return withSSLContext(newSSLContext(p_stream, p_password, KEYSTORE_TYPE, KEY_ALGORITHM));
    }

    @Override
    public final ApnsServiceBuilder withCert(final KeyStore p_keyStore, final String p_password) {
        Assert.notNull(p_password, "Passwords must be specified. Oracle Java SDK does not support passwordless p12 certificates");

        return withSSLContext(newSSLContext(p_keyStore, p_password, KEY_ALGORITHM));
    }

    private SSLContext newSSLContext(final InputStream p_cert, final String p_password, final String p_ksType, final String p_ksAlgorithm) {
        try {
            final KeyStore keyStore = KeyStore.getInstance(p_ksType);
            keyStore.load(p_cert, p_password.toCharArray());
            return newSSLContext(keyStore, p_password, p_ksAlgorithm);
        } catch (final Exception e) {
            throw new InvalidSSLConfig(e);
        }
    }

    private SSLContext newSSLContext(final KeyStore p_ks, final String p_password, final String p_ksAlgorithm) {
        try {
            // Get a KeyManager and initialize it
            final KeyManagerFactory kmf = KeyManagerFactory.getInstance(p_ksAlgorithm);
            kmf.init(p_ks, p_password.toCharArray());

            // Get a TrustManagerFactory with the DEFAULT KEYSTORE, so we have all
            // the certificates in cacerts trusted
            final TrustManagerFactory tmf = TrustManagerFactory.getInstance(p_ksAlgorithm);
            tmf.init((KeyStore) null);

            // Get the SSLContext to help create SSLSocketFactory
            final SSLContext sslc = SSLContext.getInstance("TLS");
            sslc.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

            return sslc;
        } catch (final Exception e) {
            throw new InvalidSSLConfig(e);
        }
    }

}
