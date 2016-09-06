#!/bin/csh -f
set script=$0
if( $script == $script:h ) set script=$PWD/$script
set installDir=$script:h

$installDir/Markdown.pl readme.md >readme.html
$installDir/Markdown.pl developer.readme.md > developer.readme.html
