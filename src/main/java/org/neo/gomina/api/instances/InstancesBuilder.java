package org.neo.gomina.api.instances;

import org.apache.commons.lang3.StringUtils;
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
        Map<String, Instance> instances = new HashMap<>();
        sshConnector.analyze();
        for (Environment environment : inventory.getEnvironments()) {
            //EnvMonitoring envMonitoring = monitoring.getFor(environment.id);
            if (environment.services != null) {
                for (Service service : environment.services) {
                    for (InvInstance envInstance : service.instances) {
                        String id = environment.id + "-" + envInstance.id;
                        Instance instance = new Instance();
                        instance.id = id;
                        instance.env = environment.id;
                        instance.type = service.type;
                        instance.service = service.svc;
                        instance.name = envInstance.id;

                        applyInventory(instance, service, envInstance);

                        Project project = projects.getProject(service.project);
                        ScmDetails scmDetails = project != null ? scmConnector.getSvnDetails(project.svnRepo, project.svnUrl) : new ScmDetails();
                        applyScm(instance, scmDetails);

                        // apply project

                        SshDetails sshDetails = sshConnector.getDetails(envInstance.host, envInstance.folder);
                        applySsh(instance, sshDetails);
                        instances.put(id, instance);
                    }
                }
            }
        }

        for (Environment environment : inventory.getEnvironments()) {
            EnvMonitoring monitoring = this.monitoring.getFor(environment.getId());
            for (Map.Entry<String, Map<String, Object>> entry : monitoring.getAll().entrySet()) {
                String instanceId = entry.getKey();
                Map<String, Object> indicators = entry.getValue();
                String id = environment.id + "-" + instanceId;
                Instance instance = instances.get(id);
                if (instance == null) {
                    instance = new Instance();
                    instance.id = id;
                    instance.env = environment.id;
                    instance.type = (String)indicators.get("type");
                    instance.service = (String)indicators.get("service");
                    instance.name = instanceId;
                    instance.unexpected = true;
                    instances.put(id, instance);
                }
                applyMonitoring(instance, indicators);
                if (StringUtils.isNotBlank(instance.deployHost) && !StringUtils.equals(instance.deployHost, instance.host)) {
                    instance.unexpectedHost = true;
                }
            }
        }
        return new ArrayList<>(instances.values());
    }

    private void applyInventory(Instance instance, Service service, InvInstance envInstance) {
        instance.project = service.project;

        instance.deployHost = envInstance.host;
        instance.deployFolder = envInstance.folder;
    }

    private void applySsh(Instance instance, SshDetails sshDetails) {
        instance.deployVersion = sshDetails.deployedVersion;
        instance.deployRevision = sshDetails.deployedRevision;
        instance.confCommited = sshDetails.confCommitted;
        instance.confUpToDate = sshDetails.confUpToDate;
    }

    private void applyScm(Instance instance, ScmDetails scmDetails) {
        instance.latestVersion = scmDetails.latest;
        instance.latestRevision = scmDetails.latestRevision;
        instance.releasedVersion = scmDetails.released;
        instance.releasedRevision = scmDetails.releasedRevision;
    }

    private void applyMonitoring(Instance instance, Map<String, Object> indicators) {
        instance.pid = (String)indicators.get("pid");
        instance.host = (String)indicators.get("host");
        instance.version = (String)indicators.get("version");
        instance.revision = String.valueOf(indicators.get("revision"));

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

}
