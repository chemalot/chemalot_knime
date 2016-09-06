# composite update site for all Knime modules supported at Genentech
http://mypublicServer.com/Aestel/public/files/knimeUpd/


to copy updateSite form knime server do the following:
set v=3.1.2
#wget http://www.knime.org/update/$v:r/$v/org.knime.update_$v.zip
mv UpdateSite.zip UpdateSite.zip.<date>
wget http://update.knime.org/analytics-platform/UpdateSite.zip >& wget.us.log &
   mkdir $v
   mkdir $v/knime.org
   mkdir $v/knime.org/$v:r
   mkdir $v/knime.org/$v:r/$v
   unzip -d $v/knime.org/$v:r/$v UpdateSite.zip
   rm knime.org
   ln -sf $v/knime.org knime.org


# get url the latest zipped trusted comunity extentions on: http://tech.knime.org/community
wget http://update.knime.org/community-contributions/trusted/3.1/TrustedCommunityContributions_3.1_201604130900.zip >& wget.tus.log &
   mkdir $v/trustedCommunity
   rm trustedCommunity
   ln -sf $v/trustedCommunity trustedCommunity
   unzip -d trustedCommunity [TrustedCommunityContributions_2.11_201509231641.zip]

# get url the latest zipped comunity extentions on: http://tech.knime.org/community
wget http://update.knime.org/community-contributions/3.1/CommunityContributions_3.1_201603210922.zip >&wget.cus.log &
   mkdir $v/community
   rm community
   ln -sf $v/community community
   unzip -d community [CommunityContributions_2.12_201412191209.zip]


# server extentions are always 0.9 versions above knime desktop versions
# check for updatesite url at:  https://www.knime.org/knimecom-product-downloads
# current: https://update.knime.org/server/4.2/4.2.4/com.knime.update.server_4.2.4.zip
set v=4.2.4
wget --user=knimeUser --password=mypw https://update.knime.org/server/$v:r/$v/com.knime.update.server_$v.zip >&serverPluginUpdSite.log &
   mkdir $v
   mkdir $v/knime.com
   mkdir $v/knime.com/$v:r
   mkdir $v/knime.com/$v:r/$v
   unzip -d $v/knime.com/$v:r/$v com.knime.update.server_$v.zip
   rm knime.com
   ln -sf $v/knime.com knime.com

   wget <https://update.knime.org/store/UpdateSite_Store_latest31.zip> >& wgetstore.log
   mkdir $v/store
   unzip -d $v/store UpdateSite_Store_latest<212>.zip
   rm store
   ln -sf $v/store store


   ls -1 knime.org/*/*/*.xml \
         knime.com/*/*/*.xml \
         store/*.jar \
         trustedCommunity/*.jar \
         community/*.jar \
   | perl -pe 's#/[\d.]+/[\d.]+##' \
   | diffy checkDownLoads.ref.txt -

# edit compositeArtifacts.xml compositeContent.xml
#     change version numbers in path

