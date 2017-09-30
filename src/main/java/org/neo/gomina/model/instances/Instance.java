package org.neo.gomina.model.instances;

public class Instance {

    // FIXME Clean values

    public String env = "PROD";
    public String id = "-1"; // Unique by env
    public String type = "redis"; // Y Functional
    public String service = "redis"; // Z Partitioning
    public String name = "redis1"; // X Replication

    public String pid = "13456";
    public String host = "10.143.29.42";
    public String status = "LIVE";

    public String project = "redis";
    public String deployHost = "10.143.29.42";
    public String deployFolder = "/home/svc/prod-redis";
    public boolean confCommited = false;
    public boolean confUpToDate = false;
    public String version = "1.0.0";
    public String revision = "34560";

    public String jmx = null;
    public String busVersion = "2.0";
    public String coreVersion = "1.1";
    public String quickfixPersistence = null;
    public String redisHost = "vil";
    public Integer redisPort = 2345;
    public String redisMasterHost = "";
    public String redisMasterPort = null;
    public String redisMasterLink = null;
    public String redisMasterLinkDownSince = null;
    public Integer redisOffset = 543445;
    public Integer redisOffsetDiff = 0;
    public Boolean redisMaster = true;
    public String redisRole = "MASTER";
    public String redisRW = "rw";
    public String redisMode = "AOF";
    public String redisStatus = "LIVE";
    public Integer redisSlaveCount = 1;
    public Integer redisClientCount = 12;

}
