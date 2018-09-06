import React from "react";
import {BuildLink} from "../project/project";
import {Host} from "./misc/Host";
import './Instance.css'
import {Version} from "../common/Version";
import {CopyButton} from "../common/CopyButton";
import {Badge} from "../common/Badge";
import {ConfCommited} from "./misc/ConfCommited";
import {Expected} from "./misc/Expected";

class AppInstance extends React.Component {
    render() {
        const instance = this.props.instance;
        //let extra = instance.extra || {};
        let extra = instance || {}; // FIXME Extra
        //, alignItems: 'top', justifyContent: 'center'
        /*
        <Status status={instance.status} />
        <li><Leader leader={instance.leader} participating={instance.participating} cluster={instance.cluster}/></li>
        <li><Versions instance={instance}/></li>
        */
        return (
            <div className="instance">
                <div className="line">
                    <li><Badge><b>{instance.name}</b></Badge></li>
                    <li>
                        <Badge>
                            {instance.pid && <span>{instance.pid}@</span>}
                            <Host host={extra.host} expected={instance.deployHost}/>
                        </Badge>
                    </li>
                    <li><Version version={instance.version} revision={instance.revision} {...{color:'black', backgroundColor:'lightgray'}} /></li>
                    <li><Version version={instance.deployVersion} revision={instance.deployRevision} {...{color:'black', backgroundColor:'lightgray'}} /></li>
                    <li><Badge title={'Conf SVN revision'} backgroundColor='red' color='white'>{instance.confRevision}</Badge></li>
                    <li><ConfCommited commited={instance.confCommited}/></li>
                    <li><Expected expected={!instance.unexpected}/></li>
                    <li><BuildLink url={instance.project}/></li> {/* // TODO Project Build URL */}
                </div>
                <div className="line">
                    {instance.deployFolder && <li><Badge>{instance.deployFolder}</Badge></li>}
                    {instance.deployFolder && <li><CopyButton value={instance.deployFolder}/></li>}
                    {extra.quickfixPersistence && <li><Badge>{extra.quickfixPersistence}</Badge></li>}
                    {extra.busVersion && <li><Badge>{extra.busVersion}</Badge></li>}
                    {extra.coreVersion && <li><Badge>{extra.coreVersion}</Badge></li>}
                    {extra.jmx && <li><Badge>{extra.jmx}</Badge></li>}
                </div>
            </div>
        )
    }
}

export {AppInstance}