package com.cloudbees.sdk.commands.app;

import com.cloudbees.api.*;
import com.thoughtworks.xstream.XStream;

import java.io.File;
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


    public ApplicationInstanceListResponse applicationInstanceList(String appId) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("app_id", appId);
        String url = getRequestURL("application.instance.list", params);
        trace("API call: " + url);
        String response = executeRequest(url);
        traceResponse(response);
        ApplicationInstanceListResponse apiResponse =
            (ApplicationInstanceListResponse)readResponse(response);
        return apiResponse;
    }

    public ApplicationInstanceStatusResponse applicationInstanceReplace(String instanceId) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("instance_id", instanceId);
        String url = getRequestURL("application.instance.replace", params);
        trace("API call: " + url);
        String response = executeRequest(url);
        traceResponse(response);
        ApplicationInstanceStatusResponse apiResponse =
            (ApplicationInstanceStatusResponse)readResponse(response);
        return apiResponse;
    }

    public ApplicationInstanceStatusResponse applicationInstanceRestart(String instanceId) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("instance_id", instanceId);
        String url = getRequestURL("application.instance.restart", params);
        trace("API call: " + url);
        String response = executeRequest(url);
        traceResponse(response);
        ApplicationInstanceStatusResponse apiResponse =
            (ApplicationInstanceStatusResponse)readResponse(response);
        return apiResponse;
    }

    public ApplicationInstanceStatusResponse applicationInstanceDelete(String instanceId) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("instance_id", instanceId);
        String url = getRequestURL("application.instance.delete", params);
        trace("API call: " + url);
        String response = executeRequest(url);
        traceResponse(response);
        ApplicationInstanceStatusResponse apiResponse =
            (ApplicationInstanceStatusResponse)readResponse(response);
        return apiResponse;
    }

    public ApplicationInstanceStatusResponse applicationInstanceParametersUpdate(String instanceId, Map<String, String>parameters, boolean replace) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("instance_id", instanceId);
        params.put("reset", Boolean.toString(replace));
        params.put("parameters", createParameter(parameters));

        String url = getApiUrl("application.instance.update").toString();
        params.put("action", "application.instance.update");
        // use the upload method (POST) to handle the potentially large parameters payload
        trace("API call: " + url);
        String response = executeUpload(url, params, new HashMap<String, File>(), null);
        traceResponse(response);
        ApplicationInstanceStatusResponse apiResponse =
            (ApplicationInstanceStatusResponse)readResponse(response);
        return apiResponse;
    }

    public ApplicationInstanceInvokeResponse applicationInstanceInvoke(String instanceId, String invoke, Map<String, String>parameters) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("instance_id", instanceId);
        params.put("invoke",invoke);
        params.put("parameters", createParameter(parameters));

        String url = getRequestURL("application.instance.invoke", params);
        trace("API call: " + url);
        String response = executeRequest(url);
        traceResponse(response);
        ApplicationInstanceInvokeResponse apiResponse =
            (ApplicationInstanceInvokeResponse)readResponse(response);
        return apiResponse;
    }

    protected XStream getXStream() throws Exception
    {
        XStream xstream = super.getXStream();

        xstream.processAnnotations(ApplicationInstanceInfo.class);
        xstream.processAnnotations(ApplicationInstanceListResponse.class);

        return xstream;
    }
}
