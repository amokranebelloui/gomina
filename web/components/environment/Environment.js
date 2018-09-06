import React from "react";
import './Environment.css'
import {Service} from "./Service";
import {Instance} from "./Instance";

class EnvironmentLogical extends React.Component {
    constructor(props) {
        super(props);
    }
    render() {
        const services = this.props.services;
        return (
            <div className='env-table'>
                {services.map( svc =>
                    <div key={svc.service.svc} className='env-row'>
                        <Service service={svc.service} instances={svc.instances || []} highlightFunction={this.props.highlight} />
                        {(svc.instances || []).map(instance =>
                            <Instance key={instance.id} instance={instance} highlighted={this.props.highlight(instance)} />
                        )}
                    </div>
                )}
            </div>
        )
    }
}

export {EnvironmentLogical}
