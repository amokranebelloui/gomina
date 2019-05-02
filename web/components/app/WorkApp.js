// @flow
import axios from "axios/index";
import {AppLayout, PrimarySecondaryLayout} from "./common/layout";
import React, {Fragment} from "react";
import {Well} from "../common/Well";
import Link from "react-router-dom/es/Link";
import type {CommitType} from "../commitlog/CommitLog";
import {CommitLog, CommitLogLegend} from "../commitlog/CommitLog";
import {InlineAdd} from "../common/InlineAdd";
import {Secure} from "../permission/Secure";
import Route from "react-router-dom/es/Route";
import {WorkEditor} from "../work/WorkEditor";
import {DateTime} from "../common/DateTime";
import {sortWorkBy, WorkSort} from "../work/WorkSort";
import ls from "local-storage";
import {filterWork, WorkFilter} from "../work/WorkFilter";
import type {ComponentRefType} from "../component/ComponentType";
import type {UserRefType} from "../misc/UserType";
import type {WorkDataType, WorkManifestType, WorkStatusType, WorkType} from "../work/WorkType";
import {Issue} from "../misc/Issue";
import type {EnvType} from "../environment/Environment";
import {WorkStatus} from "../work/WorkStatus";
import {joinTags, splitTags} from "../common/utils";

type Props = {
    match: any
}

type State = {
    envs: Array<EnvType>,
    refEnv: ?string,
    commitLimit: number,
    components: Array<ComponentRefType>,
    users: Array<UserRefType>,
    systems: Array<string>,

    search: string,
    selectedSystems: Array<string>,
    sortBy: string,
    workList: Array<WorkType>,
    workDetail?: ?WorkManifestType,
    workId: ?string,
    newWorkData?: ?WorkDataType,
    commitHighlight: string,

    workEdition: boolean,
    workEdited?: ?WorkDataType,
    selectedNewStatus?: ?WorkStatusType
}

let timeoutId;

class WorkApp extends React.Component<Props, State> {

    constructor(props: Props) {
        super(props);
        this.state = {
            envs: [],
            refEnv: ls.get('work.list.ref.env'),
            commitLimit: ls.get('work.list.commit.limit') || 100,
            components: [],
            users: [],
            systems: [],
            search: ls.get('work.list.search') || '',
            selectedSystems: splitTags(ls.get('components.systems')) || [],
            sortBy: ls.get('work.list.sort') || 'due-date',
            workList: [],
            workDetail: null,
            commitHighlight: ls.get('work.list.commit.highlight') || 'all',
            workId: this.props.match.params.id,
            newWorkData: null,
            workEdition: false
        };
        //this.retrieveWorkList = this.retrieveWorkList.bind(this);
        //this.retrieveWorkDetail = this.retrieveWorkDetail.bind(this);
        console.info("workApp !constructor ");
    }

    retrieveComponentRefs() {
        const thisComponent = this;
        axios.get('/data/components/refs')
            .then(response => thisComponent.setState({components: response.data}))
            .catch(error => thisComponent.setState({components: []}));
    }

    retrieveUserRefs() {
        const thisComponent = this;
        axios.get('/data/user/refs')
            .then(response => thisComponent.setState({users: response.data}))
            .catch(error => thisComponent.setState({users: []}));
    }
    retrieveSystems() {
        const thisComponent = this;
        axios.get('/data/systems/refs')
            .then(response => thisComponent.setState({systems: [...response.data, ""]}))
            .catch(error => thisComponent.setState({systems: []}));
    }

    setFilter(search: string, selectedSystems: Array<string>) {
        this.setState({search: search, selectedSystems: selectedSystems});
        ls.set('work.list.search', search);
        ls.set('components.systems', joinTags(selectedSystems))
    }
    sortChanged(sortBy: string) {
        this.setState({sortBy: sortBy});
        ls.set('work.list.sort', sortBy)
    }
    setCommitHighlight(commitHighlight: string, save: boolean = true) {
        this.setState({commitHighlight: commitHighlight});
        save && ls.set('work.list.commit.highlight', commitHighlight)
    }

    retrieveEnvs() {
        const thisComponent = this;
        axios.get('/data/envs')
            .then(response => thisComponent.setState({envs: response.data}))
            .catch(error => thisComponent.setState({envs: []}));
    }
    
    retrieveWorkList() {
        const thisComponent = this;
        axios.get('/data/work/list')
            .then(response => thisComponent.setState({workList: response.data}))
            .catch(() => thisComponent.setState({workList: []}));
    }
    retrieveWorkDetail(workId: string) {
        const thisComponent = this;
        axios.get('/data/work/detail' + (workId ? '/' + workId : ''))
            .then(response => thisComponent.setState({workDetail: response.data}))
            .catch(() => thisComponent.setState({workDetail: null}));
    }

    componentDidMount() {
        console.info("workApp !did mount ", this.props.match.params.id);
        this.retrieveEnvs();
        this.retrieveWorkList();
        this.retrieveWorkDetail(this.props.match.params.id);
        this.retrieveComponentRefs();
        this.retrieveUserRefs();
        this.retrieveSystems();
    }

    componentWillReceiveProps(nextProps: Props) {
        const newWorkId = nextProps.match.params.id;
        console.info("workApp !did willRecProps ", newWorkId, nextProps);
        this.setState({workId: newWorkId});
        if (this.props.match.params.id !== newWorkId) {
            this.setState({commitHighlight: ls.get('work.list.commit.highlight') || 'all'});
            this.retrieveWorkDetail(newWorkId)
        }
    }

    changeRefEnv(refEnv: string) {
        this.setState({"refEnv": refEnv});
        ls.set('work.list.ref.env', refEnv)
    }
    changeCommitLimit(limit: string) {
        this.setState({"commitLimit": limit && parseInt(limit) || 0});
        ls.set('work.list.commit.limit', limit)
    }

    clearNewWorkState() {
        this.setState({
            newWorkData: null
        })
    }
    addWork() {
        return axios.post('/data/work/add', this.state.newWorkData);
    };
    onWorkAdded(workId: string, history: any) {
        this.retrieveWorkList();
        history.push("/work/" + workId)
    };


    editWork() {
        this.setState({"workEdition": true});
    }
    changeWork(work: WorkDataType) {
        console.info("Work Changed", work);
        this.setState({"workEdited": work});
    }
    cancelWorkEdition() {
        this.setState({"workEdition": false});
    }
    updateWork() {
        const thisComponent = this;
        const workId = this.state.workId;
        workId && axios.put('/data/work/' + workId + '/update', this.state.workEdited)
            .then(() => {
                thisComponent.retrieveWorkList();
                thisComponent.retrieveWorkDetail(workId);
            })
            .catch((error) => console.log("Work update error", error.response));
        this.setState({"workEdition": false}); // FIXME Display Results/Errors
    }
    selectNewWorkStatus(status: WorkStatusType, current?: ?WorkStatusType) {
        this.setState({"selectedNewStatus": status});
        const thisComponent = this;
        timeoutId && clearTimeout(timeoutId);
        timeoutId = setTimeout(() => thisComponent.setState({"selectedNewStatus": current}), 4000);
    }
    changeWorkStatus(status: WorkStatusType) {
        const thisComponent = this;
        const workId = this.state.workId;
        workId && axios.put('/data/work/' + workId + '/change-status/' + status)
            .then(() => {
                thisComponent.retrieveWorkList();
                thisComponent.retrieveWorkDetail(workId);
            })
            .catch((error) => console.log("Work change status error", error.response));
    }
    archiveWork() {
        const thisComponent = this;
        const workId = this.state.workId;
        workId && axios.put('/data/work/' + workId + '/archive')
            .then(() => {
                thisComponent.retrieveWorkList();
                thisComponent.retrieveWorkDetail(workId);
            })
            .catch((error) => console.log("Work archive error", error.response));
    }
    unarchiveWork() {
        const thisComponent = this;
        const workId = this.state.workId;
        workId && axios.put('/data/work/' + workId + '/unarchive')
            .then(() => {
                thisComponent.retrieveWorkList();
                thisComponent.retrieveWorkDetail(workId);
            })
            .catch((error) => console.log("Work unarchive error", error.response));
    }

    commitsNotInEnv(commits: Array<CommitType>): Array<CommitType> {
        let index = null;
        for (let i = 0; i < commits.length; i++) {
            const commit = commits[i];
            if (commit.instances && commit.instances.filter(i => i.env === this.state.refEnv).length > 0 ||
                commit.deployments && commit.deployments.filter(i => i.env === this.state.refEnv).length > 0) {
                index = i;
            }
        }
        const count = index ? Math.min(index + 1, this.state.commitLimit) : this.state.commitLimit;
        return commits.slice(0, count);
    }

    render()  {
        const workList = filterWork(this.state.workList || [], this.state.search, this.state.selectedSystems);
        const workDetail = this.state.workDetail;
        //console.info("workDetail", workDetail);
        let highlightedIssues = [this.state.commitHighlight];
        if (this.state.commitHighlight === 'all') {
            highlightedIssues = null
        }
        else if (this.state.commitHighlight === 'work') {
            highlightedIssues = workDetail && workDetail.work && workDetail.work.issues.map(i => i.issue) || []
        }
        return (
            <AppLayout title="Work List">
                <PrimarySecondaryLayout>
                    <div>
                        {!(workDetail && workDetail.work) &&
                            <Fragment>
                                <div>
                                    Select a Work to see details<br/>
                                    <li>Components involved</li>
                                    <li>Commit logs</li>
                                    <li>etc...</li>
                                </div>
                                <hr/>
                            </Fragment>
                        }
                        {workDetail && workDetail.work &&
                            <div>
                                <div>
                                    {!this.state.workEdition &&
                                    <div>
                                        <div style={{float: 'right'}}>
                                            <Secure permission="work.status">
                                                <button onClick={() => this.state.selectedNewStatus && this.changeWorkStatus(this.state.selectedNewStatus)}>
                                                    {this.state.selectedNewStatus && this.state.selectedNewStatus !== workDetail.work.status &&
                                                    <span>Status to {this.state.selectedNewStatus}</span>
                                                    }
                                                </button>
                                            </Secure>
                                            {!workDetail.work.archived && [
                                                <Secure key="Edit" permission="work.manage">
                                                    <button onClick={() => this.editWork()}>Edit</button>
                                                </Secure>,
                                                <Secure key="Archive" permission="work.archive">
                                                    <button onClick={() => this.archiveWork()}>Archive</button>
                                                </Secure>
                                            ]}
                                            {workDetail.work.archived &&
                                            <Secure permission="work.archive">
                                                <button onClick={() => this.unarchiveWork()}>Restore</button>
                                            </Secure>
                                            }
                                        </div>
                                        <Secure permission="work.status"
                                                fallback={
                                                    <Work work={workDetail.work}/>
                                                }>
                                            <Work work={workDetail.work}
                                                  onStatusChange={(workId, s) => this.selectNewWorkStatus(s, workDetail.work && workDetail.work.status)}/>
                                        </Secure>
                                    </div>
                                    }
                                    {this.state.workEdition &&
                                    <div>
                                        <WorkEditor key={workDetail.work.id}
                                                    work={workDetail.work}
                                                    peopleRefs={this.state.users}
                                                    componentsRefs={this.state.components}
                                                    onChange={(id, w) => this.changeWork(w)} />
                                        <hr/>
                                        <button onClick={() => this.updateWork()}>Update</button>
                                        <button onClick={() => this.cancelWorkEdition()}>Cancel</button>
                                    </div>
                                    }
                                </div>
                            </div>
                        }
                        <div>
                            {workDetail && workDetail.work &&
                                <Fragment>
                                    <button disabled={this.state.commitHighlight === 'all'} onClick={e => this.setCommitHighlight('all')}>ALL</button>
                                    <button disabled={this.state.commitHighlight === 'work'} onClick={e => this.setCommitHighlight('work')}>WORK</button>
                                    {workDetail.work.issues.map(issue =>
                                        <button key={issue.issue} disabled={this.state.commitHighlight === issue.issue} onClick={e => this.setCommitHighlight(issue.issue, false)}>{issue.issue}</button>
                                    )}
                                </Fragment>
                            }
                            <div style={{float: 'right', display: 'inline-block'}}>
                                {this.state.refEnv}
                                <select name="type" value={this.state.refEnv}
                                        onChange={e => this.changeRefEnv(e.target.value)}
                                        style={{width: '150px', fontSize: 9}}>
                                    <option value=""></option>
                                    {this.state.envs.map(e =>
                                        <option key={e.env} value={e.env}>{e.description}</option>
                                    )}
                                </select>
                                <input type="text" value={this.state.commitLimit}
                                       onChange={e => this.changeCommitLimit(e.target.value)}
                                       style={{width: '30px'}} />
                            </div>
                        </div>
                        <div>
                        {workDetail && workDetail.details && workDetail.details.map(d =>
                            (workDetail.work || d.commits.length > 0) &&
                                <div key={d.componentId}>
                                    <h3>{d.componentId}</h3>
                                    <CommitLog type={d.scmType} commits={this.commitsNotInEnv(d.commits)} highlightedIssues={highlightedIssues} />
                                </div>
                        )}
                        </div>
                        <CommitLogLegend />
                    </div>
                    <div>
                        <Well block>
                            <WorkFilter search={this.state.search} systems={this.state.selectedSystems}
                                        systemsRefs={this.state.systems}
                                        onFilterChanged={(s, systems) => this.setFilter(s, systems)} />
                        </Well>

                        <Secure permission="work.add">
                            <Route render={({ history }) => (
                                <InlineAdd type="Work"
                                           action={() => this.addWork()}
                                           onEnterAdd={() => this.clearNewWorkState()}
                                           onItemAdded={(work) => this.onWorkAdded(work, history)}
                                           editionForm={() =>
                                               <WorkEditor peopleRefs={this.state.users}
                                                           componentsRefs={this.state.components}
                                                           onChange={(id, w) => this.setState({newWorkData: w})} />
                                           }
                                           successDisplay={(data: any) =>
                                               <div>
                                                   <b>{data ? ("Added " + JSON.stringify(data)) : 'no data returned'}</b>
                                               </div>
                                           }
                                />
                            )} />
                        </Secure>

                        <WorkSort sortBy={this.state.sortBy} onSortChanged={sortBy => this.sortChanged(sortBy)} />
                        <table width="100%">
                            <tbody>
                            <tr key="header">
                                <td><b>label</b></td>
                                <td><b>type</b></td>
                                <td><b>issues</b></td>
                                <td><b>status</b></td>
                                <td><b>components</b></td>
                                <td><b>people</b></td>
                                <td><b>created</b></td>
                                <td><b>due</b></td>
                            </tr>
                            {sortWorkBy(workList, this.state.sortBy).map(work =>
                                <tr key={work.id} style={{opacity: work.archived ? .5 : 1}}>
                                    <td>
                                        <Link to={"/work/" + work.id}>{work.label}</Link>
                                    </td>
                                    <td>{work.type}</td>
                                    <td>
                                        {work.issues.map(issue =>
                                            <Issue key={issue.issue} issue={issue} />
                                        )}
                                    </td>
                                    <td>{work.status}</td>
                                    <td>
                                        {work.components.map(c =>
                                            <span key={c.id}><Link to={"/component/" + c.id}>{c.label}</Link> </span>
                                        )}
                                    </td>
                                    <td>
                                        {work.people.map(p =>
                                            <span key={p.id}><Link to={"/user/" + p.id}>{p.shortName}</Link> </span>
                                        )}
                                    </td>
                                    <td>
                                        <DateTime date={work.creationDate} />
                                    </td>
                                    <td>
                                        <DateTime date={work.dueDate} />
                                    </td>
                                </tr>
                            )}
                            </tbody>
                        </table>
                    </div>
                </PrimarySecondaryLayout>
            </AppLayout>
        )
    }
}

function Work(props: {work: WorkType, onStatusChange?: (string, WorkStatusType) => void}) {
    const work = props.work;
    //border: '1px solid blue'
    return (
        <div style={{border: '1px solid gray', padding: '2px', minWidth: '80px'}}>
            <div>
                <Link to={"/work/" + work.id}>
                    <h3 style={{display: 'inline-block'}}>{work.label} {work.archived && "(Archived)"}</h3>
                </Link>
            </div>
            <div><i>&lt;{work.type}&gt;</i><i>&lt;{work.status}&gt;</i></div>
            <div><b>Issues </b>{work.issues.map(issue => <i key={issue.issue}>{issue.issue} </i>)}</div>
            <div>
                <b>People </b>
                {work.people.map(p =>
                    <span key={p.id} style={{color: 'blue'}}>
                        <Link to={"/user/" + p.id}>{p.shortName} </Link>
                    </span>
                )}
            </div>
            <div>
                <b>Components </b>
                {work.components.map(c =>
                    <span key={c.id} style={{color: 'blue'}}>
                        <Link to={"/component/" + c.id}>{c.label} </Link>
                    </span>
                )}
            </div>
            <div><b>Created </b><DateTime date={work.creationDate} /></div>
            <div><b>Due </b><DateTime date={work.dueDate} /></div>
            <div>
                <b>Status </b>
                <WorkStatus status={work.status}
                            onStatusChange={(s) => props.onStatusChange && props.onStatusChange(work.id, s)} />
            </div>
        </div>
    )
}

export {WorkApp}