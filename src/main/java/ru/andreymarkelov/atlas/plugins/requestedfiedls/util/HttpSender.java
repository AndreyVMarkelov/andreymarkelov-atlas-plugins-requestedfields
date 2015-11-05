package ru.andreymarkelov.atlas.plugins.requestedfiedls.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpSender {
    private static Logger log = LoggerFactory.getLogger(HttpSender.class);

    private static SSLContext createSslContext() {
        try {
            return new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                public boolean isTrusted(X509Certificate[] x509Certificates, String authType) throws CertificateException {
                    return true;
                }
            }).build();
        } catch (NoSuchAlgorithmException e) {
            log.error("Error creating SSL context", e);
            throw new RuntimeException("Error creating SSL context. No such algorithm.");
        } catch (KeyManagementException e) {
            log.error("Error creating SSL context", e);
            throw new RuntimeException("Error creating SSL context. Key manager exception.");
        } catch (KeyStoreException e) {
            log.error("Error creating SSL context", e);
            throw new RuntimeException("Error creating SSL context. Keystore exception.");
        }
    }

    /**
     * Binding URL.
     */
    private String bindingUrl;

    /**
     * Request type.
     */
    private String type;

    /**
     * Password.
    */
    private String password;

    /**
     * User.
     */
    private String user;

    /**
     * Data type.
     */
    private String reqDataType;

    /**
     * Constructor.
     */
    public HttpSender(String bindingUrl, String type, String reqDataType, String user, String password) {
        this.bindingUrl = bindingUrl;
        this.type = type;
        this.reqDataType = reqDataType;
        this.user = user;
        this.password = password;
    }

    public String call(String data) {
        SSLContext sslContext = createSslContext();
        HostnameVerifier hostnameVerifier = NoopHostnameVerifier.INSTANCE;
        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);

        Header headerContent = new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/" + reqDataType);
        Header headerAccept = new BasicHeader(HttpHeaders.ACCEPT, "application/" + reqDataType);
        List<Header> headers = Arrays.asList(headerContent, headerAccept);

        HttpClientBuilder httpClientBuilder = HttpClients.custom()
            .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
            .setSSLSocketFactory(sslSocketFactory)
            .setDefaultHeaders(headers);
        if (StringUtils.isNotBlank(user) && StringUtils.isNotBlank(password)) {
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(user, password));
            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
        }
        HttpUriRequest request = type.equals("POST") ? new HttpPost(bindingUrl) : new HttpGet(bindingUrl);

        try {
            CloseableHttpResponse httpResponse = httpClientBuilder.build().execute(request, new BasicHttpContext());

            StatusLine statusLine = httpResponse.getStatusLine();
            if (statusLine.getStatusCode() >= 200 && statusLine.getStatusCode() < 300) {
                return EntityUtils.toString(httpResponse.getEntity());
            } else {
                throw new RuntimeException(EntityUtils.toString(httpResponse.getEntity()));
            }
        }
        catch (MalformedURLException mex) {
            log.error("HttpSender::call - Incorrect URL", mex);
            throw new RuntimeException(mex);
        } catch (IOException e) {
            log.error("HttpSender::call - I/O errro occurred", e);
            throw new RuntimeException(e);
        }
    }
}
