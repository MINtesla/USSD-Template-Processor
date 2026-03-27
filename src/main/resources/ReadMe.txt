***********************************************************************
                                README
***********************************************************************
                (c) Copyright Comviva Technologies Ltd.
                           All rights reserved
=======================================================================
Product Details
=======================================================================
Product Name    : MobiLytix Real Time Marketing (MRTM)
Product Version : 7.12.2.118.8.0
Date of Release : 26th February 2026
Release Branch  : release/7.12.2

MobiLytix Real Time Marketing (MRTM) is an analytics-driven consumer management platform that automates real-time campaign processes and intelligently interacts with customers. It provides relevant offers based on customer data through defined rule sets and decisioning logic.

=======================================================================

1. Packing List / Release Contents
=======================================================================
Package Provided:
-----------------
MRTM_v7.12.2.118.8.0.tgz

Contents:
---------
/cm_release/VDRC/MRTM_v7.12.2.118.8.0/
    |_ binaries/
        |_ routerservice:v7.12.2.118.8.0_7444.tar
        
Checksum:
---------
3856271954 443658240 MRTM_v7.12.2.118.8.0.tgz
=======================================================================

2. System Requirements
=======================================================================

Hardware Requirements:
-----------------------
Component              Description                                Configuration
------------------------------------------------------------------------------------
App Server (5)         App server 1: Tomcat (ENBA, ERED, ECMP, GUI)
                       App server 2: Kafka, Zookeeper, RAM, Kafka-Manager
                       App server 3: Jenkins + Version Control + Code Management
                       App server 4: Qlik Sense
                       App server 5: ETL
                       Rack-Mountable Server, Intel Dual CPU Quad Core E5-2609,
                       2.4 GHz Processor, 8 GB RAM, 60 GB HDD,
                       RAID 1/0, RPS, Cables, RFK, DVDRW, Quad NIC

Database Server (3)    Database server 1: SingleStore
                       Database server 2: Jenkins + Version Control + Code Mgmt
                       Database server 3: PostgreSQL (GUI) & SingleStore
                       Dual CPU Quad Core E5-2609, 2.4 GHz Processor,
                       16 GB RAM, 200 GB HDD, RAID 1/0, RPS, Cables,
                       RFK, DVDRW, Quad NIC

Software Requirements:
----------------------
- OS: 64-bit Red Hat Linux EL 8.0 or later
- Application Server:
    * JDK 1.8 (64-bit)
    * Confluent Zookeeper 7.2.2 (3 servers)
    * Confluent Kafka 7.2.2 (4–5 clusters)
    * CMAK 3.0.0.5
    * TIBCO Jaspersoft 6.4.1 (ECAP)
    * Zabbix + Postgres zabbixdb
    * Qlik Sense installer
- Database:
    * SingleStore 8.5.1 or above
    * Postgres EDB 14.2
- Web Server:
    * Apache Tomcat 8.5.3 (9 clusters for 2 app servers)
    * Apache Tomcat 8.0.41 (1 cluster for 2 app servers) or above
- Kubernetes:
    * Kubernetes v1.23.5

=======================================================================

3. Dependencies
=======================================================================
Required baseline versions:

	7.12.2, 7.12.2.0.0.1, 7.12.2.0.1.1, 7.12.2.0.2.1, 7.12.2.0.3.1,
	7.12.2.0.4.1, 7.12.2.0.6.1,
	MRTM_v7.12.2.118.1.1_B3,
	Hotfix MRTM_v7.12.2.0.6.1_HF,
	MRTM_v7.12.2.118.2.1_B1,
	MRTM_v7.12.2.118.2.1_B2,
	MRTM_v7.12.2.0.6.2,
	MRTM_v7.12.2.118.2.1_HF1,
	MRTM_v7.12.2.118.2.2,
	MRTM_v7.12.2.118.3.0,
	MRTM_v7.12.2.0.6.3
	MRTM_v7.12.2.118.5.0
	MRTM_v7.12.2.118.5.1
	MRTM_v7.12.2.118.6.0
	MRTM_v7.12.2.118.7.0


=======================================================================

4. Installation Procedure
=======================================================================

⚠️ **Deployment Note:** 
	Configuration changes may vary based on environment (UAT/PROD).  

	- Update authentication details (Username and Password) in CUIG Integration as per environment
	- Consul values differ by environment  

-----------------------------------------------------------------------

STEP 1: BACKUP
---------------
	Before making any changes, backup the following:

	1.1 Deployment Files  
		mkdir -p <backup_file_path>/7.12.2.118.8.0  
		cp -rf /share/containerfiles/<org_name>/<namespace>/deploymentfiles/ <backup_file_path>/7.12.2.118.8.0/

	1.2 Configuration Files  
		cp -rf /share/containerfiles/<org_name>/<namespace>/dockerconfig/ <backup_file_path>/7.12.2.118.8.0/

	

-----------------------------------------------------------------------
STEP 2: LOAD, TAG, AND PUSH DOCKER IMAGES
------------------------------------------
	For each image under `binaries/`:

		docker load < <image_tar_file>
		docker tag <image_name>:<image_version> <k8s_registry_ip>:<port>/<image_name>:<new_tag>
		docker push <k8s_registry_ip>:<port>/<image_name>:<new_tag>

	Example:

		docker load < menuservice:v7.12.2.118.7.0_6657.tar
		docker tag menuservice:v7.12.2.118.6.0_6619 10.109.111.33:5000/menuservice:v7.12.2.118.7.0_6619
		docker push 10.109.111.33:5000/menuservice:v7.12.2.118.7.0_6619

---------------------------------------------------------------------
STEP 3: Certificate Integration with Kubernetes

---------------------------------------------------------------------

**Note:** This step is required only when the endpoint is HTTPS.  
**Prerequisite:** Certificate needed.

   ### Login to the Kubernetes master server.

------------------------------------------

	### Step 3.1: Create a Kubernetes Secret
	
	kubectl create secret generic <secret-name> --from-file=<path-to-cert-file> -n <namespace>


	### Step 3.2: Verify Secret Creation
	
	kubectl get secret <secret-name> -n <namespace>
	kubectl describe secret <secret-name> -n <namespace>

	### Step 3.3: Update Deployment File
	Edit:
	
	vi /nfsshare/containerfiles/vdrc/usecase/deploymentfiles/igw/fulfilment_plugin.yaml


	### Add under **volumeMounts**:

	- name: <volume-name>
	  mountPath: /etc/certs/<cert-dir>
	  readOnly: true


	### Add under **volumes**:

	- name: <volume-name>
	  secret:
		secretName: <secret-name>

-----------------------------------------------------------------------
STEP 4: STOP TRAFFIC AND DATABASE CHANGES
------------------------------------------------------------
NA


-----------------------------------------------------------------------
STEP 5: UPDATE CONSUL CONFIGURATION
------------------------------------

Update/add below mentioned properties in routerservice Consul.
-----------------------------------------------------------
# General application settings
owner-name: comviva

server:
  port: 8080
  servlet:
    context-path: /routerservice   # The application will be available at http://localhost:8080/routerservice
 
spring:
  application:
    name: routerservice

kafka:
  bootstrapServers: 172.28.1.99:9093  # Kafka broker address
  batchSize: 40000  # Kafka batch size (in bytes)
  lingerMs: 10  # Time in milliseconds to wait before sending a batch of messages to Kafka
  fetchMinBytes: 5  # Minimum amount of data the server will return for a fetch request
  maxPollRecords: 25  # Max records returned in a single poll from Kafka
  maxPollIntervalMs: 600000  # Max time (in milliseconds) between two poll operations before the session is expired
  autoCommitIntervalMs: 500  # Interval (in milliseconds) for automatic offset commits in Kafka
  retryBackOffMs: 5000         # Retry backoff time in milliseconds
  requestTimeOutMs: 60000      # Request timeout in milliseconds
  
global:
  routerTopicList: "VDRCJ4UROUTERSERVICE:GVDRCJ4UROUTERSERVICE:1"  # Kafka topic(s)
  
routerservice:
  kafka:
    debugEnable: false
    threadCount: 1  # Number of threads for Kafka consumer/producer operations
    tps: 2000  # Target transactions per second for Kafka
  opennet-base-url: "http://gatewayserviceplugin-service:8000/fulfilment/v1/api?group="
  huawei-base-url: "http://gatewayserviceplugin-service:8000/fulfilment/v1/api?group=HW_"
  ecmp-topic: "VDRCUATECMPTOPICSS"
  opennet-endpoint-topic-map:
    CCSChangeOOAdd: VDRCUATCCSChangeOOAdd
    CCSAdjustAccount: OPCCSAdjustAccount
    CCS_QUERY_BALANCE: VDRCUATCCSQueryBalance
    AdjustAccBalSubtract: VDRCUATAdjustAccBalSubtract
    CCSLoanManage: VDRCUSECASELOANMGR
    CCSModifySubQuota: CCSModifySubQuota
    CCSChangeSubInfo: CCSChangeSubInfo
    
  huawei-endpoint-topic-map:
    CCSChangeOOAdd: HUAWEIChangeOOAdd
    CCSAdjustAccount: HWCCSAdjustAccount
    CCS_QUERY_BALANCE: HUAWEIQueryBalance
    AdjustAccBalSubtract: HUAWEIAdjustAccBalSubtract
    CCSLoanManage: HUAWEILoanManage
    CCSModifySubQuota: HUAWEIModifySubQuota
    CCSChangeSubInfo: HUAWEIChangeSubInfo
    
  endpoint-config:
    CCSChangeOOAdd:
      source-name: OCS
      trigger-name: REWARDS
    CCSAdjustAccount:
      source-name: OCS
      trigger-name: REWARDS
    CCS_QUERY_BALANCE:
      source-name: API
      trigger-name: API_RESPONSE
    AdjustAccBalSubtract:
      source-name: OCS
      trigger-name: REWARDS
    CCSLoanManage:
      source-name: OCS
      trigger-name: REWARDS
    CCSModifySubQuota:
      source-name: OCS
      trigger-name: REWARDS
    CCSChangeSubInfo:
      source-name: API
      trigger-name: API_RESPONSE

hazelcast:
  member-address: "172.28.1.99"
  member-cluster: "vdrc-j4u-uat"
  instance-name: "routerservice-j4u"
  instance-label: "routerservice-j4u-uat"
  smart-routing: true
  redo-operation: false
  client:
    invocation-timeout-seconds: 2
    heartbeat-timeout-ms: 5000
    retry-pause-ms: 100

-----------------------------------------------------------------------

STEP 7: CUIG INTEGRATION IMPORT
-----------------------------------

	As part of the release, required CUIG Integration must be imported into MobilityX CUIG.
	-----------------------------------------------------------------------
	Login to **MobilityX CUIG** and import the following Integration:
	
	### Location of Files
	----------------------
	The CUIG Integration files are bundled in:

	  CUIG_INTEGRATION.zip  (available in the RFR package) with two package inside FULFILEMNT and EVENTPROCESSOR

	This `.zip` file contains all required .json files for CUIG Integration.
		
    ## Note:- For all integrations, ensure that third-party endpoints and authentication parameters are updated according to the target environment (UAT/PROD). Also remove import in the name from all the cuig integration after importing and before applying. Change kafka topic according to your setup in the integration and change xslt as per requirement change.
	
-----------------------------------------------------------------------

STEP 8: Configuration Update
-----------------------
1) The dockerconfig and deployment file for the new routeservice microservice are bundled in config.zip (available in the RFR package).
2) 
The correspondimg topic should be added in inbound topic of the respective api in CUIG.

-----------------------------------------------------------------------
STEP 9: SANITY TESTING
-------------------------
	1. Verify all pods are running:  
		   kubectl get pods -n <namespace>
	2. Check logs for errors:  
		   kubectl logs <pod_name> -n <namespace>
	3. Confirm services are accessible.

-----------------------------------------------------------------------
STEP 10: FULL E2E TESTING
----------------------------
	1. Validate all business flows.  
	2. Confirm campaign triggers, rewards, and interactions.  
	3. Monitor application logs and performance.  

=======================================================================

5. Housekeeping Configuration
================================
None

=======================================================================


6. Rollback Procedure
=======================================================================
	1.In case rollback is required:

	  
	2. Restore configuration files:  
		   cp -rf <backup_file_path>/7.12.2.118.8.0/dockerconfig/ \
				 /share/containerfiles/<org_name>/<namespace>/dockerconfig/
	3. Restore Consul configs using. 
	4. Restore deployment files:  
		   cp -rf <backup_file_path>/7.12.2.118.8.0/deploymentfiles/ \
				 /share/containerfiles/<org_name>/<namespace>/deploymentfiles/
	5. Restart pods:  
		   kubectl apply -f <component_name>.yaml
    7. Restore CUIG Integration by importing backups Integration. 
	8. Restart **interactive service**.  
	9. Perform sanity and E2E testing.

=======================================================================

7. Troubleshooting Tips
=======================================================================
	- Always confirm image tags are updated in deployment YAMLs.  
	- Verify Consul keys after update.  
	- If GUI imports fail, recheck exported `.xlsx` format.
    - If CUIG Integration imports fail, recheck exported Integration.
	- Refer to the detailed Installation Manual for issue-specific guidance.

=======================================================================

8. Technical Support
=======================================================================
		For assistance on this release, please contact:

		Ashutosh Singh
		Email: ashutosh.singh4@comviva.com  
		Mobile: +91-7011928653

=======================================================================


NOTE - THERE IS NO GUI CHANGE INVOLVE IN THIS RELEASE