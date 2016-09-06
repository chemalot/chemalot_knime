Developer Documentation for chamalot\_knime
============

This document describes how to setup a working development environment that
allows you to debug and modify the Java code for the chemalot\_knime command line nodes.

We recommend that before installing the chamalot\_knime package, you install the chemalot command line tools on your UNIX server and make sure they are working and included in your default path. The KNIME command line package can be configured to run any command line tools that read and write SDF files on stdin/stdout. This description assumes that you will use the KNIME command line package with the chemalot command line tools.

Installing the KNIME SDK
----------------------
* Download KNIME SDK from: https://www.knime.org/downloads/overview. This document was written and tested using the 3.2.0 version of the KNIME SDK.
   * Install into `<KNIMESDK_DIR>` 

* Checkout [chemalot-KNIME](??) to: `<chemalotSDK_knime_dir>`

  
* Start `<KNIMESDK_DIR>`/eclipse.exe and select a workspace

  * Goto Help/Install New Software
  * Add new update site for KNIME e.g. http://update.knime.org/analytics-platform/3.2/
  
     * Check only the following in "KNIME Extensions"
 
         * KNIME Base Chemistry Types and Nodes
         * KNIME Chemistry Add-Ons
         * KNIME External Tool Support
         
     * Check KNIME Node Development Tools
     * Complete the installation with defaults
  * Restart KNIME

  * Goto File/Import/General/Existing Project into Workspace  
     * Specify root dir: `<chemalotSDK_knime_dir>`
     * Check search for nested projects
     * Click Finish

     
  * Look in the "Problems" tab: There should be no errors.

  * Edit the 
     `<chemalotSDK_knime_dir>`/config/dev.properties and `<chemalotSDK_knime_dir>`/config/cmdLine/commandLinePrograms.xml  
     files as described in the [readme](readme.html) file for the installation of the nodes in a working KNIME desktop.

  * Create a new KNIME Runtime Configuration
     
     From the KNIME documentation:  
     *In order to launch KNIME you need to create a new runtime configuration: open the Run menu and click "Run Configurations..." Within this frame a new runtime configuration can be set up by right-clicking the Eclipse Application tag. Specify a new workspace directory and select org.knime.product.KNIME_APPLICATION as Run an application option. Inside the Arguments tab we recommend using the following VM arguments: -ea -Xmx1G -XX:MaxPermSize=512M. Click Apply to apply the changes and click Run to launch KNIME.*
     
     Add these two options to the VM arguments   
        -DGNEKnimeConfigDir=`<chemalotSDK_knime_dir>`/config  
        -DGNEKnimePropertiesFile=`<chemalotSDK_knime_dir>`/config/dev.properties
   * Start the new runtime configuration. A KNIME window should be opening up.
      * Goto File/Preferences/General/Network Connections/ssh2
         * Configure ssh connection by adding a private key 
         * Make sure your UNIX host has the private key installed
         * For details look at [readme.md](readme.md)
      * Goto File/import/KNIME Workflow
        * Select the file `<chemalotSDK_knime_dir`>\test\Command Line Knime Test.knar
        * Select "Finish"
        * Open the "Command Line Knime Test/Test" workflow
        * Double click the "Remote SDF Reader"
           * Make sure the SDF file referenced in the "command line options" box exists on the remote server. The path is relative to your home directory on the remote server.
           * Verify the ssh settings in the SSH tab
        * Execute the workflow and verify that the output SDF file has one additional column called "counter"
     
   * Congratulations, you have now a working development version of the KNIME SDK and will be able to make modifications to the chemalot\_knime code. We are looking forward to hearing from you about the exciting modifications you are making.


Creating an updated version of the update site
-------------------------------------
* Make changes to the Java code.
* Test the modification by running and debugging them in the runtime configuration.
* Follow the instructions in [deploy.txt](deploy.txt)
     
