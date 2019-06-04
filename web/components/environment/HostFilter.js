// @flow
import * as React from "react"
import type {EnvHostsType} from "./Environment";
import {TagCloud} from "../common/TagCloud";
import {flatMap} from "../common/utils";
import type {HostType} from "./HostType";

type Props = {
    envsHosts: Array<EnvHostsType>,
    onFilterChanged?: (search: string, hosts: Array<string>) => void
}

type State = {
    search: string,
    hosts: Array<string>
}

class HostFilter extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);
        this.state = {
            search: '',
            hosts: []
        }
    }
    setSearch(search: string) {
        this.setState({search: search});
        this.props.onFilterChanged && this.props.onFilterChanged(search, this.state.hosts)
    }
    selectEnvs(envs: Array<string>) {
        const hosts = flatMap(this.props.envsHosts.filter(e => envs.includes(e.env)), e => e.hosts);
        this.setState({hosts: hosts});
        this.props.onFilterChanged && this.props.onFilterChanged(this.state.search, hosts)
    }
    render() {
        const envsHosts = this.props.envsHosts || [];
        return (
            <div>
                <b>Search: </b>
                <input type="text" name="search" value={this.state.search} onChange={e => this.setSearch(e.target.value)}/>
                <br/>
                <b>Environment: </b>
                <TagCloud tags={envsHosts.map(e => e.env)} displayCount={false}
                          selectionChanged={envs => this.selectEnvs(envs)} />
            </div>
        );
    }
}

function filterHosts(hosts: Array<HostType>, search: string, envHosts: Array<string>) {
    return hosts.filter(h => matchesSearch(h, search, envHosts));
}

function matchesSearch(host: HostType, search: string, envHosts: Array<string>) {
    let regExp = new RegExp(search, "i");
    let matchesHostName = host.host && host.host.match(regExp);
    let matchesDataCenter = host.dataCenter && host.dataCenter.match(regExp);
    let matchesGroup = host.group && host.group.match(regExp);
    let matchesOsFamily = host.osFamily && host.osFamily.match(regExp);
    let matchesOs = host.os && host.os.match(regExp);
    //let matchesIssues = host.issues && host.issues.find(value => value && value.issue.match(regExp));
    let matchesEnvs = (!(envHosts && envHosts.length > 0) || envHosts.includes(host.host));
    return (matchesHostName || matchesDataCenter || matchesGroup || matchesOsFamily || matchesOs) && matchesEnvs
}


export { HostFilter, filterHosts }