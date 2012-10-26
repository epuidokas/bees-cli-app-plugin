package com.cloudbees.sdk.commands.app;

import com.cloudbees.api.ApplicationInstanceStatusResponse;
import com.cloudbees.sdk.cli.BeesCommand;
import com.cloudbees.sdk.cli.CLICommand;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Fabian Donze
 */
@BeesCommand(group="Application", description = "Set application instance parameters")
@CLICommand("app:instance:set")
public class ApplicationInstanceSet extends ApplicationInstanceBase {
    private Map<String, String> parameters = new HashMap<String, String>();
    private Boolean reset;

    public ApplicationInstanceSet() {
        super();
    }

    public void setReset(Boolean reset) {
        this.reset = reset;
    }

    private boolean isReset() {
        return reset != null ? reset : false;
    }
    protected boolean preParseCommandLine() {
        if (super.preParseCommandLine()) {
            addOption( null, "reset", false, "Reset current parameters with specified ones. By default parameters are added" );
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
                    parameters.put(str.substring(0, idx), str.substring(idx+1));
                }
            }
            return true;
        }
        return false;
    }

    @Override
    protected boolean execute() throws Exception {
        String instanceId = getInstanceId();

        AppClient client = getStaxClient(AppClient.class);
        ApplicationInstanceStatusResponse res = client.applicationInstanceParametersUpdate(instanceId, parameters, isReset());

        if (isTextOutput()) {
            System.out.println(String.format("instance [%s]: %s",instanceId, res.getStatus()));
        } else
            printOutput(res, ApplicationInstanceStatusResponse.class);

        return true;
    }

}
