/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ctask.replicate;

import java.util.List;

import org.apache.log4j.Logger;
import org.dspace.content.packager.PackageParameters;
import org.dspace.curate.AbstractCurationTask;

/**
 * AbstractPackagerTask encapsulates a few common convenience methods which may
 * be useful to curation tasks that wrap or utilize DSpace Packager classes
 * (org.dspace.content.packager.*).
 *
 * @author Tim Donohue
 * @see org.dspace.app.packager.Packager
 * @see org.dspace.content.packager.PackageDisseminator
 * @see org.dspace.content.packager.PackageIngester
 */
public abstract class AbstractPackagerTask extends AbstractCurationTask
{
    // Name of recursive mode option configurable in curation task configuration file
    private final String recursiveMode = "recursiveMode";

    // Name of useWorkflow option configurable in curation task configuration file
    private final String useWorkflow = "useWorkflow";

    // Name of useCollectionTemplate option configurable in curation task configuration file
    private final String useCollectionTemplate = "useCollectionTemplate";

    private static Logger log = Logger.getLogger(AbstractPackagerTask.class);

    /**
     * Loads pre-configured PackageParameters settings from a given Module
     * configuration file (specified by 'moduleName').
     * <p>
     * These PackageParameters should be configured using the following
     * configuration file format:
     * <p>
     * SETTING FORMAT: [modulename].[taskname].[option] = [value]
     * <p>
     * Valid 'options' include all packager options supported by the
     * Packager class, e.g. AIP packagers minimally support these options:
     * https://wiki.lyrasis.org/display/DSDOC6x/AIP+Backup+and+Restore#AIPBackupandRestore-AdditionalPackagerOptions
     * <p>
     * Please note that different Packager classes will support different options.
     * You should determine which options are valid for your Packager class
     * and the curation task that utilizes it.
     * <p>
     * Example usage: if your module is named "mymodule" and your curation task is  named
     * "myreplacetask" in curate.cfg, then you can configure its PackageParameters like so:
     * <p>
     * mymodule.myreplacetask.replaceMode = true
     * mymodule.myreplacetask.recursiveMode = true
     * mymodule.myreplacetask.createMetadataFields = true
     * mymodule.myreplacetask.[any-supported-option] = [any-supported-value]
     *
     * @param moduleName Module name to load configuration file and settings from
     * @return configured PackageParameters (or null, if configurations not found)
     * @see org.dspace.content.packager.PackageParameters
     */
    protected PackageParameters loadPackagerParameters(String moduleName)
    {
        log.info("*** Entering AbstractPackagerTask");

        //Load up the replicate-mets.cfg file & all settings inside it
        List<String> moduleProps = configurationService.getPropertyKeys(moduleName);

        log.info("*** ModuleProps: " + moduleProps);

        PackageParameters pkgParams = new PackageParameters();

        //If our config file doesn't load properly, we'll return null
        if(moduleProps!=null)
        {
            log.info("*** ModuleProps not null");

            //loop through all properties in the config file
            for(String property : moduleProps)
            {
                log.info("*** Parameter Property: " + property);

                //Remove leading module name (if applicable)
                if(property.startsWith(moduleName + ".")) {
                    property = property.replaceFirst(moduleName + ".", "");
                    log.info("*** Property name reset to: " + property);
                }

                //Only obey the setting(s) beginning with this task's ID/name,
                if(property.startsWith(this.taskId))
                {
                    log.info("*** Property " + property + " does start with " + this.taskId);

                    //Parse out the option name by removing the "[taskID]." from beginning of property
                    String option = property.replace(taskId + ".", "");
                    String value = configurationService.getProperty(property);

                    log.info("*** Option: " + option + " Value: " + value);

                    //Check which option is being set
                    if(option.equalsIgnoreCase(recursiveMode))
                    {
                        log.info("*** Setting " + recursiveMode + " to " + Boolean.parseBoolean(value));
                        pkgParams.setRecursiveModeEnabled(Boolean.parseBoolean(value));
                    }
                    else if (option.equals(useWorkflow))
                    {
                        log.info("*** Setting " + useWorkflow + " to " + Boolean.parseBoolean(value));
                        pkgParams.setWorkflowEnabled(Boolean.parseBoolean(value));
                    }
                    else if (option.equals(useCollectionTemplate))
                    {
                        log.info("*** Setting " + useCollectionTemplate + " to " + Boolean.parseBoolean(value));
                        pkgParams.setUseCollectionTemplate(Boolean.parseBoolean(value));
                    }
                    else //otherwise, assume the Packager will understand what to do with this option
                    {
                        log.info("*** Setting " + option + " to " + Boolean.parseBoolean(value));
                        //just set it as a property in PackageParameters
                        pkgParams.addProperty(option, value);
                    }
                } else {
                    log.info("*** Property " + property + " DOES NOT start with " + this.taskId);
                }
            }

            log.info("*** Final package params:" + pkgParams.toString());
            return pkgParams;
        } else {
            log.warn("*** ModuleProps ARE NULL!!!");
            return null;
        }
    }
}
