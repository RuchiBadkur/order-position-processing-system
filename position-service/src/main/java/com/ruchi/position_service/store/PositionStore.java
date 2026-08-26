package com.ruchi.position_service.store;

import com.ruchi.position_service.model.OrderEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PositionStore {
    private final Map<String, Long> positions = new HashMap<>();
    private final Set<String> processedEventIds = new HashSet<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();



    public void apply(OrderEvent event) {
        //acquires the lock
        lock.writeLock().lock();

        try {
            //check duplicates
            if (processedEventIds.contains(event.eventId())) {
                return;
            }

            long cuncurrentPosition = positions.getOrDefault(event.symbol(), 0L);

            if ("BUY".equals(event.transactionType())) {
                cuncurrentPosition += event.quantity();
            } else if ("SELL".equals(event.transactionType())) {
                cuncurrentPosition -= event.quantity();
            }

            positions.put(event.symbol(), cuncurrentPosition);
            processedEventIds.add(event.eventId());

        } finally {
            lock.writeLock().unlock();
        }
    }

    public Map<String, Long> getPositions(){
        lock.readLock().lock();
        try{
            return new HashMap<>(positions);
        } finally {
            lock.readLock().unlock();
        }
    }
}
