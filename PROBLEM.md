# Problem Statement

In this project, you will implement a mockup of the core functionality of a Web search engine. You will be provided a set of text files (converted from webpages), alongside with a list of corresponding URL links. You are required to use the Hadoop File System (HDFS) to store the data on a single node cluster. After you have stored the data using Hadoop, you should use Spark to build an inverted index.

Once you have the inverted index ready, you are required to load the inverted index on a key-value store using RocksDB. Your search engine must be able to handle HTTP query requests. We will provide a set of queries to verify the correctness of your implementation. 
