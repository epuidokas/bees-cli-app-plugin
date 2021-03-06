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

import com.cloudbees.api.config.ResourceSettings;
import com.cloudbees.sdk.CommandServiceImpl;
import com.cloudbees.sdk.Plugin;
import com.cloudbees.sdk.cli.BeesCommand;
import com.cloudbees.sdk.cli.CLICommand;
import com.cloudbees.sdk.cli.CommandService;
import com.cloudbees.sdk.utils.Helper;
import com.cloudbees.utils.ZipHelper;
import com.staxnet.appserver.config.AppConfig;
import com.staxnet.appserver.config.AppConfigHelper;

import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Fabian Donze
 */
@BeesCommand(group="Application", description = "Run an application locally")
@CLICommand("app:run")
public class ApplicationRun extends ApplicationBase {

    @Inject
    private CommandService commandService;
    /**
     * Configuration environments to use.
     */
    private String environment;

    /**
     * The path to the Bees deployment descriptor. (stax-application.xml)
     */
    private File appConfig;

    /**
     */
    private String tmpDir;

    private String port;

    /**
     */
    private String descriptorDir;

    /**
     * The war file
     */
    private File warFile;

    private Boolean noResourceFetch;
    private String account;

    private Thread server;

    public ApplicationRun() {
        setArgumentExpected(1);
    }

    protected boolean fetchResources() {
        return noResourceFetch == null || !noResourceFetch;
    }

    public void setNoResourceFetch(Boolean noResourceFetch) {
        this.noResourceFetch = noResourceFetch;
    }

    private String getDefaultDomain() {
        return getConfigProperties().getProperty("bees.project.app.domain");
    }

    @Override
    protected String getUsageMessage() {
        return "WAR_Filename | WAR_directory";
    }

    @Override
    protected boolean preParseCommandLine() {
        if (super.preParseCommandLine()) {
        // add the Options
            addOption(null, "port", true, "server listen port (default: 8080)");
            addOption("e", "environment", true, "Environment configurations to run");
            addOption("t", "tmpDir", true, "Local working directory where temp files can be created (default: 'tmp')");
            addOption("xd", "descriptorDir", true, "Directory containing application descriptors (default: 'conf')", true);
            addOption(null, "noResourceFetch", false, "do not fetch application resources");

            return true;
        }
        return false;
    }

    @Override
    protected boolean postParseParameters() {
        setWarFile(new File(getParameters().get(0)));
        return true;
    }

    @Override
    protected void initDefaults(Properties properties) {
        super.initDefaults(properties);
        setAppConfig(new File(getDescriptorDir(), "stax-application.xml"));
    }


    protected void setAppConfig(File appConfig) {
        this.appConfig = appConfig;
    }

    protected File getWebroot() {
        return new File(getTmpDir(), "webapp");
    }

    protected File getWorkDir() {
        return new File(getTmpDir(), "workdir");
    }

    public void setTmpDir(String tmpDir) {
        this.tmpDir = tmpDir;
    }

    protected void setWarFile(File warFile) {
        this.warFile = warFile;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public void setDescriptorDir(String descriptorDir) {
        this.descriptorDir = descriptorDir;
    }

    public String getTmpDir() {
        return tmpDir == null ? "tmp" : tmpDir;
    }

    public String getDescriptorDir() {
        return descriptorDir == null ? "conf" : descriptorDir;
    }

    public int getPort() {
        return port == null ? 8080 : Integer.parseInt(port);
    }

    public void setPort(String port) {
        this.port = port;
    }

    boolean cleanWebRoot = false;
    boolean deleteAppserverXML = false;
    File webRoot = null;
    File appserverXML;

    @Override
    protected boolean execute() throws Exception {

        // run the application in a local server
        // Unpack the war file
        //deleteAll(new File(getTmpDir()));

        if (warFile.exists() && !warFile.isDirectory()) {
            cleanWebRoot = true;
            webRoot = getWebroot();
            webRoot.mkdirs();

            ZipFile zipFile = new ZipFile(warFile.getAbsolutePath());
            Enumeration<? extends ZipEntry> e = zipFile.entries();
            while (e.hasMoreElements()) {
                ZipEntry entry = e.nextElement();
                ZipHelper.unzipEntryToFolder(entry, zipFile.getInputStream(entry), getWebroot());
            }
            zipFile.close();

            // Delete on exit
            Helper.deleteDirectoryOnExit(webRoot);
        } else {
            webRoot = warFile;
        }

        File staxWebXml = new File(webRoot, "WEB-INF/cloudbees-web.xml");
        if (!staxWebXml.exists())
            staxWebXml = new File(webRoot, "WEB-INF/stax-web.xml");

        appserverXML = new File("appserver.xml");
        if (!appserverXML.exists()) {
            appserverXML = new File(getDescriptorDir(), "appserver.xml");
            if (!appserverXML.exists()) {
                appserverXML = new File(webRoot, "WEB-INF/cloudbees-appserver.xml");
                if (appserverXML.exists()) appserverXML.delete();
            }
        }

        // Create the appserver.xml
        String appid = getAppid();
        if (fetchResources() && appid != null) {
            System.out.println("Get application resources...");
            List<ResourceSettings> res = null;
            try {
                AppClient client = getAppClient(getAppId());

                res = client.applicationResources(getAppId(appid, null, null), null, null, environment);

                if (res != null && res.size() > 0) {
                    // Generate appserver.xml file
                    AppServerXML appServerXMLConfig = new AppServerXML();
                    appServerXMLConfig.setResources(res);

                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(appserverXML);
                        AppServerXML.toXML(appServerXMLConfig, fos);
                        deleteAppserverXML = true;
                    } finally {
                        if (fos != null)
                            fos.close();
                    }
                }
            } catch (Exception e) {
                System.err.println("WARNING: Cannot retrieving application resources: " + e.getMessage());
            }
        }

        String[] environments = getEnvironments(appConfig, environment);

        // Create a special class loader for tomcat without any of the SDK classes
        Plugin plugin = ((CommandServiceImpl)commandService).getPlugin("app-plugin");
        ClassLoader classLoader = createClassLoader(plugin.getJars(), ClassLoader.getSystemClassLoader().getParent());

        Object[] rc = new Object[]{appserverXML, environments, getTmpDir(), webRoot, new Integer(getPort()), staxWebXml};
        Class<?> runUtilClass = Class.forName("com.cloudbees.sdk.commands.app.run.RunUtil", true, classLoader);
        Constructor c = runUtilClass.getConstructors()[0];
        c.setAccessible(true);
        server = (Thread) c.newInstance(rc);
        server.start();

        return true;
    }

    private ClassLoader createClassLoader(List<String> jars, ClassLoader parent) throws MalformedURLException {
        List<URL> urls = new ArrayList<URL>();
        if (jars != null) {
            for (String jar : jars) {
                urls.add(new File(jar).toURI().toURL());
            }
        }
        return new URLClassLoader(urls.toArray(new URL[urls.size()]), parent);
    }

    private String[] getEnvironments(File staxappxmlFile, String envString)
    {
        if(envString == null || envString.equals("") && staxappxmlFile != null && staxappxmlFile.exists())
        {
            //load the default environment, and append the run environment
            AppConfig appConfig = new AppConfig();
            AppConfigHelper.load(appConfig, staxappxmlFile.getAbsolutePath(), new String[0], new String[0]);

            envString = appConfig.getDefaultEnvironment();
        }

        String[] environment = Helper.getEnvironmentList(envString, "run");
        return environment;
    }

    protected String getAppId(String appid, File configFile, String[] environments) throws IOException
    {
        if ((appid == null || appid.equals("")) && configFile != null && configFile.exists()) {
            FileInputStream fis = new FileInputStream(configFile);
            AppConfig appConfig = new AppConfig();
            AppConfigHelper.load(appConfig, fis, null, environments, new String[] { "deploy" });
            appid = appConfig.getApplicationId();
            fis.close();
        }

        if (appid == null || appid.equals(""))
            appid = Helper.promptForAppId();

        if (appid == null || appid.equals(""))
            throw new IllegalArgumentException("No application id specified");

        String[] parts = appid.split("/");
        if (parts.length < 2)
            appid = getAccount() + "/" + appid;

        return appid;
    }

    @Override
    public void stop() {
        if (server != null) server.interrupt();
        if (cleanWebRoot) Helper.deleteDirectory(webRoot);
        if (deleteAppserverXML && appserverXML.exists())
            appserverXML.delete();
    }

}
