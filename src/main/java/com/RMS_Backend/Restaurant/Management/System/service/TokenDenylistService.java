package com.RMS_Backend.Restaurant.Management.System.service;

public interface TokenDenylistService {
    void addToDenylist(String jti, long ttlInSeconds);

    boolean isTokenDenied(String jti);
}
