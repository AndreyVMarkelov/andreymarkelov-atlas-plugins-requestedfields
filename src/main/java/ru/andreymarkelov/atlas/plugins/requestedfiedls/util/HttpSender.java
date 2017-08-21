package ru.andreymarkelov.atlas.plugins.requestedfiedls.util;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.MalformedURLException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.split;

public class HttpSender {
    private static Logger log = LoggerFactory.getLogger(HttpSender.class);

    private static SSLContext createSslContext() {
        try {
            return new SSLContextBuilder().loadTrustMaterial(null, (x509Certificates, authType) -> true).build();
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

    public String call(String customHeaders, String data) {
        SSLContext sslContext = createSslContext();
        HostnameVerifier hostnameVerifier = NoopHostnameVerifier.INSTANCE;
        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);

        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/" + reqDataType));
        headers.add(new BasicHeader(HttpHeaders.ACCEPT, "application/" + reqDataType));
        if (isNotBlank(customHeaders)) {
            String[] headerLines = customHeaders.split("\\r?\\n");
            for (String headerLine : headerLines) {
                String[] parts = split(headerLine, "=", 2);
                headers.add(new BasicHeader(parts[0], parts[1]));
            }
        }

        HttpClientBuilder httpClientBuilder = HttpClients.custom()
            .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
            .setSSLSocketFactory(sslSocketFactory)
            .setDefaultHeaders(headers);
        if (isNotBlank(user) && isNotBlank(password)) {
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(user, password));
            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
        }

        try {
            CloseableHttpResponse httpResponse;
            if (type.equals("POST")){
                HttpPost request = new HttpPost(bindingUrl);
                request.setEntity(new StringEntity(data));
                httpResponse = httpClientBuilder.build().execute(request, new BasicHttpContext());
            } else {
                httpResponse = httpClientBuilder.build().execute(new HttpGet(bindingUrl), new BasicHttpContext());
            }

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
