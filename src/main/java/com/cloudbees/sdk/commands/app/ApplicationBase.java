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
import com.cloudbees.sdk.commands.Command;
import com.cloudbees.sdk.utils.Helper;
import java.io.IOException;
import java.util.Map;

/**
 * @author Fabian Donze
 */
public abstract class  ApplicationBase extends Command {
    /**
     * The id of the application.
     */
    private String appid;

    private String account;


    public void setAppid(String appid) {
        this.appid = appid;
    }

    protected String getAppid() {
        return appid;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    @Override
    protected String getUsageMessage() {
        return "APPLICATION_ID";
    }

    @Override
    protected boolean preParseCommandLine() {
        // add the Options
        addOption( "a", "appid", true, "CloudBees application ID" );

        return true;
    }

    @Override
    protected boolean postParseCommandLine() {
        if (!super.postParseCommandLine()) return false;

        return postParseParameters();
    }

    protected boolean postParseParameters() {
        if (getParameters().size() > 0 && appid == null) {
            String str = getParameters().get(0);
            if (isParameter(str) < 0)
                setAppid(str);
        }
        return true;
    }

    protected String getAccount() throws IOException {
        if (account == null) account = getConfigProperties().getProperty("bees.project.app.domain");
        if (account == null) account = Helper.promptFor("Account name: ", true);
        return account;
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
        if (appIdParts.length < 2) {
            String defaultAppDomain = getAccount();
            if (defaultAppDomain != null && !defaultAppDomain.equals("")) {
                appid = defaultAppDomain + "/" + appid;
            } else {
                throw new RuntimeException("default app account could not be determined, appid needs to be fully-qualified ");
            }
        }
        return appid;
    }

    protected AccountRegionInfo getApplicationRegionInfo(AppClient client, String appId) {
        try {
            ServiceResourceInfo resourceInfo = client.serviceResourceInfo("cb-app", appId);
            Map<String, String> config= resourceInfo.getConfig();
            if (config != null) {
                String region = config.get("region");
                if (region != null) {
                    String parts[] = appId.split("/");
                    AccountRegionListResponse res = client.accountRegionList(parts[0], null);
                    return getRegionInfo(res, region);
                }
            }
        } catch (Exception e) {
            if (!e.getMessage().toLowerCase().contains("no such resource"))
                System.err.println("Error: " + e.getMessage());
        }

        return null;
    }

    protected AppClient getAppClient(String appId) throws IOException {
        AppClient client = getBeesClient(AppClient.class);
        AccountRegionInfo endPoint = getApplicationRegionInfo(client, appId);
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

    private AccountRegionInfo getRegionInfo(AccountRegionListResponse regions, String region) {
        for (AccountRegionInfo regionInfo : regions.getRegions()) {
            if (regionInfo.getName().equalsIgnoreCase(region)) {
                return regionInfo;
            }
        }
        return null;
    }
}
