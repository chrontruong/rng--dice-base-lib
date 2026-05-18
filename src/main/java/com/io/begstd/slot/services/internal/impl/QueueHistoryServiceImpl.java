package com.io.begstd.slot.services.internal.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

@Service
@Primary
public class QueueHistoryServiceImpl<K,V> {
    
    public static int MAX_SIZE_QUEUE = 1000;
    
    @Value("${service.maxSizeQueue}")
    private String maxSizeQueue;
    
    private int maxQueue;
    
    private Queue<K> queue;
    private Map<K, V> map;
    
    public QueueHistoryServiceImpl() {
        queue = new LinkedList<K>();
        map = new HashMap<>();
    }
    
    public boolean isExist(K ele) {
        return map.containsKey(ele);
    }
    public V getValue(K key) {
        return map.get(key);
    }

    public synchronized void insert(K ele, V value) {
        
        maxQueue = (maxSizeQueue != null)?Integer.parseInt(maxSizeQueue):MAX_SIZE_QUEUE;
//        log.info("maxSizeQueue insert:" + maxQueue);
        while (queue.size() >= maxQueue) {
            map.remove(queue.poll());
        }
        queue.add(ele);
        map.put(ele, value);
    }
    
}
