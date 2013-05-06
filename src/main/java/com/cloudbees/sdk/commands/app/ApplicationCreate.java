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

import com.cloudbees.api.ApplicationCreateResponse;
import com.cloudbees.api.CIInfo;
import com.cloudbees.api.RepositoryInfo;
import com.cloudbees.sdk.cli.BeesCommand;
import com.cloudbees.sdk.cli.CLICommand;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Fabian Donze
 */
@BeesCommand(group="Application", description = "Create a new application")
@CLICommand("app:create")
public class ApplicationCreate extends ApplicationBase {
    private Boolean withCD;
    private Boolean withCI;
    private Boolean withRepo;
    private Boolean useApp;
    private String template;
    private String repoType;
    private String ciType;
    private String type;
    private String useRepo;
    private String ciOptions;
    private Map<String, String> applicationParameters = new HashMap<String, String>();
    private Map<String, String> configVariables = new HashMap<String, String>();
    private Map<String, String> runtimeParameters = new HashMap<String, String>();

    public ApplicationCreate() {
        setArgumentExpected(0);
    }

    public void setWithCD(Boolean withCD) {
        this.withCD = withCD;
    }

    public boolean withCD() {
        return withCD == null ? false : withCD;
    }

    public void setWithCI(Boolean withCI) {
        this.withCI = withCI;
    }

    public boolean withCI() {
        return withCI == null ? false : withCI;
    }

    public boolean withRepo() {
        return withRepo == null ? false : withRepo;
    }

    public void setWithRepo(Boolean withRepo) {
        this.withRepo = withRepo;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public boolean useExistingApp() {
        return useApp == null ? false : useApp;
    }

    public void setUseApp(Boolean useApp) {
        this.useApp = useApp;
    }

    public String getRepoType() {
        return repoType != null ? repoType : "git";
    }

    public void setRepoType(String repoType) {
        this.repoType = repoType;
    }

    public String getCiType() {
        return ciType != null ? ciType : "maven";
    }

    public void setCiType(String ciType) {
        this.ciType = ciType;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setUseRepo(String useRepo) {
        this.useRepo = useRepo;
    }

    public String getCiOptions() {
        return ciOptions;
    }

    public void setCiOptions(String ciOptions) {
        this.ciOptions = ciOptions;
    }

    public Map<String, String> getConfigVariables() {
        return configVariables;
    }

    public void setP(String var) {
        var = var.trim();
        int idx = isParameter(var);
        if (idx > -1) {
            configVariables.put(var.substring(0, idx), var.substring(idx + 1));
        }
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

    @Override
    protected String getUsageMessage() {
        return "APPLICATION_ID [parameterX=valueY]";
    }

    @Override
    protected boolean preParseCommandLine() {
        if(super.preParseCommandLine()) {
            addOption( null, "withRepo", false, "with repository (create a repository), default: GIT");
            addOption( "re", "useRepo", true, "existing repository url");
            addOption( null, "withCI", false, "with Continuous Integration (create|use a repository and create a Continuous Integration job)");
            addOption( null, "withCD", false, "with Continuous Deployment (create|use a repository and create a Continuous Integration job with application Deployment)");
            addOption( null, "ciOptions", true, "Continuous Integration optional parameters", true);
            addOption( null, "useApp", false, "use existing application (do not create a new one) if the application already exists");
            addOption( "at", "template", true, "Application archetype url template (i.e. git://git.cloudbees.com/cloudbees/the-repository-name)");
            addOption( "rt", "repoType", true, "the repository type [git, svn], default: git", true);
            addOption( "ct", "ciType", true, "the Continuous Integration type [maven, play], default: maven");
            addOption( "t", "type", true, "the deployment container type [tomcat, jboss], default: tomcat");
            addOption( "P", null, true, "Application config parameter name=value" );
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
                    applicationParameters.put(str.substring(0, idx), str.substring(idx + 1));
                }
            }
            return true;
        }
        return false;
    }

    @Override
    protected boolean execute() throws Exception {
        String appid = getAppId();

        Map<String, String> parameters = new HashMap();
        if (withRepo()) {
            parameters.put("repo_type", getRepoType());
        }
        if (useRepo != null) {
            parameters.put("repo_url", useRepo);
        }
        if (withCI()) {
            parameters.put("repo_type", getRepoType());
            parameters.put("ci_type", getCiType());
            parameters.put("with_cd", "false");
        }
        if (withCD()) {
            parameters.put("repo_type", getRepoType());
            parameters.put("ci_type", getCiType());
            parameters.put("with_cd", "true");
        }
        if (getCiOptions() != null) {
            parameters.put("ci_options", getCiOptions());
        }
        if (getTemplate() != null) {
            parameters.put("repo_template", getTemplate());
        }
        if (useExistingApp()) {
            parameters.put("use_existing_app", "true");
        }
        if (getType() != null) {
            parameters.put("app_type", getType());
        }

        Map<String, String> rts = getRuntimeParameters();
        if (rts.size() > 0) {
            for (Map.Entry<String,String> entry : rts.entrySet()) {
                applicationParameters.put("runtime." + entry.getKey(), entry.getValue());
            }
        }

        AppClient client = getBeesClient(AppClient.class);

        ApplicationCreateResponse res = client.applicationCreate(appid, parameters, applicationParameters, getConfigVariables());
        if (isTextOutput()) {
            com.cloudbees.api.ApplicationInfo applicationInfo = res.getApplicationInfo();
            System.out.println("Application: " + applicationInfo.getId());
            System.out.println("\turl: " + applicationInfo.getUrls()[0]);
            RepositoryInfo repositoryInfo = res.getRepository();
            if (repositoryInfo != null) {
                if (repositoryInfo.getName() != null)
                    System.out.println("Repository: " + repositoryInfo.getName());
                else
                    System.out.println("Repository");
                if (repositoryInfo.getUrl() != null)
                    System.out.println("\tconsole url: " + repositoryInfo.getUrl());
                System.out.println("\turl: " + repositoryInfo.getAuthenticatedUrl());
            }
            CIInfo ciInfo = res.getCIInfo();
            if (ciInfo != null) {
                System.out.println("CI Job: " + ciInfo.getJobName());
                System.out.println("\turl: " + ciInfo.getJobUrl());
            }
        } else
            printOutput(res, ApplicationCreateResponse.class, ApplicationInfo.class, RepositoryInfo.class, CIInfo.class);

        return true;
    }
}
