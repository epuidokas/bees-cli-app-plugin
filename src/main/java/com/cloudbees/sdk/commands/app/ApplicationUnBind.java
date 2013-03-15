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

import com.cloudbees.api.ServiceResourceUnBindResponse;
import com.cloudbees.api.StaxClient;
import com.cloudbees.sdk.cli.BeesCommand;
import com.cloudbees.sdk.cli.CLICommand;
import com.cloudbees.sdk.commands.services.ServiceBase;
import com.cloudbees.sdk.utils.Helper;

import java.io.IOException;

/**
 * @author Fabian Donze
 */
@BeesCommand(group="Application", description = "Unbind an application resource")
@CLICommand("app:unbind")
public class ApplicationUnBind extends ServiceBase {
    /**
     * The id of the application.
     */
    private String appid;

    public ApplicationUnBind() {
        setArgumentExpected(1);
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getAppid() {
        return appid;
    }

    @Override
    protected boolean preParseCommandLine() {
        // add the Options
        addOption( "a", "appid", true, "CloudBees application ID" );

        return true;
    }

    @Override
    protected String getUsageMessage() {
        return "BINDING_ALIAS";
    }

    @Override
    protected boolean execute() throws Exception {
        initAppId();

        StaxClient client = getBeesClient(StaxClient.class);
        ServiceResourceUnBindResponse res = client.resourceUnBind("cb-app", getAppid(), getAlias());
        if (isTextOutput()) {
//            System.out.println("Message: " + res.getMessage());
            System.out.println("application - " + getAppid() + " binding " + getAlias() + " removed");
        } else
            printOutput(res, ServiceResourceUnBindResponse.class);
        return true;
    }

    private String getAlias() {
        return getParameters().get(0);
    }

    private void initAppId() throws IOException
    {
        if (appid == null || appid.equals("")) {
            appid = AppHelper.getArchiveApplicationId();
        }

        if (appid == null || appid.equals(""))
            appid = Helper.promptForAppId();

        if (appid == null || appid.equals(""))
            throw new IllegalArgumentException("No application id specified");

        String[] parts = appid.split("/");
        if (parts.length < 2)
            appid = getAccount() + "/" + appid;
    }


}
