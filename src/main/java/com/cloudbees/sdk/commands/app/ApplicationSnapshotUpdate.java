package com.cloudbees.sdk.commands.app;

import com.cloudbees.api.ApplicationSnapshotStatusResponse;
import com.cloudbees.sdk.cli.BeesCommand;
import com.cloudbees.sdk.cli.CLICommand;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Fabian Donze
 */
@BeesCommand(group="Application", description = "Update application snapshot information")
@CLICommand("app:snapshot:update")
public class ApplicationSnapshotUpdate extends ApplicationSnapshotBase {
    private String title;

    public void setTitle(String title) {
        this.title = title;
    }

    public ApplicationSnapshotUpdate() {
        super();
    }

    protected boolean preParseCommandLine() {
        if (super.preParseCommandLine()) {
            addOption( "t", "title", true, "The snapshot title" );
            return true;
        }
        return false;
    }

    @Override
    protected boolean execute() throws Exception {
        String snapshotId = getSnapshotId();

        AppClient client = getBeesClient(AppClient.class);
        Map<String, String> parameters = new HashMap<String, String>();
        if (title != null)
            parameters.put("title", title);
        ApplicationSnapshotStatusResponse res = client.applicationSnapshotUpdate(snapshotId, parameters);

        if (isTextOutput()) {
            System.out.println(String.format("snapshot [%s]: %s", snapshotId, res.getStatus()));
        } else
            printOutput(res, ApplicationSnapshotStatusResponse.class);

        return true;
    }

}
