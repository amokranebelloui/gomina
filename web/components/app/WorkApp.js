// @flow
import axios from "axios/index";
import React, {Fragment} from "react";
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
import {ApplicationLayout} from "./common/ApplicationLayout";
import {Badge} from "../common/Badge";

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
    componentHighlight: string,

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
            refEnv: ls.get('work.list.ref.env') || '',
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
            componentHighlight: ls.get('work.list.component.highlight') || 'all',
            workId: this.props.match.params.id,
            newWorkData: null,
            workEdition: false
        };
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
    setComponentHighlight(componentHighlight: string, save: boolean = true) {
        this.setState({componentHighlight: componentHighlight});
        save && ls.set('work.list.component.highlight', componentHighlight)
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
    retrieveWorkDetail(workId: string, refEnv?: ?string) {
        const thisComponent = this;
        const refEnv_ = typeof refEnv !== 'undefined' ? refEnv : this.state.refEnv;
        axios.get('/data/work/detail' + (workId ? '/' + workId : '') + (refEnv_ ? '?refEnv=' + refEnv_ : ''))
            .then(response => {
                document.title = response.data.work.label + ' - Work';
                thisComponent.setState({workDetail: response.data})
            })
            .catch(() => thisComponent.setState({workDetail: null}));
    }

    componentDidMount() {
        document.title = 'Work List';
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
        ls.set('work.list.ref.env', refEnv);
        this.state.workId && this.retrieveWorkDetail(this.state.workId, refEnv)
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

    limit(commits: Array<CommitType>): Array<CommitType> {
        const count = Math.min(commits.length, this.state.commitLimit);
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

        let componentFilter = d => true
        if (this.state.componentHighlight === 'changes') {
            componentFilter = d => d.commits.length > 1
        }

        return (
            <ApplicationLayout title="Work List"
               header={() =>
                   <Fragment>
                       {!(workDetail && workDetail.work) &&
                           <Fragment>
                               <div>
                                   Select a Work to see details<br/>
                                   <li>Components involved</li>
                                   <li>Commit logs</li>
                                   <li>etc...</li>
                               </div>
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
                           <div style={{display: 'inline-block'}}>
                               <button disabled={this.state.commitHighlight === 'all'} onClick={e => this.setCommitHighlight('all')}>ALL</button>
                               <button disabled={this.state.commitHighlight === 'work'} onClick={e => this.setCommitHighlight('work')}>WORK</button>
                               {workDetail.work.issues.map(issue =>
                                   <button key={issue.issue} disabled={this.state.commitHighlight === issue.issue} onClick={e => this.setCommitHighlight(issue.issue, false)}>{issue.issue}</button>
                               )}
                           </div>
                       }
                           <div style={{float: 'right'}}>
                               {workDetail && workDetail.details &&
                                    workDetail.details.filter(componentFilter).length + '/' + workDetail.details.length + ' components '
                               }
                               <button disabled={this.state.componentHighlight === 'all'} onClick={e => this.setComponentHighlight('all')}>ALL</button>
                               <button disabled={this.state.componentHighlight === 'changes'} onClick={e => this.setComponentHighlight('changes')}>CHANGES</button>
                           </div>
                           <div style={{clear: 'both'}} />
                       </div>
                   </Fragment>
               }
               main={() =>
                   <Fragment>
                       <div style={{float: 'right', display: 'inline-block'}}>
                           {this.state.refEnv}&nbsp;
                           <select name="type" value={this.state.refEnv}
                                   onChange={e => this.changeRefEnv(e.target.value)}
                                   style={{width: '150px', fontSize: 9}}>
                               <option value=""></option>
                               {this.state.envs.map(e =>
                                   <option key={e.env} value={e.env}>{e.description} ({e.env})</option>
                               )}
                           </select>
                           <input type="text" value={this.state.commitLimit}
                                  onChange={e => this.changeCommitLimit(e.target.value)}
                                  style={{width: '30px'}} />
                       </div>
                       <div>
                           {workDetail && workDetail.details &&
                                <Fragment>
                                    {workDetail.details.filter(componentFilter).sort((a,b)=>a.componentLabel>b.componentLabel?1:-1).map(d =>
                                       (workDetail.work || d.commits.length > 0) &&
                                       <div key={d.componentId}>
                                           <h3 title={d.componentId}>
                                                <Link to={"/component/" + d.componentId}>{d.componentLabel}</Link>
                                                &nbsp;
                                                {d.notDeployed ? (this.state.refEnv && 'Not Deployed') : d.upToDate ? 'Up To Date' : (d.commits.length - 1)+' changes'}
                                           </h3>
                                           <CommitLog type={d.scmType} commits={this.limit(d.commits)} highlightedIssues={highlightedIssues} />
                                           {d.commits.length > this.state.commitLimit && <i>... {d.commits.length - this.state.commitLimit} more filtered commits</i>}
                                       </div>
                                    )}
                                </Fragment>
                           }
                       </div>
                       <CommitLogLegend />
                   </Fragment>
               }
               sidePrimary={() =>
                   <Fragment>
                       <WorkFilter search={this.state.search} systems={this.state.selectedSystems}
                                   systemsRefs={this.state.systems}
                                   onFilterChanged={(s, systems) => this.setFilter(s, systems)} />
                       <hr/>
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
                       <hr/>
                       <WorkSort sortBy={this.state.sortBy} onSortChanged={sortBy => this.sortChanged(sortBy)} />
                   </Fragment>
               }
               sideSecondary={() =>
                   <table width="100%">
                       <tbody>
                       <tr key="header">
                           <td>&nbsp;</td>
                           <td><b>type</b></td>
                           <td><b>status</b></td>
                           <td><b>created</b></td>
                           <td><b>due</b></td>
                       </tr>
                       {sortWorkBy(workList, this.state.sortBy).map(work =>
                           <tr key={work.id} style={{opacity: work.archived ? .5 : 1}}>
                               <td style={{borderBottom: '1px solid gray'}}>
                                   <Link to={"/work/" + work.id}><b>{work.label}</b></Link>
                                   &nbsp;
                                   <span className="items">
                                   {work.issues.map(issue =>
                                       <Issue key={issue.issue} issue={issue} />
                                   )}
                                   </span>
                                   <br/>
                                   <div className="items" style={{color: '#42748e'}}>
                                   {work.components.map(c =>
                                       <span key={c.id}>
                                           <Link to={"/component/" + c.id}>{c.label}</Link>
                                       </span>
                                   )}
                                   </div>
                                   <div className="items">
                                   {work.people.map(p =>
                                       <span key={p.id} style={{color: 'gray'}}>
                                           <Link to={"/user/" + p.id}><i>{p.shortName}</i></Link>
                                       </span>
                                   )}
                                   </div>
                               </td>
                               <td style={{borderBottom: '1px solid gray'}}>
                                   {work.type}
                               </td>
                               <td style={{borderBottom: '1px solid gray'}}>
                                   {work.status}
                               </td>
                               <td style={{borderBottom: '1px solid gray'}}>
                                   <DateTime date={work.creationDate} displayTime={false} />
                               </td>
                               <td style={{borderBottom: '1px solid gray'}}>
                                   <DateTime date={work.dueDate} displayTime={false} />
                               </td>
                           </tr>
                       )}
                       </tbody>
                   </table>
               }
            />
        )
    }
}

function Work(props: {work: WorkType, onStatusChange?: (string, WorkStatusType) => void}) {
    const work = props.work;
    //border: '1px solid blue'
    return (
        <div style={{padding: '2px', minWidth: '80px'}}>
            <div style={{marginBottom: '4px'}}>
                <Link to={"/work/" + work.id}>
                    <h3 style={{display: 'inline-block'}}>{work.label} {work.archived && "(Archived)"}</h3>
                </Link>
                &nbsp;
                <i>&lt;{work.type}&gt;</i>
            </div>
            <div>
                <WorkStatus status={work.status}
                            onStatusChange={(s) => props.onStatusChange && props.onStatusChange(work.id, s)} />
            </div>
            <div>
                <b>Issues </b>
                <span className="items">
                    {work.issues.map(issue =>
                        <Badge key={issue.issue} style={{color: '#42748e'}}>
                            <Issue issue={issue} />
                        </Badge>
                    )}
                    {work.missingIssues.map(issue =>
                        <Badge key={issue.issue} style={{opacity: 0.5}}>
                            <Issue issue={issue} />
                        </Badge>
                    )}
                </span>
            </div>
            <div>
                <b>People </b>
                {work.people.map(p =>
                    <Badge key={p.id} style={{color: '#42748e'}}>
                        <Link to={"/user/" + p.id}>{p.shortName} </Link>
                    </Badge>
                )}
            </div>
            <div>
                <b>Components </b>
                {work.components.map(c =>
                    <Badge key={c.id} style={{color: '#42748e'}}>
                        <Link to={"/component/" + c.id}>{c.label} </Link>
                    </Badge>
                )}
                {work.missingComponents.map(c =>
                    <Badge key={c.id} style={{opacity: 0.5}}>
                        <Link to={"/component/" + c.id}>{c.label} </Link>
                    </Badge>
                )}
            </div>
            <div><b>Created </b><DateTime date={work.creationDate} /></div>
            <div><b>Due </b><DateTime date={work.dueDate} /></div>
        </div>
    )
}

export {WorkApp}