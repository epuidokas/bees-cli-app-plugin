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


import com.cloudbees.api.BeesClient;
import com.cloudbees.sdk.cli.BeesCommand;
import com.cloudbees.sdk.cli.CLICommand;
import com.cloudbees.sdk.utils.Helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Fabian Donze
 */
@BeesCommand(group="Application", description = "Get an application configuration")
@CLICommand("app:info")
public class ApplicationInfo extends ApplicationBase {

    public ApplicationInfo() {
    }

    @Override
    protected boolean execute() throws Exception {
        String appid = getAppId();
        AppClient client = getAppClient(appid);
        com.cloudbees.api.ApplicationInfo res = client.applicationInfo(appid);
        if (isTextOutput()) {
            System.out.println( "Application     : " + res.getId());
            System.out.println( "Title           : " + res.getTitle());
            System.out.println( "Created         : " + res.getCreated());
            System.out.println( "Status          : " + res.getStatus());
            System.out.println( "URL             : " + res.getUrls()[0]);
            Map<String, String> settings = res.getSettings();
            if (settings != null) {
                List<String> list = new ArrayList<String>(settings.size());
                for (Map.Entry<String, String> entry: settings.entrySet()) {
                    list.add(Helper.getPaddedString(entry.getKey(), 16) + ": " + entry.getValue());
                }
                Collections.sort(list);
                for (String item : list)
                    System.out.println(item);
            }
        } else {
            printOutput(res, com.cloudbees.api.ApplicationInfo.class);
        }

        return true;
    }

}
