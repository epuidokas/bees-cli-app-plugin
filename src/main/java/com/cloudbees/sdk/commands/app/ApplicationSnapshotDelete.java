package com.cloudbees.sdk.commands.app;

import com.cloudbees.api.ApplicationInstanceStatusResponse;
import com.cloudbees.api.ApplicationSnapshotStatusResponse;
import com.cloudbees.sdk.cli.BeesCommand;
import com.cloudbees.sdk.cli.CLICommand;
import com.cloudbees.sdk.utils.Helper;

/**
 * @author Fabian Donze
 */
@BeesCommand(group="Application", description = "Delete an application snapshot")
@CLICommand("app:snapshot:delete")
public class ApplicationSnapshotDelete extends ApplicationSnapshotBase {
    private Boolean force;

    public ApplicationSnapshotDelete() {
        super();
    }

    public void setForce(Boolean force) {
        this.force = force;
    }

    protected boolean preParseCommandLine() {
        if (super.preParseCommandLine()) {
            addOption( "f", "force", false, "force delete without prompting" );
            return true;
        }
        return false;
    }

    @Override
    protected boolean execute() throws Exception {
        String snapshotId = getSnapshotId();

        if (force == null || !force.booleanValue()) {
            if (!Helper.promptMatches("Are you sure you want to delete this snapshot [" + snapshotId + "]: (y/n) ", "[yY].*")) {
                return true;
            }
        }

        AppClient client = getBeesClient(AppClient.class);
        ApplicationSnapshotStatusResponse res = client.applicationSnapshotDelete(snapshotId);

        if (isTextOutput()) {
            System.out.println(String.format("snapshot [%s]: %s",snapshotId, res.getStatus()));
        } else
            printOutput(res, ApplicationSnapshotStatusResponse.class);

        return true;
    }

}
