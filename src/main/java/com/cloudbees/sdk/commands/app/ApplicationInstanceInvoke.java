package com.cloudbees.sdk.commands.app;

import com.cloudbees.api.ApplicationInstanceInvokeResponse;
import com.cloudbees.sdk.cli.BeesCommand;
import com.cloudbees.sdk.cli.CLICommand;
import com.cloudbees.sdk.utils.Helper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Fabian Donze
 */
@BeesCommand(group="Application", description = "Invoke a specific application instance control script")
@CLICommand("app:instance:invoke")
public class ApplicationInstanceInvoke extends ApplicationInstanceBase {
    private String script;
    private String args;
    private String timeout;

    public ApplicationInstanceInvoke() {
        super();
    }

    public String getScript() throws IOException {
        if (script == null) script = Helper.promptFor("Control Script name: ", true);
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public void setArgs(String args) {
        this.args = args;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    protected boolean preParseCommandLine() {
        if (super.preParseCommandLine()) {
            addOption( "cs", "script", true, "the control script" );
            addOption( null, "args", true, "the control script arguments" );
            addOption( null, "timeout", true, "the control script execution timeout" );
            return true;
        }
        return false;
    }

    @Override
    protected boolean execute() throws Exception {
        String instanceId = getInstanceId();

        Map<String, String> parameters = new HashMap<String, String>();
        if (timeout != null)
            parameters.put("timeout", Integer.valueOf(timeout).toString());
        if (args != null)
            parameters.put("args", args);

        AppClient client = getStaxClient(AppClient.class);
        ApplicationInstanceInvokeResponse res = client.applicationInstanceInvoke(instanceId, getScript(), parameters);

        if (isTextOutput()) {
            System.out.println("Exit code: " + res.getExitCode());
            System.out.println(res.getOut());
        } else
            printOutput(res, ApplicationInstanceInvokeResponse.class);

        return true;
    }

}
