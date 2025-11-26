package com.intramural.scheduling.dao;

import com.intramural.scheduling.service.AuthenticationService;

public class TestHash {
    public static void main(String[] args) {
        AuthenticationService auth = new AuthenticationService();
        
        String password = "admin123";
        String hash = auth.hashPassword(password);
        
        System.out.println("Password: " + password);
        System.out.println("Hash: " + hash);
        System.out.println("Expected: n4bQgYhMfWWaL+qgxVrQFaO/TxsrC4Is0V1sFbDwCgg=");
        System.out.println("Match: " + hash.equals("n4bQgYhMfWWaL+qgxVrQFaO/TxsrC4Is0V1sFbDwCgg="));
    }
}
