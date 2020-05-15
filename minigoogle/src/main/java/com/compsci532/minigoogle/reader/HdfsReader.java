package com.compsci532.minigoogle.reader;

import com.compsci532.minigoogle.config.AppConfig;
import com.compsci532.minigoogle.repository.RocksDBRepository;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import scala.Tuple2;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class HdfsReader implements Serializable {

    @Autowired
    public RocksDBRepository rocksDBRepository;

    @Autowired
    public AppConfig appConfig;

    public Boolean createForwardIndex(JavaSparkContext javaSparkContext) {
        readUrlData(javaSparkContext);

        Map<String, List<String>> forwardIndex = new HashMap<>();
        JavaPairRDD<String, String> fileContentMapping = javaSparkContext.wholeTextFiles(appConfig.getDocDirectory(), 1);
        JavaRDD<Tuple2<String, List<String>>> docWordsRDD = fileContentMapping
                .map(fileNameContentTuple ->
                        new Tuple2<>(fileNameContentTuple._1, Arrays.asList(fileNameContentTuple._2
                                        .replaceAll("[^0-9a-zA-Z ]", "")
                                        .replaceAll(" +", " ")
                                        .toLowerCase()
                                        .split(" "))));
        List<Tuple2<String, List<String>>> docWordList = docWordsRDD.collect();

        docWordList.parallelStream().forEach(item -> {
            forwardIndex.put(item._1, item._2);
        });

        createInvertedIndexInMemory(docWordsRDD);
        return rocksDBRepository.save(forwardIndex, appConfig.getRocksDbForwardIndexDirectory(), Boolean.TRUE);
    }

    public Boolean createInvertedIndexInMemory(JavaRDD<Tuple2<String, List<String>>> docWordsRDD) {
        List<Tuple2<String, Iterable<Tuple2<String, String>>>> collected = docWordsRDD
                .flatMap(tuple ->
                        tuple._2.stream().map(word -> new Tuple2<>(word, tuple._1))
                                .collect(Collectors.toList()).iterator())
                .distinct()
                .groupBy(c -> c._1)
                .collect();

        Map<String, List<String>> invertedIndex = new HashMap<>();
        collected.stream().forEach(tuple -> {
            List<String> docs = new ArrayList<>();
            Iterator<Tuple2<String, String>> iterator = tuple._2.iterator();
            while (iterator.hasNext()) {
                docs.add(iterator.next()._2.replace(appConfig.getDocDirectory() + "/", ""));
            }
            invertedIndex.put(tuple._1, docs);
        });

        return rocksDBRepository.save(invertedIndex, appConfig.getRocksDbInvertedIndexDirectory(), Boolean.FALSE);
    }

    public Map<String, List<String>> getForwardIndex() {
        return rocksDBRepository.read(appConfig.getRocksDbForwardIndexDirectory(), Boolean.TRUE);
    }

    public Map<String, List<String>> getInvertedIndex() {
        return rocksDBRepository.read(appConfig.getRocksDbInvertedIndexDirectory(), Boolean.FALSE);
    }

    public Set<String> query(String query) {
        Set<String> docs = new HashSet<>();
        if(query == null)
            return docs;
        List<String> words = Arrays.asList(query.split(" "));
        words.parallelStream().filter(word -> word != null && !word.isEmpty()).map(word -> rocksDBRepository.queryWordFromInvertedIndex(appConfig.getRocksDbInvertedIndexDirectory(), word)).filter(Objects::nonNull).forEach(docs::addAll);
        Map<String,String> docIdUrlkv = rocksDBRepository.readUrl(appConfig.getRocksDbUrlMappingDir());
        Set<String> Urls = new HashSet<>();
        for(String doc:docs){
            String[] nameExtensionSplit = doc.split("\\.");
            if (docIdUrlkv.containsKey(nameExtensionSplit[0])){
                Urls.add(docIdUrlkv.get(nameExtensionSplit[0]));
            }
        }
        return Urls;
    }

    public Boolean readUrlData(JavaSparkContext javaSparkContext) {
        JavaRDD<String> javaRDD = javaSparkContext.textFile(appConfig.getUrlMappingFileDirectory(), 1);
        JavaPairRDD<String,String> keyValuePair = javaRDD.mapToPair(row -> {
                String[] kv = row.split(",");
                return new Tuple2(kv[0], kv[1]);
        });
        Map<String,String> dodIdUrlKVMap = keyValuePair.collectAsMap();
        return rocksDBRepository.saveUrl(dodIdUrlKVMap, appConfig.getRocksDbUrlMappingDir());
    }
}