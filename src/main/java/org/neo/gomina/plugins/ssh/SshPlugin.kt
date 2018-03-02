package org.neo.gomina.plugins.ssh

import org.neo.gomina.model.instances.Instance
import org.neo.gomina.model.inventory.InvInstance
import org.neo.gomina.model.inventory.Service
import org.neo.gomina.model.sshinfo.SshDetails


fun Instance.applyInventory(service: Service, envInstance: InvInstance) {
    this.project = service.project
    this.deployHost = envInstance.host
    this.deployFolder = envInstance.folder
}

fun Instance.applySsh(sshDetails: SshDetails) {
    this.deployVersion = sshDetails.deployedVersion
    this.deployRevision = sshDetails.deployedRevision
    this.confCommited = sshDetails.confCommitted
    this.confUpToDate = sshDetails.confUpToDate
}

