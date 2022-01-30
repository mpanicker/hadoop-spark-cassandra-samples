Instructions to run the samples

Setting up your environment
---------------------------
1. Install VMWare player( I used v 7.12) on Windows 10 server with 16G RAM and i7 processor
2. Enable Virtualization through BIOS settings
3. Download Ubuntu desktop v 14.04 or later from http://www.ubuntu.com/download and choose the ubuntu ISO file from VMWare player
4. Check if your machine is 32 bit or 64 bit(Open System by clicking the Start button , right-clicking Computer, and then clicking Properties. Under System, you can view the system type.
5. Ensure 4 or 8 G to the Ubuntu VM on VMWare player by going to Player-->Manage-->Virtual Machine Settings
6. Download and install Java
	a.Go To http://www.oracle.com/technetwork/java/javase/downloads/index.html and download the latest version of Oracle JDK 8
	b.sudo mkdir -p /usr/lib/jvm
	c. sudo tar zxvf jdk-8u71-linux-x64.tar.gz -C /usr/lib/jvm
	d. sudo update-alternatives --install "/usr/bin/java" "java" "/usr/lib/jvm/jdk1.8.0_version/bin/java" 1
	e. sudo update-alternatives --set java /usr/lib/jvm/jdk1.8.0_version/bin/java
	f. run java -version to ensure the system is using the correct jdk
7. 
    Install Datastax Cassandra Community Edition
A good site to visit to get more details is http://docs.datastax.com/en/cassandra/2.2/cassandra/install/installTarball.html

        a. curl -L  http://downloads.datastax.com/community/dsc-cassandra-2.2.4-bin.tar.gz | tar xz

        b. start and stop cassandra
        http://docs.datastax.com/en/cassandra/2.0/cassandra/reference/referenceStartCprocess_t.html

        c. To start Cassandra
            $ cd install_location 
         $ bin/cassandra

        d. bin/nodetool status to verify installation is a success

        e. bin/cqlsh <ipaddress>
            i use system
            ii desc tables
            iii select * from schema_columnfamilies;

        f. to stop cassandra
            i. ps -ef | grep cassandra
            ii kill <pid>

    7.a

        Configuring Cassandra to connect from external machines(i.e. driver programs)
        a. make a copy of conf/cassandra.yaml incase we need to revert
        b. change seeds, listen_address and rpc_address to IP of ubuntu machine
        c. restart cassandra
        d. bin/cqlsh <ipaddress of box>

    8
        
        a. Install and open Eclipse( I have version 4.5.2(Mars) )

Setting up Cassandra db and table
-----------------------------------
1. bin/cqlsh <ipaddress>
2. CREATE KEYSPACE bigdata
  WITH REPLICATION = { 
   'class' : 'SimpleStrategy', 
   'replication_factor' : 1 
  };	
3. use bigdata;
4. CREATE TABLE bigdata.wordcountshakespeare ( 
   word text PRIMARY KEY, 
   count text );	


Downloading and running the samples
-----------------------------------
1. git clone https://github.com/mpanicker/hadoop-spark-cassandra-samples.git
2. from eclipse, File --> Import --> Maven --> Existing Maven Projects 
3. for Hadoop samples
        
        a. comment following spark related dependencies(they do not mix well)
		spark-cassandra-connector_2.10
		spark-cassandra-connector-java_2.10
		spark-core_2.10
	    
        b. right click on project, Maven -->Update Project to refresh the dependencies
	
        c. make sure you give the ip address of your cassandra server at ConfigHelper.setInputInitialAddress. 
	
        d. Run  HadoopCassandraWrite.java first. This will put the words and their counts into Cassandra. Make sure you put the /home/mpanicker/projects/samples/all-shakespeare.txt in the Run --> Run Configurations -->Arguments
	
        e. Run HadoopCassandraRead.java. this will read from Cassandra and find the word with largest count. The output is palced in output/part-r-00000.text file. Make sure you put the /home/mpanicker/projects/samples/output in the Run --> Run Configurations -->Arguments

4. for Spark samples
	
        a. comment all the Hadoop related dependencies(they do not mix well)
		hadoop-core
		hadoop-common
		cassandra-driver-core
		cassandra-all

	    b. Run SparkCassandraWrite first. Make sure you put the /home/mpanicker/projects/samples/all-shakespeare.txt in the Run --> Run Configurations -->Arguments. Make sure you put the ip address of Cassandra at spark.cassandra.connection.host

	    c. Run SparkCassandraRead.java. This will print out the word with most occurence
5. I got the following times

        Time taken for Hadoop MR write to Cassandra:71 seconds
        Time taken for Hadoop MR read from Cassadra:39 seconds
        Time taken for Spark MR Write to Cassandra:41 sec
        Time taken for Spark MR Read from Cassandra:19 sec

    As you can see Spark is significantly faster than Hadoop. Please share your times as well
