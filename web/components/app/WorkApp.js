// @flow
import axios from "axios/index";
import {AppLayout, PrimarySecondaryLayout} from "./common/layout";
import React from "react";
import {Well} from "../common/Well";
import Link from "react-router-dom/es/Link";
import {CommitLog} from "../commitlog/CommitLog";
import {InlineAdd} from "../common/InlineAdd";
import {Secure} from "../permission/Secure";
import Route from "react-router-dom/es/Route";
import {WorkEditor} from "../work/WorkEditor";
import {Autocomplete} from "../common/AutoComplete";
import {DateTime} from "../common/DateTime";
import {sortWorkBy, WorkSort} from "../work/WorkSort";
import ls from "local-storage";
import {filterWork, WorkFilter} from "../work/WorkFilter";
import type {ComponentRefType} from "../component/ComponentType";
import type {UserRefType} from "../misc/UserType";
import type {WorkDataType, WorkManifestType, WorkType} from "../work/WorkType";

type Props = {
    match: any
}

type State = {
    components: Array<ComponentRefType>,
    users: Array<UserRefType>,
    search: string,
    sortBy: string,
    workList: Array<WorkType>,
    workDetail?: ?WorkManifestType,
    workId: ?string,
    newWorkData?: ?WorkDataType,

    workEdition: boolean,
    workEdited?: ?WorkDataType,
}

class WorkApp extends React.Component<Props, State> {

    constructor(props: Props) {
        super(props);
        this.state = {
            components: [],
            users: [],
            search: ls.get('work.list.search') || '',
            sortBy: ls.get('work.list.sort') || 'due-date',
            workList: [],
            workDetail: null,
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

    setFilter(search: string) {
        this.setState({search: search});
        ls.set('work.list.search', search)
    }
    sortChanged(sortBy: string) {
        this.setState({sortBy: sortBy});
        ls.set('work.list.sort', sortBy)
    }
    retrieveWorkList() {
        console.log("workApp Retr Hosts ... ");
        const thisComponent = this;
        axios.get('/data/work/list')
            .then(response => {
                console.log("workApp data workList", response.data);
                thisComponent.setState({workList: response.data});
            })
            .catch(function (error) {
                console.log("workApp error", error);
                thisComponent.setState({workList: []});
            });
    }
    retrieveWorkDetail(workId: string) {
        console.log("workApp Retr Hosts ... ");
        const thisComponent = this;
        axios.get('/data/work/detail' + (workId ? '/' + workId : ''))
            .then(response => {
                console.log("workApp data workDetail", response.data);
                thisComponent.setState({workDetail: response.data});
            })
            .catch(function (error) {
                console.log("workApp error workDetail", error);
                thisComponent.setState({workDetail: null});
            });
    }

    componentDidMount() {
        console.info("workApp !did mount ", this.props.match.params.id);
        this.retrieveWorkList();
        this.retrieveWorkDetail(this.props.match.params.id);
        this.retrieveComponentRefs();
        this.retrieveUserRefs();
    }

    componentWillReceiveProps(nextProps: Props) {
        const newWorkId = nextProps.match.params.id;
        console.info("workApp !did willRecProps ", newWorkId, nextProps);
        this.setState({workId: newWorkId});
        if (this.props.match.params.id !== newWorkId && newWorkId) {
            this.retrieveWorkDetail(newWorkId)
        }
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

    render()  {
        const workList = filterWork(this.state.workList || [], this.state.search);
        const workDetail = this.state.workDetail;
        //console.info("workDetail", workDetail);
        return (
            <AppLayout title="Work List">
                <PrimarySecondaryLayout>
                    <div>
                        {workDetail && workDetail.work ?
                            <div>
                                {workDetail.work &&
                                    <div>
                                        {!this.state.workEdition &&
                                            <div>
                                                <div style={{float: 'right'}}>
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
                                                <Work key={workDetail.work.id} work={workDetail.work}></Work>
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
                                }
                                {workDetail.details && workDetail.details.map(d =>
                                    <div key={d.componentId}>
                                        <h3>{d.componentId}</h3>
                                        <CommitLog commits={d.commits} />
                                    </div>
                                )}
                            </div>
                            :
                            <div>
                                Select a Work to see details<br/>
                                <li>Components involved</li>
                                <li>Commit logs</li>
                                <li>etc...</li>

                                <Autocomplete suggestions={this.state.components}
                                              idProperty="id" labelProperty="label" />
                                <Autocomplete suggestions={this.state.users}
                                              idProperty="id" labelProperty="shortName" />
                            </div>
                        }
                    </div>
                    <div>
                        <Well block>
                            <WorkFilter search={this.state.search} onFilterChanged={s => this.setFilter(s)} />
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
                                        {work.issues.map(issue => issue.issueUrl
                                            ? (<a href={issue.issueUrl} target="_blank">{issue.issue}</a>)
                                            : (issue.issue)
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

function Work(props) {
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
            <div><b>Issues </b>{work.issues.map(issue => <i>{issue.issue}</i>)}</div>
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
        </div>
    )
}

export {WorkApp}