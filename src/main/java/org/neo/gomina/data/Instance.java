package org.neo.gomina.data;

public class Instance {

    private String env = "PROD";
    private String id = "-1"; // Unique by env
    private String type = "redis"; // Y Functional
    private String service = "redis"; // Z Partitioning
    private String name = "redis1"; // X Replication

    private String pid = "13456";
    private String host = "10.143.29.42";
    private String status = "LIVE";

    private String project = "redis";
    private String deployHost = "10.143.29.42";
    private String deployFolder = "/home/svc/prod-redis";
    private boolean confCommited = false;
    private boolean confUpToDate = false;
    private String version = "1.0.0";
    private String revision = "34560";

    private String jmx = null;
    private String busVersion = "2.0";
    private String coreVersion = "1.1";
    private String quickfixPersistence = null;
    private String redisHost = "vil";
    private Integer redisPort = 2345;
    private String redisMasterHost = "";
    private String redisMasterPort = null;
    private String redisMasterLink = null;
    private String redisMasterLinkDownSince = null;
    private Integer redisOffset = 543445;
    private Integer redisOffsetDiff = 0;
    private Boolean redisMaster = true;
    private String redisRole = "MASTER";
    private String redisRW = "rw";
    private String redisMode = "AOF";
    private String redisStatus = "LIVE";
    private Integer redisSlaveCount = 1;
    private Integer redisClientCount = 12;

}
