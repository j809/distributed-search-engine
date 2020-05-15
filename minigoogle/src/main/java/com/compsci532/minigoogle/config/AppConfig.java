package com.compsci532.minigoogle.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@Data
@Configuration
@PropertySource("classpath:application.properties")
public class AppConfig {
    @Autowired
    private Environment env;

    @Value("${hadoop.doc_data.dir}")
    private String docDirectory;

    @Value("${hadoop.url_mapping.dir}")
    private String urlMappingFileDirectory;

    @Value("${rocksdb.forwardindex.dir:/tmp/group-7/app-fi}")
    private String rocksDbForwardIndexDirectory;

    @Value("${rocksdb.invertedindex.dir:/tmp/group-7/app-iv}")
    private String rocksDbInvertedIndexDirectory;

    @Value("${rocksdb.url_mapping.dir}")
    private String rocksDbUrlMappingDir;
}