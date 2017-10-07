package org.neo.gomina.api.instances;

public class Instance {

    public String env;
    public String id; // Unique by env
    public String name; // X Replication
    public String service; // Z Partitioning
    public String type; // Y Functional

    public String pid;
    public String host;
    public String status;

    public String project;
    public String deployHost;
    public String deployFolder;
    public String deployVersion;
    public String deployRevision;
    public Boolean confCommited;
    public Boolean confUpToDate;
    public String version;
    public String revision;

    public Integer jmx = null;
    public String busVersion;
    public String coreVersion;
    public String quickfixPersistence;
    public String redisHost;
    public Integer redisPort;
    public String redisMasterHost;
    public Integer redisMasterPort;
    public Boolean redisMasterLink;
    public String redisMasterLinkDownSince;
    public Integer redisOffset;
    public Integer redisOffsetDiff;
    public Boolean redisMaster;
    public String redisRole;
    public String redisRW;
    public String redisMode;
    public String redisStatus;
    public Integer redisSlaveCount;
    public Integer redisClientCount;

}
