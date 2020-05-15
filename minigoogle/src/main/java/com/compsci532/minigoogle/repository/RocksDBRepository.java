package com.compsci532.minigoogle.repository;

import com.compsci532.minigoogle.dictionary.Lexicon;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class RocksDBRepository implements Serializable {
    static {
        RocksDB.loadLibrary();
    }

    @Autowired
    private Lexicon lexicon;

    public Boolean save(Map<String, List<String>> kv, String dataDir, Boolean addToLexicon) {
        try (final Options options = new Options().setCreateIfMissing(true)) {
            try (final RocksDB db = RocksDB.open(options, dataDir)) {
                for (Map.Entry<String, List<String>> entry : kv.entrySet()) {
                    if (!entry.getKey().equals("")) {
                        StringBuilder serializedWordList = new StringBuilder();
                        for (String val : entry.getValue()) {
                            String value;
                            if (addToLexicon)
                                value = lexicon.add(val).toString();
                            else
                                value = val;
                            if (serializedWordList.length() != 0)
                                serializedWordList.append("|").append(value);
                            else
                                serializedWordList.append(value);
                        }
                        db.put(entry.getKey().getBytes(), serializedWordList.toString().getBytes());
                    }
                }
                return Boolean.TRUE;
            }
        } catch (RocksDBException e) {
            System.out.println("Cannot save values");
        }
        return Boolean.FALSE;
    }

    public Map<String, List<String>> read(String dataDir, Boolean checkLexicon) {
        Map<String, List<String>> kv = new HashMap<>();
        try (final Options options = new Options().setCreateIfMissing(true)) {
            try (final RocksDB db = RocksDB.open(options, dataDir)) {
                RocksIterator iter = db.newIterator();
                iter.seekToFirst();
                while (iter.isValid()) {
                    String key = new String(iter.key(), StandardCharsets.UTF_8);
                    String val = new String(iter.value(), StandardCharsets.UTF_8);
                    List<String> values = Arrays.asList(val.split("\\|"));
                    if (checkLexicon) {
                        List<String> words = values.stream().map(wordId -> lexicon.get(Integer.valueOf(wordId))).collect(Collectors.toList());
                        kv.put(key, words);
                    } else {
                        kv.put(key, values);
                    }
                    iter.next();
                }
                return kv;
            }
        } catch (RocksDBException e) {
            System.out.println("Cannot read values");
        }
        return kv;
    }

    public Set<String> queryWordFromInvertedIndex(String dataDir, String queryWord) {
        try (final Options options = new Options().setCreateIfMissing(true)) {
            try (final RocksDB db = RocksDB.open(options, dataDir)) {
                String value = new String(db.get(queryWord.getBytes()));
                List<String> docsList = Arrays.asList(value.split("\\|"));
                return new HashSet<>(docsList);
            }
        } catch (RocksDBException e) {
            System.out.println("Cannot read values");
        }
        return new HashSet<>();
    }

    public Boolean saveUrl(Map<String, String> kv, String dataDir) {
        try (final Options options = new Options().setCreateIfMissing(true)) {
            try (final RocksDB db = RocksDB.open(options, dataDir)) {
                for (Map.Entry<String, String> entry : kv.entrySet()) {
                    if(!entry.getKey().equals(""))
                        db.put(entry.getKey().getBytes(), entry.getValue().getBytes());
                }
                return Boolean.TRUE;
            }
        } catch (RocksDBException e) {
            System.out.println("Cannot save values");
        }
        return Boolean.FALSE;
    }

    public Map<String, String> readUrl(String dataDir) {
        Map<String, String> kv = new HashMap<>();
        try (final Options options = new Options().setCreateIfMissing(true)) {
            try (final RocksDB db = RocksDB.open(options, dataDir)) {
                RocksIterator iter = db.newIterator();
                iter.seekToFirst();
                while (iter.isValid()) {
                    String key = new String(iter.key(), StandardCharsets.UTF_8);
                    String val = new String(iter.value(), StandardCharsets.UTF_8);
                    kv.put(key,val);
                    iter.next();
                }
                return kv;
            }
        } catch (RocksDBException e) {
            System.out.println("Cannot read values");
        }
        return kv;
    }
}