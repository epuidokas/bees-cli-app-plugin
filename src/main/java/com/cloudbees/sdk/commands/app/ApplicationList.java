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
    private Boolean more;

    public ApplicationList() {
    }

    @Override
    protected boolean preParseCommandLine() {
        // add the Options
        addOption( "a", "account", true, "Account Name" );
        addOption( null, "more", false, "Display more information" );

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

    public boolean displayMore() {
        return more != null ? more : false;
    }

    public void setMore(Boolean more) {
        this.more = more;
    }

    @Override
    protected boolean execute() throws Exception {
        BeesClient client = getBeesClient(BeesClient.class);
        ApplicationListResponse res = client.applicationList(getAccount());

        if (isTextOutput()) {
            if (displayMore())
                System.out.println("Application                Status    URL                           Instance(s) Container        Pool");
            else
                System.out.println("Application                Status    URL                           Instance(s)");
            System.out.println();
            List<String> list = new ArrayList<String>();
            for (com.cloudbees.api.ApplicationInfo applicationInfo: res.getApplications()) {
                String msg = s(applicationInfo.getId(), 26)+ " " + s(applicationInfo.getStatus(), 10) + s(applicationInfo.getUrls()[0], 38);
                Map<String, String> settings = applicationInfo.getSettings();
                if (settings != null) {
                    msg += " " + s(settings.get("clusterSize"),2);
                    if (displayMore()) {
                        String more = " ";
                        String container = settings.get("container") != null ? settings.get("container") : "free";
                        if (container.startsWith("java_")) container = container.substring(5);
                        more += container;
                        String type = settings.get("containerType");
                        if (type != null) more += " " + type;
                        msg += s(more, 17);
                        msg += " " + settings.get("serverPool");
                    }
                }
                list.add(msg);
            }
            Collections.sort(list);
            for (String app: list) {
                System.out.println(app);
            }
            if (displayMore()) {
                System.out.println();
                System.out.println("Total applications: " + list.size());
            }
        } else {
            printOutput(res, ApplicationInfo.class, ApplicationListResponse.class);
        }

        return true;
    }

    private String s(String str, int length) {
        return Helper.getPaddedString(str, length);
    }

}
