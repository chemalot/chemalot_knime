/*
    The chemalot-knime package provides a framework to execute commandline
    programs that read and wrie SDF files on a remote host from the KNIME
    graphical pipelining platform. 
    Copyright (C) 2016 Genentech Inc.

    This file is part of chemalot-knime.

    chemalot-knime is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    chemalot-knime is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with chemalot-knime.  If not, see <http://www.gnu.org/licenses/>.

*/
package com.genentech.knime;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.knime.core.node.NodeLogger;

/**
 * This file reads settings from a configuration file tand stores them in private static final variables
 * for use throught the whole chemalot-knime package.
 * 
 * Configure eclipse eg. with -DGNEKnimeConfigDir=http://yourPublicServer.com/Aestel/public/files/knimeCfg
 * The file can either be a url or a local filename.
 * 
 * @author albertgo
 *
 */
public class Settings {
    public static final String CONFIGPath = System.getProperty("GNEKnimeConfigDir");
    public static final String CMD_CONFIG_FILE_PATH = Settings.CONFIGPath + 
                                               "/cmdLine/commandLinePrograms.xml";
    private static final NodeLogger LOGGER = NodeLogger.getLogger(Settings.class);
    private static final String TASKLogTemplate;
    
    private static String EXCHANGELocalDir = null;
    private static String EXCHANGERemoteDir = null;
    private static String MYSUBOptions = null;
    static boolean DISABLEKnimeUpdateSites;
    public static final int SSHTimeout;
    public static final String SSHInitFileTemplate;
    public static final String SSHRemoteHost;
    public static final Map<String, String> GNEProperties;
    
    public static String getExchangeLocalDir()
    {   if( EXCHANGELocalDir != null ) return EXCHANGELocalDir;
        
        readExchangeDirs();
        return EXCHANGELocalDir;
    }

    public static String getExchangeRemoteDir()
    {   if( EXCHANGERemoteDir != null ) return EXCHANGERemoteDir;
        
        readExchangeDirs();
        return EXCHANGERemoteDir;
    }


	public static String getMysubOptions() {
		if( MYSUBOptions != null ) return MYSUBOptions;
        
		MYSUBOptions = "-limit 24 -nCPU 1 -totalMem 30 -jobName knime";
        return MYSUBOptions;	}

	static {
        Element root = getConfigRoot(CMD_CONFIG_FILE_PATH);
        readExchangeDirs(root);
        
        Element config = root.getChild("config");
        Element ssh = config.getChild("ssh");
        SSHRemoteHost = getAttribute(ssh, "remoteHost", "rosalind.gene.com");
        SSHTimeout = Integer.parseInt(getAttribute(ssh, "timeout", "1000"));
        SSHInitFileTemplate = getAttribute(ssh, "initFileTemplate", "knimerc.$mode");
        
        String dummy = null;
        Element tLog = config.getChild("loggingURLTemplate");
        if( tLog != null )
            dummy = tLog.getAttributeValue("url");
        TASKLogTemplate = dummy;
        
        DISABLEKnimeUpdateSites = getAttribute(config, "disableKnimeUpdateSites", "Y")
        		                  .toLowerCase().startsWith("y") ? true : false; 
        
        GNEProperties = readPropertyMap(CONFIGPath, System.getProperty("GNEKnimePropertiesFile"));

        // log knime startup
        String uid = System.getProperty("user.name");
        logUsage(uid, "knime", "", "startup", "", 0);
    }
    
    
    
    private static void readExchangeDirs() {
        Element root = getConfigRoot(CMD_CONFIG_FILE_PATH);
        readExchangeDirs(root);
    }

    private static String getAttribute(Element ele, String name, String deflt) {
        if( ele == null || name == null || name.length() == 0 ) return deflt;
        return ele.getAttributeValue(name, deflt);
    }

    /**
     * @param root
     * @throws Error
     */
    @SuppressWarnings("unchecked")
    private static void readExchangeDirs(Element root) throws Error {
        for(Element e : (List<Element>)(root.getChild("config").getChildren("exchangeDir")) ) {
            String localD = e.getAttributeValue("local");
            String remoteD = e.getAttributeValue("remote");
            
            if(localD != null )   localD = localD.trim();
            if( remoteD != null ) remoteD = remoteD.trim();
            
            if( localD == null || remoteD == null || 
                localD.length() == 0 || remoteD.length() == 0)
                throw new Error(String.format("Invalid %s config local=%s remote=%s\n",
                        e.getName(), localD, remoteD));
            
            File lDir = new File(localD);
            if( ! lDir.isDirectory() ) {
                LOGGER.info(localD + " is not an accessible directory.");
                continue;
            }
            
            EXCHANGELocalDir = localD;
            EXCHANGERemoteDir = remoteD;
            
            return; // we found the first valid pair
        }
    }

    /**
     * @return the root element of the CommandLine config xml file.
     */
    public static Element getConfigRoot(final String cmdConfigFilePath)
            throws Error {
        SAXBuilder builder = new SAXBuilder(false); // non validating

        try {
        Document xmlDoc;
        if( cmdConfigFilePath.toLowerCase().startsWith("http://"))
        {   URL url = new URL(cmdConfigFilePath);
            File xmlFile = cacheToFile(url, Pattern.compile("application/xml",Pattern.CASE_INSENSITIVE));
       
            xmlDoc = builder.build(xmlFile);
        }else
            xmlDoc = builder.build(new File(cmdConfigFilePath));
        
        Element root = xmlDoc.getRootElement();
        return root;
        
        } catch(Exception e) {
            LOGGER.error(String.format("%s in XML filename: ", e, cmdConfigFilePath));
            throw new Error(e);
        }
    }

    /**
     * try to access the url and if successful cache the result into local folder.
     * @param url
     * @param requiredTypeRe if mime type does not match use cached version
     * @return file on local folder.
     * 
     * @throws IOException if file not accessible and not in cache.
     */
    public static File cacheToFile(URL url, Pattern requiredTypeRe) throws IOException {
        String fName = url.toString();
        String fPath = fName.substring(CONFIGPath.length()+1);
        if( fPath.indexOf('/') >= 0 )
            fName = fPath.substring(fPath.lastIndexOf('/')+1);
        else fName = "";
        
        if( fName.length() == 0 ) { 
            fName = fPath;
            fPath = "";
        } else {
            fPath = fPath.substring(0,fPath.lastIndexOf('/'));
        }
        
        
        String uDir = System.getenv("USERPROFILE");
        if( uDir == null || ! new File(uDir).exists() ) {
            uDir = System.getProperty("user.home");
            
            if( ! new File(uDir).exists() )
                throw new Error("can not find user home directory!");
        }
        
        File cacheDir = new File(uDir + "/.knimeGenentech/cache/"+fPath);
        cacheDir.mkdirs();
        
        InputStream in = null;
        OutputStream out = null;
        try {
            URLConnection con = url.openConnection();
            String cType = con.getContentType();
            if( cType == null ) cType = "";
            if( ! requiredTypeRe.matcher(cType).matches() )
                throw new IOException("Wrong File Type:" + cType);
            in = con.getInputStream();
            
            out = new FileOutputStream(new File(cacheDir, fName));
            byte[] buf = new byte[4096];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch( IOException e ) {
            // network may be down lets try and see if we have a cached version
            LOGGER.error("Could not access url:" + url);
            LOGGER.error("Required type is:" + requiredTypeRe);
            LOGGER.error(e);
        } finally {
            try {
                if( out != null ) out.close();
                if (in != null ) in.close();
            } catch( Throwable e ) {
               e.printStackTrace(System.err);  // ignore on close
            }
        }
        File f = new File(cacheDir, fName);
        if( ! f.exists() )
            throw new IOException(String.format("Could not access: %s", fName));

        return f;
    }
    
    /**
     * Log usage of application into stats tables.
     */
    static public void logUsage(String userId, String application, String appParam,
                           String task, String taskParam, long timeMs )
    {   if( TASKLogTemplate == null ) return;  // no logging
    
        String urlStr = TASKLogTemplate
                            .replace("#uName#", userId)
                            .replace("#application#", application)
                            .replace("#appParam#", appParam)
                            .replace("#task#", task)
                            .replace("#taskParam#", taskParam)
                            .replace("#timeMs#", Long.toString(timeMs));
        try {
            URL url = new URL(urlStr);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.connect();
            int resCode = con.getResponseCode();
            if( resCode != 200 )
                LOGGER.warn("Got errorcode while logging usage: " + resCode);
            con.disconnect();
        } catch (IOException e) {
            // This is just task logging lets ignore errors
            e.printStackTrace();
        }
    }
    
    /**
     * Read properties from property file.
     * 
     * if Filename starts with "/", "\" or "?:" basename is ignored.
     * else the properties are read from baseName/fileName.
     * 
     */
    static private Map<String,String> readPropertyMap(String baseName, String fileName) 
    {   Comparator<String> descender = new Comparator<String>()
        {   @Override
            public int compare(String o1, String o2)
            {   return o2.compareToIgnoreCase(o1);
            }
        };
        Map<String, String> pMap = new TreeMap<String, String>(descender);
    
        if( fileName == null || fileName.length() == 0 )
            return Collections.emptyMap();
        
        if( ! fileName.startsWith("/") && ! fileName.startsWith("\\")
            && (fileName.length() <= 2 || fileName.charAt(1) != ':') )  // relative path
        {   fileName = baseName + "/" + fileName;
        }
        
        try
        {   LOGGER.info("Reading GNESettings from: " + fileName);
            File propFile;
            if( fileName.toLowerCase().startsWith("http://"))
            {   URL url = new URL(fileName);
                propFile = cacheToFile(url, Pattern.compile("text/plain|application/octet-stream",Pattern.CASE_INSENSITIVE));
            }else
            {   propFile = new File(fileName);
            }
            
            
            Properties p = new Properties();
            p.load(new BufferedInputStream(new FileInputStream(propFile)));
            
            String uid = System.getProperty("user.name");
            if( uid.equalsIgnoreCase(p.getProperty("executer.user")))
                p.setProperty("ssh.user",p.getProperty("server.ssh.user"));
            else
                p.setProperty("ssh.user",uid);
            p.remove("executer.user");
            p.remove("server.ssh.user");
            
            String os = System.getProperty("os.name").toLowerCase();
            if( os.contains("win") )
                os = "win";
            else if( os.contains("mac"))
                os = "mac";
            else if( os.contains("linux"))
                os = "linux";
            os = os + '.';
            
            for( Entry<?, ?> e : p.entrySet() )
            {   String key = (String) e.getKey();
                String val = (String) e.getValue();
                pMap.put(key, val);
                if( key.startsWith(os) )
                {   key = key.substring(os.length());
                    pMap.put(key, val);
                }
            }
            
        }catch (IOException e) 
        {   throw new Error(e);
        }
        
        return Collections.unmodifiableMap(pMap);
    }
}
