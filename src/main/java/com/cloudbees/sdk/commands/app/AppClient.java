package com.cloudbees.sdk.commands.app;

import com.cloudbees.api.ApplicationStatusResponse;
import com.cloudbees.api.BeesClientConfiguration;
import com.cloudbees.api.StaxClient;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Fabian Donze
 */
public class AppClient extends StaxClient {
    public AppClient(String serverApiUrl, String apikey, String secret, String format, String version) {
        super(serverApiUrl, apikey, secret, format, version);
    }

    public AppClient(BeesClientConfiguration beesClientConfiguration) {
        super(beesClientConfiguration);
    }

    public ApplicationStatusResponse applicationHibernate(String appId) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("app_id", appId);
        String url = getRequestURL("application.hibernate", params);
        trace("API call: " + url);
        String response = executeRequest(url);
        traceResponse(response);
        ApplicationStatusResponse apiResponse =
            (ApplicationStatusResponse)readResponse(response);
        return apiResponse;
    }


}
