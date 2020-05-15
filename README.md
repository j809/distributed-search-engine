# Distributed Search Engine
## Dependencies
* java v1.8.0_222
* spark v2.4.4
* hadoop v3.2.1
* rocksdb - Compiled from the [source](https://github.com/facebook/rocksdb)

 ## Configuration Variables
* `spark.home` = Bin directory inside spark home where spark binaries are located, eg. `/home/user/spark/bin`
* `hadoop.doc_data.dir` =  HDFS URI for directory with documents, eg. `hdfs://localhost:9000/user/<username>/doc_contents`
* `hadoop.url_mapping.dir` = HDFS URI for directory with file containing URL document mapping [LINK](https://github.com/ds-umass/mini-google-group-7/blob/master/id_URL_pairs.txt), eg. `hdfs://localhost:9000/user/<username>/url_data`
* `rocksdb.forwardindex.dir` = RocksDB directory for storing forward index, eg. `/tmp/forward-index`. Please note, `/tmp` must exist.
* `rocksdb.invertedindex.dir` = RocksDB directory for storing inverted index, eg.  `/tmp/inverted-index`. Please note, `/tmp` must exist.
* `rocksdb.url_mapping.dir` = RocksDB directory for storing URL document mappings, eg. `/tmp/url-doc-map`. Please note, `/tmp` must exist.

## Project Setup
Steps to setup the project locally:
1. Clone the repository from [source](https://github.com/ds-umass/mini-google-group-7.git)
2. Create a hdfs path for storing the data in HDFS using the following commands:
    * ``` hdfs dfs -mkdir /user ```
    * ``` hdfs dfs -mkdir /user/<username> ```
        
    *Note:* Please make sure hadoop is running before executing the above commands.   
3. Create a hdfs sub-directory and load the all the documents in that directory by running the following commands:
    * ``` hdfs dfs -mkdir /user/<username>/doc_contents ```
    * ``` hdfs dfs -put </path/to/local/*.*> /user/<username>/doc_contents ```
4. Similarly create a hdfs sub-directory and load the `id_url_pairs.txt` file in that directory by running the following commands:
    * ``` hdfs dfs -mkdir /user/<username>/url_data ```
    * ``` hdfs dfs -put </path/to/local/id_url_pairs.txt> /user/<username>/url_data ```
5. Run the following command from `minigoogle` directory with appropriate path values to generate the executable. Example command will look like below.
    ```shell
    ./mvnw clean install -Dspark.home=/home/kautilya/spark-2.4.4-bin-hadoop2.7/bin -Dhadoop.doc_data.dir=hdfs://localhost:9000/user/kautilya/data -Dhadoop.url_mapping.dir=hdfs://localhost:9000/user/kautilya/url_data -Drocksdb.forwardindex.dir=/tmp/app-kv -Drocksdb.invertedindex.dir=/tmp/app-iv-kv -Drocksdb.url_mapping.dir=/tmp/url-kv
    ```
6. Run the following command from `minigoogle` directory with appropriate path values to run the web server. Example command should look something like below.
    ```shell
   ./mvnw spring-boot:run -Dspring-boot.run.arguments="--spark.home=/home/kautilya/spark-2.4.4-bin-hadoop2.7/bin,--hadoop.doc_data.dir=hdfs://localhost:9000/user/kautilya/data,--hadoop.url_mapping.dir=hdfs://localhost:9000/user/kautilya/url_data,--rocksdb.forwardindex.dir=/tmp/app-kv,--rocksdb.invertedindex.dir=/tmp/app-iv-kv,--rocksdb.url_mapping.dir=/tmp/url-kv"
    ```
   
   *Note:* By default the application runs on port 8080. 

 ## APIs
 1. `/minigoogle/api/v1/save/index` - Computes forward and inverted index and persists both in rocksDB.
 2. `/minigoogle/api/v1/search?query=<your query>` - Returns list of URLs containing any of the words in the query. 
 3. `/minigoogle/api/v1/get/forwardIndex` - Returns the entire forward index persisted in rocksDB. *NOTE:* Results can be huge.
 4. `/minigoogle/api/v1/get/invertedIndex` - Returns the entire inverted index persisted in rocksDB. *NOTE:* Results can be huge.
 
 ## Generate Index
 * Hit the API `http://<HOST:PORT>/minigoogle/api/v1/save/index` to generate the indices.
 
 ## Query
  * After generating the indices search using the API `http://<HOST:PORT>/minigoogle/api/v1/search?query=<your query>` to retrieve the results.
