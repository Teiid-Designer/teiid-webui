#!/bin/bash

JBOSS_TARGET_DIR="teiid-webui-distros/target"

# Clean up war
rm -rf `find $JBOSS_TARGET_DIR -name *.war`

# Build
mvn clean install -s settings.xml

