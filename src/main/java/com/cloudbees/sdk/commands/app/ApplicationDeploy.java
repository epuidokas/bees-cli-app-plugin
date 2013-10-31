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

import com.cloudbees.api.ApplicationDeployArchiveResponse;
import com.cloudbees.api.ApplicationDeployArgs;
import com.cloudbees.api.HashWriteProgress;
import com.cloudbees.sdk.cli.BeesCommand;
import com.cloudbees.sdk.cli.CLICommand;
import com.cloudbees.sdk.utils.Helper;
import com.staxnet.appserver.config.AppConfig;
import com.staxnet.appserver.utils.StringHelper;
import com.staxnet.appserver.utils.ZipHelper;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipOutputStream;

/**
 * @author Fabian Donze
 */
@BeesCommand(group="Application", description = "Deploy an application")
@CLICommand("app:deploy")
public class ApplicationDeploy extends ApplicationBase {

    /**
     * Configuration environments to use.
     */
    private String environment;

    /**
     * Message associated with the deployment.
     */
    private String message;

    /**
     * The path to the Bees deployment descriptor.
     *
     * parameter expression="${bees.appxml}" default-value =
     *            "${basedir}/src/main/config/stax-application.xml"
     */
    private File appConfig;

    /**
     * The path to the J2EE appplication deployment descriptor.
     *
     * parameter expression="${bees.j2ee.appxml}" default-value = "${basedir}/src/main/config/application.xml"
     */
    private File appxml;

    /**
     */
    private String baseDir;

    /**
     * The war file
     *
     */
    private File warFile;

    /**
     * The packaged deployment file.
     *
     */
    private File deployFile;

    /**
     * Bees deployment type.
     *
     * parameter expression="${bees.delta}" default-value = "true"
     */
    private String delta;

    private String descriptorDir;

    private String type;

    private Map<String, String> vars = new HashMap<String, String>();

    private Map<String, String> runtimeParameters = new HashMap<String, String>();

    public ApplicationDeploy() {
    }

    @Override
    protected String getUsageMessage() {
        return "ARCHIVE_FILE | ARCHIVE_DIRECTORY";
    }

    @Override
    protected boolean preParseCommandLine() {
        if (super.preParseCommandLine()) {
            // add the Options
            addOption( "m", "message", true, "Message describing the deployment" );
            addOption( "e", "environment", true, "Environment configurations to deploy" );
            addOption( "d", "delta", true, "true to enable, false to disable delta upload (default: true)" );
            addOption( "b", "baseDir", true, "Base directory (default: '.')");
            addOption("xd", "descriptorDir", true, "Directory containing application descriptors (default: 'src/main/conf/')", true);
            addOption("t", "type", true, "Application container type");
            addOption( "P", null, true, "Application config parameter name=value" );
            addOption( "R", null, true, "Runtime config parameter name=value" );

            return true;
        }
        return false;
    }

    @Override
    protected boolean postParseParameters() {
        boolean ok = true;
        List<String> otherArgs = getParameters();
        if (otherArgs.size() > 0) {
            setWarFile(new File(getBaseDir(), otherArgs.get(0)));
        }
        if (otherArgs.size() == 0 || warFile == null)
            ok = false;

        return ok;
    }

    @Override
    protected void initDefaults(Properties properties) {
        super.initDefaults(properties);
        setAppxml(new File(getDescriptorDir(), "application.xml"));
        setAppConfig(new File(getDescriptorDir(), "stax-application.xml"));
    }


    protected void setAppConfig(File appConfig) {
        this.appConfig = appConfig;
    }

    protected void setAppxml(File appxml) {
        this.appxml = appxml;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    public String getBaseDir() {
        return baseDir;
    }

    protected void setWarFile(File warFile) {
        this.warFile = warFile;
    }

    public String getDescriptorDir() {
        return descriptorDir == null ? "src/main/conf" : descriptorDir;
    }

    public void setDescriptorDir(String descriptorDir) {
        this.descriptorDir = descriptorDir;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setDelta(String delta) {
        this.delta = delta;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, String> getConfigVariables() {
        return vars;
    }

    public void setP(String var) {
        var = var.trim();
        int idx = isParameter(var);
        if (idx > -1) {
            vars.put(var.substring(0, idx), var.substring(idx+1));
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
    protected boolean execute() throws Exception {

        // create the deployment package
        if(appConfig.exists() && appxml.exists())
        {
            FileOutputStream fstream = null;
            try {
                deployFile = new File(warFile.getParent(), warFile.getName() + ".ear");
                fstream = new FileOutputStream(deployFile);
                ZipOutputStream zos = new ZipOutputStream(fstream);
                ZipHelper.addFileToZip(warFile, "webapp.war", zos);
                ZipHelper.addFileToZip(appConfig, "META-INF/stax-application.xml", zos);
                ZipHelper.addFileToZip(appxml, "META-INF/application.xml", zos);
                zos.close();
            } catch (Exception e) {
                throw new RuntimeException("Package failure: " + e.getMessage(), e);
            }
        }
        else
        {
            if (warFile.isDirectory()) {
                // zip the directory
                deployFile = new File(warFile.getParent(), "deploy.zip");
                FileOutputStream fileOutputStream = new FileOutputStream(deployFile);
                ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(fileOutputStream));

                ZipHelper.addDirectoryToZip(warFile, null, null, zipOutputStream);

                close(zipOutputStream);
                close(fileOutputStream);

                deployFile.deleteOnExit();
            } else {
                deployFile = warFile;
            }
        }

        // deploy the application to the server
        AppConfig appConfig = AppHelper.getAppConfig(  deployFile,
                                                    Helper.getEnvironmentList(environment),
                                                    new String[]{"deploy"});

        String appid = getAppid();
        appid = initAppId(appid, appConfig);

        environment = StringHelper.join(appConfig.getAppliedEnvironments().toArray(new String[0]), ",");

        System.out.println(String.format("Deploying application %s (environment: %s): %s", appid, environment, deployFile));
        AppClient client = getAppClient(appid);

        boolean deployDelta = (delta == null || delta.equalsIgnoreCase("true")) ? true : false;

        Map<String, String> parameters = new HashMap<String, String>();
        if (type != null)
            parameters.put("containerType", type);

        List<String> otherArgs = getParameters();
        for (int i = 1; i<otherArgs.size(); i++) {
            String str = otherArgs.get(i);
            int idx = isParameter(str);
            if (idx > -1)
                parameters.put(str.substring(0, idx), str.substring(idx+1));
        }
        if (parameters.size() > 0)
            System.out.println("Application parameters: " + parameters);

        Map<String, String> rts = getRuntimeParameters();
        if (rts.size() > 0) {
            System.out.println("Runtime parameters: " + rts);
            for (Map.Entry<String,String> entry : rts.entrySet()) {
                parameters.put("runtime." + entry.getKey(), entry.getValue());
            }
        }

        Map<String, String> variables = getConfigVariables();
        if (variables.size() > 0)
            System.out.println("Config parameters: " + variables);

        String deployType;
        if(deployFile.getName().endsWith(".war")) {
            deployType = "war";
        } else if(deployFile.getName().endsWith(".zip")) {
            deployType = "zip";
        } else if(deployFile.getName().endsWith(".jar")) {
            deployType = "jar";
        } else {
            deployType = "ear";
            deployDelta = false;
        }

        ApplicationDeployArgs.Builder argBuilder = new ApplicationDeployArgs
                .Builder(appid)
                .environment(environment)
                .description(message).deployPackage(deployFile, deployType)
                .withParams(parameters)
                .withVars(getConfigVariables())
                .incrementalDeployment(deployDelta)
                .withProgressFeedback(new HashWriteProgress());

        ApplicationDeployArchiveResponse res = client.applicationDeployArchive(argBuilder.build());

        if (isTextOutput())
            System.out.println("Application " + res.getId() + " deployed: " + res.getUrl());
        else
            printOutput(res, ApplicationDeployArchiveResponse.class);

        return true;
    }

    protected String initAppId(String appid, AppConfig appConfig) throws IOException
    {
        if (appid == null || appid.equals("")) {
            appid = appConfig.getApplicationId();

            if (appid == null || appid.equals(""))
                appid = Helper.promptForAppId();

            if (appid == null || appid.equals(""))
                throw new IllegalArgumentException("No application id specified");
        }

        String[] appIdParts = appid.split("/");
        if (appIdParts.length < 2) {
            String defaultAppDomain = getAccount();
            if (defaultAppDomain != null && !defaultAppDomain.equals("")) {
                appid = defaultAppDomain + "/" + appid;
            } else {
                throw new RuntimeException("default app account could not be determined, appid needs to be fully-qualified ");
            }
        }

        return appid;
    }

    protected static void close(Closeable closeable) {
        try {
            if (closeable != null)
                closeable.close();
        } catch (IOException ignored) {
        }
    }

}
