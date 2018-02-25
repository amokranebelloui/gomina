package org.neo.gomina.api.instances;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.neo.gomina.model.inventory.Environment;
import org.neo.gomina.model.inventory.InvInstance;
import org.neo.gomina.model.inventory.Inventory;
import org.neo.gomina.model.inventory.Service;
import org.neo.gomina.model.monitoring.EnvMonitoring;
import org.neo.gomina.model.monitoring.Monitoring;
import org.neo.gomina.model.project.Project;
import org.neo.gomina.model.project.Projects;
import org.neo.gomina.model.scminfo.ScmConnector;
import org.neo.gomina.model.scminfo.ScmDetails;
import org.neo.gomina.model.sshinfo.SshConnector;
import org.neo.gomina.model.sshinfo.SshDetails;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InstancesBuilder {

    @Inject private Inventory inventory;
    @Inject private SshConnector sshConnector;
    @Inject private Monitoring monitoring;

    @Inject private Projects projects;
    @Inject private ScmConnector scmConnector;

    public List<Instance> getInstances() {
        Map<String, Instance> instancesMap = new HashMap<>();
        List<Instance> instancesList = new ArrayList<>();
        sshConnector.analyze();
        for (Environment environment : inventory.getEnvironments()) {
            //EnvMonitoring envMonitoring = monitoring.getFor(environment.id);
            if (environment.getServices() != null) {
                for (Service service : environment.getServices()) {
                    for (InvInstance envInstance : service.getInstances()) {
                        String id = environment.getId() + "-" + envInstance.getId();
                        Instance instance = new Instance();
                        instance.id = id;
                        instance.env = environment.getId();
                        instance.type = service.getType();
                        instance.service = service.getSvc();
                        instance.name = envInstance.getId();

                        applyInventory(instance, service, envInstance);

                        Project project = projects.getProject(service.getProject());
                        ScmDetails scmDetails = project != null ? scmConnector.getSvnDetails(project.getSvnRepo(), project.getSvnUrl()) : new ScmDetails();
                        applyScm(instance, scmDetails);

                        // apply project

                        SshDetails sshDetails = sshConnector.getDetails(envInstance.getHost(), envInstance.getFolder());
                        applySsh(instance, sshDetails);
                        instancesMap.put(id, instance);
                        instancesList.add(instance);
                    }
                }
            }
        }

        for (Environment environment : inventory.getEnvironments()) {
            EnvMonitoring monitoring = this.monitoring.getFor(environment.getId());
            for (Map.Entry<String, Map<String, Object>> entry : monitoring.getAll().entrySet()) {
                String instanceId = entry.getKey();
                Map<String, Object> indicators = entry.getValue();
                String id = environment.getId() + "-" + instanceId;
                Instance instance = instancesMap.get(id);
                if (instance == null) {
                    instance = new Instance();
                    instance.id = id;
                    instance.env = environment.getId();
                    instance.type = (String)indicators.get("type");
                    instance.service = (String)indicators.get("service");
                    instance.name = instanceId;
                    instance.unexpected = true;
                    instancesMap.put(id, instance);
                    instancesList.add(instance);
                }
                applyMonitoring(instance, indicators);
                if (StringUtils.isNotBlank(instance.deployHost) && !StringUtils.equals(instance.deployHost, instance.host)) {
                    instance.unexpectedHost = true;
                }
            }
        }
        return instancesList;
    }

    private void applyInventory(Instance instance, Service service, InvInstance envInstance) {
        instance.project = service.getProject();

        instance.deployHost = envInstance.getHost();
        instance.deployFolder = envInstance.getFolder();
    }

    private void applySsh(Instance instance, SshDetails sshDetails) {
        instance.deployVersion = sshDetails.deployedVersion;
        instance.deployRevision = sshDetails.deployedRevision;
        instance.confCommited = sshDetails.confCommitted;
        instance.confUpToDate = sshDetails.confUpToDate;
    }

    private void applyScm(Instance instance, ScmDetails scmDetails) {
        instance.latestVersion = scmDetails.getLatest();
        instance.latestRevision = scmDetails.getLatestRevision();
        instance.releasedVersion = scmDetails.getReleased();
        instance.releasedRevision = scmDetails.getReleasedRevision();
    }

    private void applyMonitoring(Instance instance, Map<String, Object> indicators) {
        instance.pid = (String)indicators.get("pid");
        instance.host = (String)indicators.get("host");
        instance.version = (String)indicators.get("version");
        instance.revision = String.valueOf(indicators.get("revision"));

        instance.cluster = defaultTo((Boolean)indicators.get("cluster"), false);
        instance.participating = defaultTo((Boolean)indicators.get("participating"), false);
        instance.leader = defaultTo((Boolean)indicators.get("leader"), isLive(indicators));

        instance.status = (String)indicators.get("status");
        instance.jmx = (Integer) indicators.get("jmx");
        instance.busVersion = (String)indicators.get("busVersion");
        instance.coreVersion = (String)indicators.get("coreVersion");
        instance.quickfixPersistence = (String)indicators.get("quickfixPersistence");
        instance.redisHost = (String)indicators.get("redisHost");
        instance.redisPort = (Integer) indicators.get("redisPort");
        instance.redisMasterHost = (String)indicators.get("redisMasterHost");
        instance.redisMasterPort = (Integer)indicators.get("redisMasterPort");
        instance.redisMasterLink = (Boolean) indicators.get("redisMasterLink");
        instance.redisMasterLinkDownSince = (String)indicators.get("redisMasterLinkDownSince");
        instance.redisOffset = (Integer) indicators.get("redisOffset");
        instance.redisOffsetDiff = (Integer) indicators.get("redisOffsetDiff");
        instance.redisMaster = (Boolean) indicators.get("redisMaster");
        instance.redisRole = (String)indicators.get("redisRole");
        instance.redisRW = (String)indicators.get("redisRW");
        instance.redisMode = (String)indicators.get("redisMode");
        instance.redisStatus = (String)indicators.get("redisStatus");
        instance.redisSlaveCount = (Integer) indicators.get("redisSlaveCount");
        instance.redisClientCount = (Integer) indicators.get("redisClientCount");
    }

    // FIXME Easier to have it on the UI level
    private boolean isLive(Map<String, Object> indicators) {
        LocalDateTime timestamp = (LocalDateTime) indicators.get("timestamp");
        boolean delayed = timestamp != null ? new LocalDateTime(DateTimeZone.UTC).minusSeconds(1).isAfter(timestamp): true;
        return delayed;
    }

    private boolean defaultTo(Boolean cluster, boolean defaultVal) {
        return cluster != null ? cluster : defaultVal;
    }

}
