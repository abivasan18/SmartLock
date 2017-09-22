package com.abi.smartlogin.entity;

import java.io.Serializable;

public class User implements Serializable {

    private int id = -1;
    private String username;
    private String password;
    private int wallpaperId;
    private UserRole role;
    private int confidence;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getWallpaperId() {
        return wallpaperId;
    }

    public void setWallpaperId(int wallpaperId) {
        this.wallpaperId = wallpaperId;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public int getConfidence() {
        return confidence;
    }

    public void setConfidence(int confidence) {
        this.confidence = confidence;
    }

    @Override
    public String toString() {
        return this.username;
    }
}
