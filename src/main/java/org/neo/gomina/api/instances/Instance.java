package org.neo.gomina.api.instances;

public class Instance {

    // FIXME Clean values

    public String env;
    public String id; // Unique by env
    public String type; // Y Functional
    public String service; // Z Partitioning
    public String name; // X Replication

    public String pid = "13456";
    public String host;
    public String status;

    public String project;
    public String deployHost;
    public String deployFolder;
    public Boolean confCommited = false;
    public Boolean confUpToDate = false;
    public String version = "1.0.0";
    public String revision = "34560";

    public Integer jmx = null;
    public String busVersion = "2.0";
    public String coreVersion = "1.1";
    public String quickfixPersistence = null;
    public String redisHost = "vil";
    public Integer redisPort = 2345;
    public String redisMasterHost = "";
    public Integer redisMasterPort = null;
    public Boolean redisMasterLink = null;
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
