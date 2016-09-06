#!/bin/csh -f
set d=`date +%Y-%m-%d`
svn mv --force chemalot_knime_Install*.zip old
# in case we run this multiple times on the same day
mv  chemalot_knime_Install*.zip old

bin/makeDoc.csh
mkdir chemalot_knime
cp -r updateSite/ config/ bin/ readme.* test/ chemalot_knime
zip -r chemalot_knime_Install.$d.zip chemalot_knime
rm -rf chemalot_knime
svn add chemalot_knime_Install.$d.zip

svn status


#git archive --format zip --output full.zip master
svn export -q http://resscm.gene.com/smdd/ciKNIME/branches/oss package
( cd package/ ; tar -czf ../chemalot_knimeSRC.$d.tgz * .??* --exclude=old\* --exclude=repCP\* )
rm -rf package/

