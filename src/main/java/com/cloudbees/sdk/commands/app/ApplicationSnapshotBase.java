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

import com.cloudbees.api.ApplicationInstanceInfo;
import com.cloudbees.api.ApplicationSnapshotInfo;
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
public abstract class ApplicationSnapshotBase extends Command {
    private String snapshot;
    private String account;

    public ApplicationSnapshotBase() {
        setArgumentExpected(0);
    }

    public String getSnapshot() throws IOException {
        if (snapshot == null) snapshot = Helper.promptFor("Snapshot ID: ", true);
        return snapshot;
    }

    public void setSnapshot(String snapshot) {
        this.snapshot = snapshot;
    }

    @Override
    protected String getUsageMessage() {
        return "SNAPSHOT_ID";
    }

    @Override
    protected boolean preParseCommandLine() {
        if (super.preParseCommandLine()) {
            addOption( "ss", "snapshot", true, "the application snapshot ID" );
            return true;
        }
        return false;
    }

    @Override
    protected boolean postParseCommandLine() {
        if (!super.postParseCommandLine()) return false;

        if (getParameters().size() > 0 && snapshot == null) {
            String str = getParameters().get(0);
            if (isParameter(str) < 0)
                setSnapshot(str);
        }

        return true;
    }

    protected String getAccount() throws IOException {
        if (account == null) account = getConfigProperties().getProperty("bees.project.app.domain");
        if (account == null) account = Helper.promptFor("Account name: ", true);
        return account;
    }

    protected String getSnapshotId() throws IOException
    {
        String instanceId = getSnapshot();

        String[] appIdParts = instanceId.split("/");
        if (appIdParts.length < 2) {
            String defaultAppDomain = getAccount();
            if (defaultAppDomain != null && !defaultAppDomain.equals("")) {
                instanceId = defaultAppDomain + "/" + instanceId;
            } else {
                throw new RuntimeException("default app account could not be determined, instanceId needs to be fully-qualified ");
            }
        }
        return instanceId;
    }

    public static void printApplicationSnapshotInfo(ApplicationSnapshotInfo snapshotInfo) {
        System.out.println("Snapshot ID     : " + snapshotInfo.getId());
        System.out.println("application ID  : " + snapshotInfo.getApplicationId());
        Map<String, String> settings = snapshotInfo.getSettings();
        if (settings != null) {
            List<String> list = new ArrayList<String>(settings.size());
            for (Map.Entry<String, String> entry: settings.entrySet()) {
                list.add(Helper.getPaddedString(entry.getKey(), 16) + ": " + entry.getValue());
            }
            Collections.sort(list);
            for (String item : list)
                System.out.println(item);
        }
    }


}
