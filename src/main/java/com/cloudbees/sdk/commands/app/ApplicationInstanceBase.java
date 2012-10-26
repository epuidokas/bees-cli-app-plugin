package com.cloudbees.sdk.commands.app;

import com.cloudbees.api.ApplicationInstanceInfo;
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
public abstract class ApplicationInstanceBase extends Command {
    private String instance;
    private String account;

    public ApplicationInstanceBase() {
        setArgumentExpected(0);
    }

    public String getInstance() throws IOException {
        if (instance == null) instance = Helper.promptFor("Instance ID: ", true);
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    @Override
    protected String getUsageMessage() {
        return "INSTANCE_ID";
    }

    @Override
    protected boolean preParseCommandLine() {
        if (super.preParseCommandLine()) {
            addOption( "i", "instance", true, "the application instance ID" );
            return true;
        }
        return false;
    }

    @Override
    protected boolean postParseCommandLine() {
        if (!super.postParseCommandLine()) return false;

        if (getParameters().size() > 0 && instance == null) {
            String str = getParameters().get(0);
            if (isParameter(str) < 0)
                setInstance(str);
        }

        return true;
    }

    protected String getAccount() throws IOException {
        if (account == null) account = getConfigProperties().getProperty("bees.project.app.domain");
        if (account == null) account = Helper.promptFor("Account name: ", true);
        return account;
    }

    protected String getInstanceId() throws IOException
    {
        String instanceId = getInstance();

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

    public static void printApplicationInstanceInfo(ApplicationInstanceInfo instanceInfo) {
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
    }


}
