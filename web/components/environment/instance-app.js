import React from "react";
import {Badge, CopyButton} from "../common/component-library";
import {BuildLink} from "../project";
import {ConfCommited, Expected, Host, Leader, Versions} from "./instance-common";

class AppInstance extends React.Component {
    render() {
        const instance = this.props.instance;
        //let extra = instance.extra || {};
        let extra = instance || {}; // FIXME Extra
        //, alignItems: 'top', justifyContent: 'center'
        /*<Status status={instance.status} />*/
        return (
            <div>
                <div>
                    <span>{instance.name}</span>
                    &nbsp;
                    <Leader leader={instance.leader} participating={instance.participating} cluster={instance.cluster}/>

                    &nbsp;
                    <Versions instance={instance}/>
                    &nbsp;
                    <ConfCommited commited={instance.confCommited}/>
                    <Expected expected={!instance.unexpected}/>

                    <BuildLink url={instance.project}/> {/* // TODO Project Build URL */}
                </div>
                <div style={{display: 'flex', fontSize: 10}}>
                    {instance.pid && <span>{instance.pid}@</span>}
                    <span><Host host={extra.host} expected={instance.deployHost}/></span>&nbsp;
                    <CopyButton value={instance.deployFolder}/>&nbsp;
                    <span>{instance.deployFolder}</span>
                </div>
                <div>
                    <Badge>{extra.quickfixPersistence}</Badge>
                    <Badge>{extra.busVersion}</Badge>
                    <Badge>{extra.coreVersion}</Badge>
                    <Badge>{extra.jmx}</Badge>
                </div>
            </div>
        )
    }
}

export {AppInstance}