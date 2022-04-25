package com.csd.replica.impl;

import java.util.concurrent.ConcurrentHashMap;

public class SessionRegistry {

    private final ConcurrentHashMap<String, Long> sessions;

    public SessionRegistry() {
        this.sessions = new ConcurrentHashMap<>();
    }

    public void putSession(String client, Long nonce) {
        sessions.put(client,nonce);
    }

    public Long getSession(String client) {
        return sessions.get(client);
    }

    public void increment(String client) {
        Long nonce = sessions.get(client);
        if(nonce != null) {
            sessions.put(client,nonce+1);
        }
    }

    public boolean contains(String client) {
        return sessions.containsKey(client);
    }
}
