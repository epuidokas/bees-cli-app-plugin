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

import com.cloudbees.api.ApplicationListResponse;
import com.cloudbees.api.BeesClient;
import com.cloudbees.sdk.cli.BeesCommand;
import com.cloudbees.sdk.cli.CLICommand;
import com.cloudbees.sdk.commands.Command;
import com.cloudbees.sdk.utils.Helper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Fabian Donze
 */
@BeesCommand(group="Application", pattern = "app:li.*", description = "List applications")
@CLICommand("app:list")
public class ApplicationList extends Command {
    private String account;

    public ApplicationList() {
    }

    @Override
    protected boolean preParseCommandLine() {
        // add the Options
        addOption( "a", "account", true, "Account Name" );

        return true;
    }

    @Override
    protected boolean postParseCommandLine() {
        return true;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    protected String getAccount() throws IOException {
        return account;
    }

    @Override
    protected boolean execute() throws Exception {
        BeesClient client = getBeesClient(BeesClient.class);
        ApplicationListResponse res = client.applicationList(getAccount());

        if (isTextOutput()) {
            System.out.println("Application                Status    URL                           Instance(s)");
            System.out.println();
            List<String> list = new ArrayList<String>();
            for (com.cloudbees.api.ApplicationInfo applicationInfo: res.getApplications()) {
                String msg = s(applicationInfo.getId(), 26)+ " " + s(applicationInfo.getStatus(), 10) + s(applicationInfo.getUrls()[0], 38);
                Map<String, String> settings = applicationInfo.getSettings();
                if (settings != null) {
                    msg += " " + settings.get("clusterSize");
                }
                list.add(msg);
            }
            Collections.sort(list);
            for (String app: list)
                System.out.println(app);
        } else {
            printOutput(res, ApplicationInfo.class, ApplicationListResponse.class);
        }

        return true;
    }

    private String s(String str, int length) {
        return Helper.getPaddedString(str, length);
    }

}
