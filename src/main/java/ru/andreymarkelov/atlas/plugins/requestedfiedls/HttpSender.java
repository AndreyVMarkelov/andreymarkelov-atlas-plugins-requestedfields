package ru.andreymarkelov.atlas.plugins.requestedfiedls;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpSender {
    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(HttpSender.class);

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
     * Constructor.
     */
    public HttpSender(String bindingUrl, String type, String user, String password) {
        this.bindingUrl = bindingUrl;
        this.type = type;
        this.user = user;
        this.password = password;
    }

    /**
     * Call service.
     */
    protected String call(String data) {
        StringBuilder infWebSvcReplyString = new StringBuilder();

        try {
            URL url = new URL(bindingUrl);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setDoOutput(isPost());
            httpConn.setDoInput(true);
            httpConn.setAllowUserInteraction(true);
            httpConn.setRequestMethod(type);
            httpConn.setRequestProperty("Host", url.getHost());
            httpConn.setRequestProperty("Content-Type","application/json; charset=utf-8");
            if (isAuth()) {
                httpConn.setRequestProperty("Authorization", "Basic " + getAuthRealm());
            }
            if (isPost() && data != null && data.length() > 0) {
                OutputStreamWriter out = new OutputStreamWriter(httpConn.getOutputStream());
                out.write(data);
                out.flush();
                out.close();
            }

            int rc = httpConn.getResponseCode();
            if (rc != HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(httpConn.getErrorStream(), "UTF-8"));
                String line;
                while ((line = in.readLine()) != null) {
                    infWebSvcReplyString.append(line);
                }
                in.close();
                httpConn.disconnect();

                log.error("");
                throw new RuntimeException(infWebSvcReplyString.toString());
            }
            else {
                BufferedReader in = new BufferedReader(new InputStreamReader(httpConn.getInputStream(), "UTF-8"));
                String line;
                while ((line = in.readLine()) != null) {
                    infWebSvcReplyString.append(line);
                }
                in.close();
                httpConn.disconnect();
            }
        }
        catch (MalformedURLException mex) {
            log.error("HttpSender::call - Incorrect URL", mex);
            throw new RuntimeException(mex);
        } catch (IOException e) {
            log.error("HttpSender::call - I/O errro occurred", e);
            throw new RuntimeException(e);
        }

        return infWebSvcReplyString.toString();
    }

    /**
     * Get auth realm.
     */
    private String getAuthRealm() {
        return Base64.encodeBase64String(user.concat(":").concat(password).getBytes());
    }

    private boolean isAuth() {
        return (user != null && user.length() > 0) && (password != null && password.length() > 0);
    }

    private boolean isPost() {
        return type.equals("POST");
    }
}