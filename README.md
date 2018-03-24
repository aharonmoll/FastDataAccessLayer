# GigaSpaces-FastDataAccessLayer
The code
-----------------------------------------

[_Introduction_](#introduction)

[1. Building project](#building-project)

[2. Start XAP grid](#start-xap-grid)

[3. Deploying PUs](#deploying-pus)

## Introduction

This project is an implementation of REST calls from the Mobile app to XAP data grid to solve the performance issue with accessing data in Kinvey BAAS on China side due to geographical separation. Each GET request for application data is addressed to XAP web pus, that load data from the US and China spaces. All other requests are redirected by XAP to Kinvey and update the space data in the grid.

## Building project

This project is based on Maven, so to build it, you would run next command:

```bash
export WORKING_DIRECTORY="path to the project dir"
cd ${WORKING_DIRECTORY}
mvn clean package
```

## Start XAP grid

Start 2 XAP clusters in the USA and CHINA zones on host1 and host2.
Login to host1:

```bash
export XAP_HOME="path to gigaspaces-xap-premium directory"
export XAP_LOOKUP_LOCATORS="host1:4174"
export EXT_JAVA_OPTIONS="-Dcom.gs.zones=USA"

cd $XAP_HOME/bin
./gs-agent.sh
```
 
Login to host2:

```bash
export XAP_HOME="path to gigaspaces-xap-premium directory"
export XAP_LOOKUP_LOCATORS="host2:4174,host1:4174"
export EXT_JAVA_OPTIONS="-Dcom.gs.zones=CHINA"

cd $XAP_HOME/bin
./gs-agent.sh
```

## Deploying PUs

To deploy the spaces, REST services and WAN gateway components to your XAP clusters via XAP CLI, next command can be used on the host1:

```bash
./gs.sh deploy -zones USA ${WORKING_DIRECTORY}/space-us/target/space-us.jar
./gs.sh deploy -zones USA -properties "embed://usa.host=host1;usa.lus.port=4174;china.host=host2;china.lus.port=4174" ${WORKING_DIRECTORY}/wan-gateway-us/target/wan-gateway-us.jar
./gs.sh deploy -zones USA ${WORKING_DIRECTORY}/web-us/target/web-us.war
```

On the host2 run the following commands:

```bash
./gs.sh deploy -zones CHINA ${WORKING_DIRECTORY}/space-china/target/space-china.jar
./gs.sh deploy -zones CHINA -properties "embed://china.host=host2;china.lus.port=4174" ${WORKING_DIRECTORY}/wan-gateway-china/target/wan-gateway-china.jar
./gs.sh deploy -zones CHINA ${WORKING_DIRECTORY}/web-china/target/web-china.war
```