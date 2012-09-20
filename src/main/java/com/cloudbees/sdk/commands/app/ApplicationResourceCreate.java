package com.cloudbees.sdk.commands.app;

import com.cloudbees.api.ServiceResourceBindResponse;
import com.cloudbees.api.ServiceResourceInfo;
import com.cloudbees.api.StaxClient;
import com.cloudbees.sdk.cli.BeesCommand;
import com.cloudbees.sdk.cli.CLICommand;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Fabian Donze
 */
@BeesCommand(group="Application")
@CLICommand("app:resource:create")
public class ApplicationResourceCreate extends ApplicationResourceBase {
    private String bind;

    public ApplicationResourceCreate() {
        setArgumentExpected(1);
    }

    protected boolean bind() {
        return bind == null ? false : Boolean.valueOf(bind);
    }

    public void setBind(String bind) {
        this.bind = bind;
    }

    @Override
    protected String getUsageMessage() {
        return "RESOURCE_NAME";
    }

    @Override
    public String getAccount() throws IOException {
        if (getAppid() != null) {
            String[] parts = getAppid().split("/");
            setAccount(parts[0]);
        }
        return super.getAccount();
    }

    @Override
    protected boolean preParseCommandLine() {
        if(super.preParseCommandLine()) {
            removeOption("a");
            addOption("ac", "account", true, "CloudBees Account Name");
            addOption( "a", "appid", true, "CloudBees Application ID" );
            addOption( "bind", true, "bind resource, default: false" );

            return true;
        }
        return false;
    }

    @Override
    protected boolean execute() throws Exception {
        StaxClient client = getStaxClient(StaxClient.class);
        ServiceResourceInfo resource = client.serviceResourceCreate(getServiceName(), getAccount(), getResourceType(), getParameterName(), getSettings());
        if (bind()) {
            String appid = getAppId();

            ServiceResourceBindResponse res2 = client.resourceBind(getServiceName(), appid, resource.getService(), resource.getId(), resource.getId(), new HashMap<String, String>());
            if (isTextOutput()) {
                System.out.println("Application - " + appid + " bound to " + resource.getId());
            } else
                printOutput(res2, ServiceResourceBindResponse.class);
        } else {
            if (isTextOutput()) {
                System.out.println("Resource: " + resource.getId());
                Map<String, String> config = resource.getConfig();
                if(config != null && config.size() > 0) {
                    System.out.println("config:");
                    for (Map.Entry<String, String> entry : config.entrySet()) {
                        System.out.println("  " + entry.getKey() + "=" + entry.getValue());
                    }
                }
                Map<String, String> settings = resource.getSettings();
                if(settings != null && settings.size() > 0) {
                    System.out.println("settings:");
                    for (Map.Entry<String, String> entry : settings.entrySet()) {
                        System.out.println("  " + entry.getKey() + "=" + entry.getValue());
                    }
                }
            } else
                printOutput(resource, ServiceResourceInfo.class);
        }
        return true;
    }

}

