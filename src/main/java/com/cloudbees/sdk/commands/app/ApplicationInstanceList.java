package com.cloudbees.sdk.commands.app;


import com.cloudbees.api.ApplicationInstanceInfo;
import com.cloudbees.api.ApplicationInstanceListResponse;
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
@BeesCommand(group="Application", description = "List application instances")
@CLICommand("app:instance:list")
public class ApplicationInstanceList extends ApplicationBase {

    public ApplicationInstanceList() {
    }

    @Override
    protected boolean execute() throws Exception {
        AppClient client = getStaxClient(AppClient.class);
        ApplicationInstanceListResponse res = client.applicationInstanceList(getAppId());
        if (isTextOutput()) {
            for (ApplicationInstanceInfo instanceInfo : res.getInstances()) {
                System.out.println( "Instance ID     : " + instanceInfo.getId());
                Map<String, String> settings = instanceInfo.getSettings();
                if (settings != null) {
                    List<String> list = new ArrayList<String>(settings.size());
                    for (Map.Entry<String, String> entry: settings.entrySet()) {
                        list.add(Helper.getPaddedString(entry.getKey(), 16) + ": " + entry.getValue());
                    }
                    Collections.sort(list);
                    for (String item : list)
                        System.out.println(item);
                }
                Map<String, String> parameters = instanceInfo.getParameters();
                if (parameters != null && parameters.size() > 0) {
                    System.out.println( "Parameters");
                    List<String> list = new ArrayList<String>(parameters.size());
                    for (Map.Entry<String, String> entry: parameters.entrySet()) {
                        list.add(Helper.getPaddedString("  " + entry.getKey(), 16) + ": " + entry.getValue());
                    }
                    Collections.sort(list);
                    for (String item : list)
                        System.out.println(item);
                }
                System.out.println();
            }
        } else {
            printOutput(res, ApplicationInstanceInfo.class, ApplicationInstanceListResponse.class);
        }

        return true;
    }

}
