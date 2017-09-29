package org.neo.gomina.model;

public class InventoryInstance {

    private String env = "PROD";
    private String id = "-1"; // Unique by env
    private String type = "redis"; // Y Functional
    private String service = "redis"; // Z Partitioning
    private String name = "redis1"; // X Replication
    private String project = "redis";

}
