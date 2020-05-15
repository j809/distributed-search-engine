package com.compsci532.minigoogle.api;

import com.compsci532.minigoogle.reader.HdfsReader;
import org.apache.spark.api.java.JavaSparkContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RequestMapping("/minigoogle/api/v1")
@RestController
public class Controller {
    @Autowired
    private JavaSparkContext javaSparkContext;

    @Autowired
    private HdfsReader hdfsReader;

    @GetMapping("/save/index")
    public Boolean saveForwardIndex() {
        return hdfsReader.createForwardIndex(javaSparkContext);
    }

    @GetMapping("/get/forwardIndex")
    public Map<String, List<String>> getForwardIndex() {
        return hdfsReader.getForwardIndex();
    }

    @GetMapping("/get/invertedIndex")
    public Map<String, List<String>> getInvertedIndex() {
        return hdfsReader.getInvertedIndex();
    }

    @GetMapping("/search")
    public Set<String> query(@RequestParam(name = "query") String query) {
        return hdfsReader.query(query.toLowerCase());
    }
}
