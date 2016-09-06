#!/usr/bin/python
import os
import re
from xml.dom.minidom import parse, parseString

helpDir = '/ApplicationData/commandlineHelpText/'
knimeHDir = 'knimeConfig/cmdLine/helpText/'
dom = parse('config/cmdLine/commandLinePrograms.xml')
commands = dom.getElementsByTagName("commands")[0].getElementsByTagName("command")

for com in commands:
   hCom = com.getAttribute("name")
   baseName = re.sub("\.[^.]+$","",hCom)
   outFileName = helpDir + knimeHDir + baseName + ".txt"
   if os.path.exists(  helpDir + baseName + ".txt" ) :
      #create by concatenating help and .ex. file
      com = helpDir + baseName + ".txt"
      if os.path.exists(  helpDir + baseName + ".ex.txt" ) :
         com = 'echo "\\nExamples:\\n" | cat  ' + com + " - " + helpDir + baseName + ".ex.txt"
      else :
         com = 'cat  ' + com;
   else:  
      # create by calling command
      com = hCom + " " + com.getElementsByTagName("help")[0].getAttribute("option")
      com += " 2>&1|egrep -v 'jar:|Settings:|Missing required options?:|Unrecognized option:|Reading settings from |-h unknown parameter| [AP]M com\\.aestel|Unknown option: '"

   os.system(com + ">" + outFileName)
