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

import com.cloudbees.sdk.commands.services.ServiceBase;
import com.cloudbees.sdk.utils.Helper;

import java.io.IOException;

/**
 *
 */
public abstract class ApplicationResourceBase extends ServiceBase {
    private String appid;
    private String serviceName = "cb-app";
    private String resourceType;

    public ApplicationResourceBase() {
        super();
    }

    /**
     * This method is call after parsing the command line.
     * This is the place to parse additional command arguments
     *
     * @return true if successful, false otherwise
     */
    @Override
    protected boolean postParseCommandLine() {
        if (super.postParseCommandLine()) {
            String[] parts = getCommandName().split(":");
            if (parts != null && parts.length > 0) {
                if (parts.length > 1)
                    resourceType = parts[1];

                return true;
            }
        }
        return false;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getAppid() {
        return appid;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getResourceType() {
        return resourceType;
    }

    protected String getAppId() throws IOException
    {
        if (appid == null || appid.equals("")) {
            appid = AppHelper.getArchiveApplicationId();
        }

        if (appid == null || appid.equals(""))
            appid = Helper.promptForAppId();

        if (appid == null || appid.equals(""))
            throw new IllegalArgumentException("No application id specified");

        String[] appIdParts = appid.split("/");
        if (appIdParts.length > 1) {
            setAccount(appIdParts[0]);
        }

        String defaultAppDomain = getAccount();
        if (appIdParts.length < 2) {
            if (defaultAppDomain != null && !defaultAppDomain.equals("")) {
                appid = defaultAppDomain + "/" + appid;
            } else {
                throw new RuntimeException("Default app account could not be determined, appid needs to be fully-qualified ");
            }
        }
        return appid;
    }
}
