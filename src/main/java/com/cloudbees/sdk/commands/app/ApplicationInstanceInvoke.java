/*
 * Copyright 2010-2013, CloudBees Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cloudbees.sdk.commands.app;

import com.cloudbees.api.*;
import com.cloudbees.sdk.cli.BeesCommand;
import com.cloudbees.sdk.cli.CLICommand;
import com.cloudbees.sdk.utils.Helper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Fabian Donze
 */
@BeesCommand(group="Application", description = "Invoke an application instance control script")
@CLICommand("app:instance:invoke")
public class ApplicationInstanceInvoke extends ApplicationInstanceBase {
    private String script;
    private String args;
    private String timeout;
    private String appid;
    private String account;

    public ApplicationInstanceInvoke() {
        super();
    }

    public String getScript() throws IOException {
        if (script == null) script = Helper.promptFor("Control Script name: ", true);
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public void setArgs(String args) {
        this.args = args;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    public String getAppid() throws IOException {
        String[] appIdParts = appid.split("/");
        if (appIdParts.length < 2) {
            String defaultAppDomain = getAccount();
            if (defaultAppDomain != null && !defaultAppDomain.equals("")) {
                appid = defaultAppDomain + "/" + appid;
            } else {
                throw new RuntimeException("default app account could not be determined, instanceId needs to be fully-qualified ");
            }
        }
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    protected boolean preParseCommandLine() {
        if (super.preParseCommandLine()) {
            addOption( "cs", "script", true, "the control script" );
            addOption( "a", "appid", true, "CloudBees application ID to invoke on all instances" );
            addOption( null, "args", true, "the control script arguments" );
            addOption( null, "timeout", true, "the control script execution timeout" );
            return true;
        }
        return false;
    }

    @Override
    protected boolean execute() throws Exception {
        // Invoke all instances
        if (appid != null) {
            String appid = getAppid();
            AppClient client = getAppClient(appid);
            ApplicationInstanceListResponse res = client.applicationInstanceList(appid);
            for (com.cloudbees.api.ApplicationInstanceInfo instanceInfo : res.getInstances()) {
                invokeInstance(client, instanceInfo.getId());
            }
        // Invoke a specific instance
        } else {
            AppClient client = getBeesClient(AppClient.class);
            String instanceId = getInstanceId();
            invokeInstance(client, instanceId);
        }

        return true;
    }

    protected AppClient getAppClient(String appId) throws IOException {
        AppClient client = getBeesClient(AppClient.class);
        AccountRegionInfo endPoint = AppHelper.getApplicationRegionInfo(client, appId);
        if (endPoint != null) {
            String apiUrl = endPoint.getSettings().get("api.url");
            BeesClientConfiguration clientConfiguration = client.getBeesClientConfiguration();
            String currentApiUrl = clientConfiguration.getServerApiUrl();
            if (!currentApiUrl.equalsIgnoreCase(apiUrl)) {
                System.err.println(String.format("WARNING: Application [%s] defined in the [%s] region, using [%s] API end point",
                        appId, endPoint.getName(), endPoint.getName()));
                clientConfiguration.setServerApiUrl(apiUrl);
                client = new AppClient(clientConfiguration);
                client.setVerbose(isVerbose());
            }
        }
        return client;
    }

    protected void invokeInstance(AppClient client, String instanceId) throws Exception {
        Map<String, String> parameters = new HashMap<String, String>();
        if (timeout != null)
            parameters.put("timeout", Integer.valueOf(timeout).toString());
        if (args != null)
            parameters.put("args", args);

        ApplicationInstanceInvokeResponse res = client.applicationInstanceInvoke(instanceId, getScript(), parameters);

        if (isTextOutput()) {
            System.out.println(String.format("%s > Exit code: %s",instanceId, res.getExitCode()));
            System.out.println(res.getOut());
        } else
            printOutput(res, ApplicationInstanceInvokeResponse.class);
    }

}
