package no.shitt.myshit.helper;

import android.accounts.AuthenticatorException;
//import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import no.shitt.myshit.Constants;

public class ServerAPI /*extends AsyncTask<ServerAPI.Params, String, String>*/ {
    private final static String LOG_TAG = ServerAPI.class.getSimpleName();

    // Interface constants
    public enum Method {
        PUT    ("PUT"),
        POST   ("POST"),
        GET    ("GET");

        private final String rawValue;
        Method(String rawValue) {
            this.rawValue = rawValue;
        }
    }

    public final static String URL_USER_VERIFY  = "https://www.shitt.no/mySHiT/v2/user";
    public final static String URL_TRIP_INFO    = "https://www.shitt.no/mySHiT/v2/trip";
    public final static String URL_BASE         = "https://www.shitt.no/mySHiT/v2";

    public final static int    VERSION_CURRENT  = 2;

    public final static String RESOURCE_TRIP    = "trip";
    public final static String RESOURCE_CHAT    = "thread";

    public final static String VERB_MSG_READ    = "read";

    public static class Param {
        public final static String USER_NAME        = "userName";
        public final static String PASSWORD         = "password";
        public final static String LANGUAGE         = "language";
        public final static String LAST_MESSAGE_ID  = "lastMessageId";
    }

    public static class ResultItem {
        public static final String API_VERSION       = "apiVersion";
        public static final String COUNT             = "count";
        public static final String RESULTS_V1        = "results";
        public static final String TRIP_LIST         = "trips";
        public static final String CONTENT           = "content";
        public static final String USER              = "user";
        public static final String MESSAGE           = "message";
        public static final String TIMESTAMP         = "timestamp";
    }

    public static class ResultItemValue {
        public final static String CONTENT_LIST      = "list";
        //public final static String CONTENT_DETAILS  = "details";
    }

    // Instance properties
    private final Listener listener;
    private JSONObject response;
    private final Method method;

    public ServerAPI(Method method, Listener listener) {
        this.method   = method;
        this.listener = listener;
    }

    public ServerAPI(Listener listener) {
        this.method   = Method.GET;
        this.listener = listener;
    }

//    @Override
//    protected String doInBackground(Params... params) {
    public /*String*/ void execute(Params... params) {
        StringBuilder urlBuilder = new StringBuilder(params[0].baseUrl);

        // Build URL
        if (params[0].resource != null) {
            urlBuilder.append("/");
            urlBuilder.append(params[0].resource);
            if (params[0].resourceId != null) {
                urlBuilder.append("/");
                urlBuilder.append(params[0].resourceId);
            }
        }

        if (params[0].verb != null) {
            urlBuilder.append("/");
            urlBuilder.append(params[0].verb);
            urlBuilder.append("/");
            urlBuilder.append(params[0].verbArgument);
        }

        try {
            if (params[0].parameters != null) {
                boolean firstParam = true;
                for (Map.Entry<String, String> entry : params[0].parameters.entrySet()) {
                    urlBuilder.append(firstParam ? "?" : "&");
                    urlBuilder.append(entry.getKey());
                    urlBuilder.append("=");
                    urlBuilder.append(URLEncoder.encode(entry.getValue(), Constants.URL_ENCODE_CHARSET));
                    firstParam = false;
                }

            }
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException(uee.getMessage());
        }

        String urlString = urlBuilder.toString();

        // Invoke REST API
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute( () -> {
            InputStream in;
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(urlString);
                Log.i(LOG_TAG, "Connecting to " + urlString + " using " + method.rawValue);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(method == Method.POST || method == Method.PUT);
                urlConnection.setConnectTimeout(15000);
                urlConnection.setReadTimeout(10000);
                urlConnection.setRequestMethod(method.rawValue);

                if ((method == Method.POST || method == Method.PUT) && params[0].getPayload() != null) {
                    OutputStream out = urlConnection.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(
                            new OutputStreamWriter(out, StandardCharsets.UTF_8));
                    writer.write(params[0].getPayload().toString());
                    writer.flush();
                    writer.close();
                    out.close();
                }

                // getResponseCode() connects to server - all settings and data must go before this
                if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    throw new AuthenticatorException();
                }
                in = new BufferedInputStream(urlConnection.getInputStream());

                BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.ISO_8859_1), 8);
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                    sb.append("\n");
                }
                reader.close();
                in.close();

                //resultToDisplay = sb.toString();
                if (sb.length() == 0) {
                    throw new IOException("Empty response, expected JSON");
                }
                try {
                    response = new JSONObject(sb.toString());
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Invalid response for '" + urlString + "': " + sb);
                    throw e;
                }
//                if (response != null) {
                    listener.onRemoteCallComplete(response);
//                }
            } catch (Exception e) {
                listener.onRemoteCallFailed(e);
                //return e.getMessage();
            } finally {
                if (urlConnection != null) urlConnection.disconnect();
            }
        } ) ;
    }


    public static class Params {
        private final String baseUrl;
        private String resource;
        private String resourceId;
        private String verb;
        private String verbArgument;
        public Map<String, String> parameters;
        private JSONObject payload;

        public Params(String url) {
            baseUrl = url;
        }

        public Params(String baseUrl, String resource, String resourceId, String verb, String verbArgument) {
            this.baseUrl = baseUrl;
            this.resource = resource;
            this.resourceId = resourceId;
            this.verb = verb;
            this.verbArgument = verbArgument;
        }

        public void addParameter(String name, String value) {
            if (parameters == null) {
                parameters = new HashMap<>();
            }
            parameters.put(name, value);
        }

        public void setPayload(JSONObject payload) {
            this.payload = payload;
        }

        JSONObject getPayload() {
            return payload;
        }
    }

    public interface Listener {
        void onRemoteCallComplete(JSONObject jsonFromNet);
        void onRemoteCallFailed();
        void onRemoteCallFailed(Exception e);
    }
}
