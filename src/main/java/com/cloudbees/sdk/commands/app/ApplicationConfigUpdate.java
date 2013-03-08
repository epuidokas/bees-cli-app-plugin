package com.cloudbees.sdk.commands.app;


import com.cloudbees.api.ApplicationConfigUpdateResponse;
import com.cloudbees.api.StaxClient;
import com.cloudbees.sdk.cli.BeesCommand;
import com.cloudbees.sdk.cli.CLICommand;
import com.cloudbees.sdk.utils.Helper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
@BeesCommand(group="Application", description = "Update an application configuration parameters")
@CLICommand("app:update")
public class ApplicationConfigUpdate extends ApplicationBase {
    private Map<String, String> settings;
    private Map<String, String> runtimeParameters = new HashMap<String, String>();
    private String type;
    private String snapshot;
    private Boolean force;

    public ApplicationConfigUpdate() {
        super();
        setArgumentExpected(0);
        settings = new HashMap<String, String>();
    }

    public Map<String, String> getSettings() {
        return settings;
    }

    public Map<String, String> getRuntimeParameters() {
        return runtimeParameters;
    }

    public void setR(String rt) {
        rt = rt.trim();
        int idx = isParameter(rt);
        if (idx > -1) {
            runtimeParameters.put(rt.substring(0, idx), rt.substring(idx + 1));
        }
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setSnapshot(String snapshot) {
        this.snapshot = snapshot;
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
            addOption("t", "type", true, "Application container type");
            addOption("ss", "snapshot", true, "Application active snapshot");
            addOption( "R", null, true, "Runtime config parameter name=value" );
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
            if (!Helper.promptMatches("This command will restart your application, are you sure you want to update this application [" + appId + "]: (y/n) ", "[yY].*")) {
                return true;
            }
        }

        StaxClient client = getBeesClient(StaxClient.class);

        Map<String, String> rts = getRuntimeParameters();
        if (rts.size() > 0) {
//            System.out.println("Runtime parameters: " + rts);
            for (Map.Entry<String,String> entry : rts.entrySet()) {
                settings.put("runtime." + entry.getKey(), entry.getValue());
            }
        }

        if (type != null)
            settings.put("containerType", type);
        if (snapshot != null)
            settings.put("snapshot", snapshot);

        ApplicationConfigUpdateResponse res = client.applicationConfigUpdate(appId, getSettings());
        if (isTextOutput()) {
            System.out.println("application - " + appId + " updated: " + res.getStatus());
        } else
            printOutput(res, ApplicationConfigUpdateResponse.class);

        return true;
    }
}
