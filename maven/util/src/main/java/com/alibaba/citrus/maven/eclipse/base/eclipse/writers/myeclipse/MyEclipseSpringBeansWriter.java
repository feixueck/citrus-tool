package com.alibaba.citrus.maven.eclipse.base.eclipse.writers.myeclipse;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import com.alibaba.citrus.maven.eclipse.base.eclipse.Messages;
import com.alibaba.citrus.maven.eclipse.base.eclipse.writers.AbstractEclipseWriter;
import com.alibaba.citrus.maven.eclipse.base.ide.IdeUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.xml.PrettyPrintXMLWriter;
import org.codehaus.plexus.util.xml.XMLWriter;

/**
 * MyEclipse .springBeans configuration file writer
 *
 * @author Olivier Jacob
 */
public class MyEclipseSpringBeansWriter
        extends AbstractEclipseWriter {
    private static final String MYECLIPSE_SPRING_CONFIGURATION_FILENAME = ".springBeans";

    private static final String MYECLIPSE_SPRING_BEANS_PROJECT_DESCRIPTION = "beansProjectDescription";

    private static final String MYECLIPSE_SPRING_CONFIG_EXTENSIONS = "configExtensions";

    private static final String MYECLIPSE_SPRING_CONFIG_EXTENSION = "configExtension";

    private static final String MYECLIPSE_SPRING_CONFIGS = "configs";

    private static final String MYECLIPSE_SPRING_CONFIG = "config";

    private static final String MYECLIPSE_SPRING_CONFIGSETS = "configSets";

    private static final String MYECLIPSE_SPRING_VERSION = "springVersion";

    /** Spring configuration filenames (injected by the plugin) */
    private Map springConfig;

    /**
     * Allow injection of Spring configuration filenames through constructor
     *
     * @param springConfig a map holding Spring configuration properties
     */
    public MyEclipseSpringBeansWriter(Map springConfig) {
        this.springConfig = springConfig;
    }

    /**
     * Write MyEclipse .springBeans configuration file
     *
     * @throws MojoExecutionException
     */
    public void write()
            throws MojoExecutionException {
        FileWriter springFileWriter;
        try {
            springFileWriter =
                    new FileWriter(new File(config.getEclipseProjectDirectory(),
                                            MYECLIPSE_SPRING_CONFIGURATION_FILENAME));
        } catch (IOException ex) {
            throw new MojoExecutionException(Messages.getString("EclipsePlugin.erroropeningfile"), ex); //$NON-NLS-1$
        }

        XMLWriter writer = new PrettyPrintXMLWriter(springFileWriter, "UTF-8", null);

        writer.startElement(MYECLIPSE_SPRING_BEANS_PROJECT_DESCRIPTION);
        // Configuration extension
        writer.startElement(MYECLIPSE_SPRING_CONFIG_EXTENSIONS);
        writer.startElement(MYECLIPSE_SPRING_CONFIG_EXTENSION);
        writer.writeText("xml");
        writer.endElement();
        writer.endElement();

        // Configuration files
        writer.startElement(MYECLIPSE_SPRING_CONFIGS);

        // maven's cwd stays at the top of hierarchical projects so we
        // do this with full path so it works as we descend through various modules (projects)
        File basedir = config.getEclipseProjectDirectory();
        Iterator onConfigFiles =
                getConfigurationFilesList(new File(basedir, (String) springConfig.get("basedir")),
                                          (String) springConfig.get("file-pattern")).iterator();

        while (onConfigFiles.hasNext()) {
            String onConfigFileName = (String) onConfigFiles.next();
            File onConfigFile = new File(onConfigFileName);
            String relativeFileName = IdeUtils.toRelativeAndFixSeparator(basedir, onConfigFile, false);

            writer.startElement(MYECLIPSE_SPRING_CONFIG);
            writer.writeText(relativeFileName);
            writer.endElement();
        }
        writer.endElement();

        // Configuration sets
        writer.startElement(MYECLIPSE_SPRING_CONFIGSETS);
        writer.endElement();

        // Spring version
        writer.startElement(MYECLIPSE_SPRING_VERSION);
        writer.writeText((String) springConfig.get("version"));
        writer.endElement();

        writer.endElement();

        IOUtil.close(springFileWriter);
    }

    /**
     * Retrieve the list of Spring configuration files recursively from the <code>basedir</code> directory, considering
     * only filenames matching the <code>pattern</code> given
     *
     * @param basedir the path to the base directory to search in
     * @param pattern file include pattern
     * @return the list of filenames matching the given pattern
     */
    private Collection getConfigurationFilesList(File basedir, String pattern) {
        ArrayList configFiles = new ArrayList();

        try {
            if (basedir.exists()) {
                log.debug("Scanning " + basedir + " for spring definition files");
                File[] subdirs = basedir.listFiles(new FileFilter() {
                    public boolean accept(File pathname) {
                        return pathname.isDirectory();
                    }
                });

                if (subdirs != null) {
                    for (int i = 0; i < subdirs.length; i++) {
                        configFiles.addAll(getConfigurationFilesList(subdirs[i], pattern));
                    }
                }

                configFiles.addAll(FileUtils.getFileNames(basedir, pattern, null, true));
            } else {
                // This isn't fatal because sometimes we run this in a nested set of
                // projects where some of the projects may not have spring configuration
                log.warn(Messages.getString("MyEclipseSpringBeansWriter.baseDirDoesNotExist",
                                            new Object[] { basedir }));
            }
        } catch (IOException ioe) {
            log.error("Error while retrieving Spring configuration files. Returning list in current state");
        }
        // Sort them to have something constant
        Collections.sort(configFiles);
        return configFiles;
    }
}