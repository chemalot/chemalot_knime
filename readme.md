Overview of chemalot\_knime
=============

The chemalot\_knime package contains the chemalot\_knime framework. The chemalot\_knime dynamically creates KNIME nodes from a configuration file that defines command line programs.  
The command line nodes are executed on a remote server using ssh. The framework reads an xml configuration file and automatically creates KNIME nodes that execute command line programs for processing chemical compounds. A few lines of xml in the configuration file suffice to generate a new node. The only requirement for the command line tools is that they read SDF files from stdin and write SDF files to stdout.

In addition to executing the command line tools on a remote server, the nodes can also generate the UNIX csh script to execute the same sequence of tools as a UNIX pipe. The result is that KNIME can be used to develop and debug complex UNIX pipes. Once the pipes have been tested and validated, a light-weight execution is possible by simply extracting the c-shell syntax from the terminal command line node in KNIME and incorporating it into a UNIX csh shell script.

We recommend that before installing the chemalot\_knime package, you install the [chemalot](https://github.com/chemalot/chemalot/) command line tools on your UNIX server and make sure they are working and included in your default path.

The configuration file (`<chemalot_knime_dir>`/config/cmdLine/commandLinePrograms.xml) in this package has entries predefined for all of the chemalot command line tools. The KNIME nodes for those will be auto generated but the nodes will only work if the command line tools themselves are in the user's path on the remote host.

 Licensing
-----------------------
The chemalot\_knime package is released under the GNU General Public License Version 3.

A copy of the license file can be found in the [license](license/gpl-3.0.txt) folder.



Installation
----------------------

This description assumes that you have downloaded and installed the KNIME Desktop from the [KNIME Download Page](https://www.knime.org/downloads/). In this documentation, the directory containing your KNIME executable will be denoted `<KNIME_INSTALL_DIR>`.
To install the chemalot\_knime framework and KINME nodes into an existing KNIME environment, follow these steps:

The installation was tested using the 3.2.0 version of KNIME. It requires the 3.2.0 version but might work with newer versions.
Make sure you have the following modules installed:

* KNIME Base Chemistry Types & Nodes

* KNIME Chemistry Add-Ons

* KNIME External Tool Support  

* If you do not, goto "Help/Install New Software" in KNIME desktop and

   * choose *http://update.knime.org/analytics-platform/3.2* as update site
   * check the required modules mentioned above
   * follow the instructions to install the modules.

Installation of the chemalot\_knime package:

* Download and unpack the newest chemalot\_knime_Install.*.zip file from the [chemalot\_knime GitHub page](https://github.com/chemalot/chemalot_knime) onto your hard drive.
* Unzip the contents into your preferred program directory. In the rest of this documentation, this unzipped directory will be denoted `<chemalot_knime_dir>`.
* Edit `<KNIME_INSTALL_DIR>`/knime.ini:
   * append the following two lines:
   * -DGNEKnimeConfigDir=`<chemalot_knime_dir>`/config
   * -DGNEKnimePropertiesFile=`<chemalot_knime_dir>`/config/dev.properties
* Edit the `<chemalot_knime_dir>`/config/cmdLine/commandLinePrograms.xml file:
   * Change the `remoteHost` attribute to the `ssh` element so that it contains the remote host on which you want to execute the command line programs
   * Change the `initFileTemplate` attribute to the `ssh` element so that it points to a file that is "sourced" before executing the command line programs to setup the environment.
   * Edit the `exchangeDir` elements; provide the correct paths to the exchange directory.  
     The exchange directories are used by the "Knime to SDF Port" node and are pairs of directories. One directory must be a network drive writable on the local computer, the other must be the path to the same directory for reading files from the remote computer.
* Edit the `<chemalot_knime_dir>`/config/dev.properties file:
   * Edit the `SSHHost` property; enter the remote host name.
   * If you plan to use the command line nodes in the the KNIME Web Portal:
      * edit the `executer.user`. It should contain the username executing the KNIME executer when using the KNIME Web Portal
      * edit the `server.ssh.user`. It should contain the ssh user to be used when executing the workflow in the KNIME Web Portal
   * If you do not plan to use the command line node in the the KNIME Web Portal, specify a non-existing user for these two properties.
   * All properties in this file are made avaialble to the Genentech/Settings node. Feel free to add your own properties.
* Restart KNIME
* Goto "Help/Install New Software" 
   * Click "Add..."
   * Click "local..."
   * Navigate to `<chemalot_knime_dir>`/updateSite
   * Click Open
   * Click OK
   * Check Genentech
   * Click Next and use defaults to complete the installation

For the SDF command line nodes to work, you have to setup ssh using private keys in KNIME:

* Goto File/Preferences/General/Network Connections/ssh2
   * Click the key management tab
   * Click "Generate RSA Key"
   * Click "Save Private Key"
   * Save the key into the directory specified in the "General" tab (usually $HOME/.ssh)
   * Click "OK" to close the dialog box
* This should have saved a file named __$HOME/.ssh/id_rsa.pub__
* Copy this file onto your remote Linux machine into a file named   
   __~/.ssh/authorized\_keys__   
   If the file already exists, append the contents of id_rsa.pub to it.
   
* Goto File/Import/KNIME Workflow
   * Browse to "`<chemalot_knime_dir>`/test/Command Line Knime Test.knar"
   * Import all workflows
   
* Open the "Command Line Knime Test/ExtSSHTest" workflow. This workflow only uses basic KNIME nodes to test your ssh configuration
   * Double click the "External SSH Tool" node
   * In the "Ext SSH Tool Settings" tab, enter Host and username.
     Do NOT enter a password as authentication must work with your public key
   * Uncheck the "Use known Host" box
   * Click "Check Connection"  
     You should get a message saying that the connection was fine.
   * Recheck the  "Use known Host" box
   * Click "Check Connection" again  
     You should get a message saying that the connection was fine.
   * Click OK
   * Execute the workflow and make sure it does not produce errors   
   
Test the chemalot\_knime nodes:

* Open the "Test" workflow
* Double click the "Remote SDF Reader" node
   * Enter a filename for an SDF file located on the remote server. The path is
     relative to the home directory of the ssh user.  
     As example you could use the 10.sdf in the test directory.
* Execute the workflow   
  The output SDF file should contain an additional column called "counter"
* If you see "Problem creating ssh connection" messages in the console, repeat the steps used to test the ssh connection using the ExtSSHTest workflow.
* If the nodes do not execute correctly, check the stderr output by right clicking the corresponding node and selecting "View: Command Error output"


Adding your own command line programs as KNIME nodes.
-------------------------------------------------------

Command Line nodes are configured in the "`<chemalot_knime_dir>`/config/cmdLine/commandLinePrograms.xml" file. Open the file with your favorite text editor.
Each KNIME node is defined by a `<command>` element. The example below defines the "babel" command line node:

    <command name='babel' label='babel (OE)' subfolder='GNEStructManipulation'>
       <IO in="-in .sdf" out="-out .sdf"/>
       <default>-add2d</default>
       <ports in='sdf' out='sdf'/>
       <help option='--help all'/>
    </command>

Attributes to the `<command>` element:   

* `name`  
   provides a unique identifier for the node and is also used as default for the executable name. The executable name can be overwritten with a "command" attribute.  
* `label`  
   specifies the label used in the KNIME "Node Repository"
* `command`
   specifies the executable name used when executing the command line. If the executable is not in your path, you must supply an absolute path or a path relative to your home directory on the remote host.
* `subfolder`  
  specifies in which subfolder your KNIME node will appear in the KNIME "Node Repository". To create a new subfolder, the source code has to be changed. Currently the following values are defined:
<pre><code>
   GNEAdvanced, 
   GNEAdvanced/GNEAdvUtilities,
   GNEAdvanced/GNEAdvStructManipulation,
   GNEAdvanced/GNEAdvWriter,
   GNEAdvanced/GNEAdvStructManipulation/GNEAdvProps,
   GNEStructManipulation, 
   GNEStructManipulation/GNEStrProps,
   GNEStructManipulation/GNEStrDiversity,
   GNEStructManipulation/GNEStrSearch,
   GNEStructManipulation/GNEStrQSAR,
   GNEWriter
   GNEUserDefined  This is not currently used but could be used for adding your own command line programs.
</code></pre>

Sub elements of `<command>`:

* `<IO>`  
   The IO sub element has the `in` and `out` attributes that define command line options needed to configure the executable to read SDF files from stdin and stdout.
* `<default>`  
  The default element contains the default command line options that the KNIME user sees when configuring a node newly added to a workflow. This is a great place to put the most commonly used options.
* `<ports>`  
  The ports element takes the `in` and `out` attributes that can have empty values or the string "sdf". If `in='sdf'` is used, the node will have an input port. If out='sdf' is used, the node will have an output port.
* `<help>`  
  The help element has the option attribute that specifies the command line option to be specified to cause the executable to produce a help text.


Modifying the chemalot\_knime package
---------------------------------------------

To install the KNIME SDK environment in order to debug or modify the code, please follow the steps outlined in [developer.readme.md](developer.readme.md)
