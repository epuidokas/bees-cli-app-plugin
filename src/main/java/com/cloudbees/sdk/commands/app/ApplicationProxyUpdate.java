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


import com.cloudbees.api.ApplicationStatusResponse;
import com.cloudbees.sdk.cli.BeesCommand;
import com.cloudbees.sdk.cli.CLICommand;
import com.cloudbees.sdk.utils.Helper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
@BeesCommand(group="Application", description = "Update an application proxy parameters")
@CLICommand("app:proxy:update")
public class ApplicationProxyUpdate extends ApplicationBase {
    private Map<String, String> settings;
    private String alias;
    private Boolean force;

    public ApplicationProxyUpdate() {
        super();
        setArgumentExpected(0);
        settings = new HashMap<String, String>();
    }

    public Map<String, String> getSettings() {
        return settings;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public void setForce(Boolean force) {
        this.force = force;
    }

    /**
     * This method is call by the help command.
     * This is the place to define the command usage.
     * No need to return the options, they will be automatically added to the help
     *
     * @return usage String
     */
    @Override
    protected String getUsageMessage() {
        return "APPLICATION_ID [parameterX=valueY]";
    }

    @Override
    protected boolean preParseCommandLine() {
        if (super.preParseCommandLine()) {
            addOption( "f", "force", false, "force update without prompting" );
            addOption("al", "alias", true, "Application domain name aliases");
            return true;
        }
        return false;
    }

    @Override
    protected boolean postParseCommandLine() {
        if (super.postParseCommandLine()) {
            List otherArgs = getCommandLine().getArgList();
            for (int i=0; i<otherArgs.size(); i++) {
                String str = (String)otherArgs.get(i);
                int idx = isParameter(str);
                if (idx > -1) {
                    settings.put(str.substring(0, idx), str.substring(idx+1));
                }
            }
            return true;
        }
        return false;
    }

    @Override
    protected boolean execute() throws Exception {
        String appId = getAppId();

        if (force == null || !force.booleanValue()) {
            if (!Helper.promptMatches("Are you sure you want to update this application proxy [" + appId + "]: (y/n) ", "[yY].*")) {
                return true;
            }
        }

        AppClient client = getBeesClient(AppClient.class);

        if (alias != null)
            settings.put("aliases", alias);

        ApplicationStatusResponse res = client.applicationProxyUpdate(appId, getSettings());
        if (isTextOutput()) {
            System.out.println("application proxy - " + appId + " updated: " + res.getStatus());
        } else
            printOutput(res, ApplicationStatusResponse.class);

        return true;
    }
}
