package org.neo.gomina.model;

public class RedisInfos {

    private String redisHost = "vil";
    private Integer redisPort = 2345;

    private Boolean redisMaster = true;
    private String redisRole = "MASTER";

    private String redisMasterHost = "";
    private String redisMasterPort = null;
    private String redisMasterLink = null;
    private String redisMasterLinkDownSince = null;
    private Integer redisOffset = 543445;
    private Integer redisOffsetDiff = 0;

    private String redisRW = "rw";
    private String redisMode = "AOF";
    private String redisStatus = "LIVE";
    private Integer redisSlaveCount = 1;
    private Integer redisClientCount = 12;

}
