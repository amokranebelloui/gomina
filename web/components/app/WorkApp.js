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

class WorkApp extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            components: [],
            users: [],
            workList: [],
            workDetail: [],
            workId: this.props.match.params.id,
            newWorkData: null
        };
        this.retrieveWorkList = this.retrieveWorkList.bind(this);
        this.retrieveWorkDetail = this.retrieveWorkDetail.bind(this);
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
    retrieveWorkDetail(workId) {
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

    componentWillReceiveProps(nextProps) {
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
    onWorkAdded(workId, history) {
        this.retrieveWorkList();
        history.push("/work/" + workId)
    };


    editWork() {
        this.setState({"workEdition": true});
    }
    changeWork(work) {
        console.info("Work Changed", work);
        this.setState({"workEdited": work});
    }
    cancelWorkEdition() {
        this.setState({"workEdition": false});
    }
    updateWork() {
        const thisComponent = this;
        const workId = this.state.workId;
        axios.put('/data/work/' + workId + '/update', this.state.workEdited)
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
        axios.put('/data/work/' + workId + '/archive')
            .then(() => {
                thisComponent.retrieveWorkList();
                thisComponent.retrieveWorkDetail(workId);
            })
            .catch((error) => console.log("Work archive error", error.response));
    }
    unarchiveWork() {
        const thisComponent = this;
        const workId = this.state.workId;
        axios.put('/data/work/' + workId + '/unarchive')
            .then(() => {
                thisComponent.retrieveWorkList();
                thisComponent.retrieveWorkDetail(workId);
            })
            .catch((error) => console.log("Work unarchive error", error.response));
    }

    render()  {
        const workList = this.state.workList || [];
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
                                                        <Secure permission="work.manage">
                                                            <button onClick={() => this.editWork()}>Edit</button>
                                                        </Secure>,
                                                        <Secure permission="work.archive">
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
                                    <div>
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
                            Filtering
                        </Well>

                        <Secure permission="work.add">
                            <Route render={({ history }) => (
                                <InlineAdd type="Work"
                                           action={() => this.addWork()}
                                           onEnterAdd={() => this.clearNewWorkState()}
                                           onItemAdded={(work) => this.onWorkAdded(work, history)}
                                           editionForm={() =>
                                               <WorkEditor work={{}}
                                                           peopleRefs={this.state.users}
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

                        <table width="100%">
                            <tr>
                                <td><b>id</b></td>
                                <td><b>label</b></td>
                                <td><b>type</b></td>
                                <td><b>jira</b></td>
                                <td><b>status</b></td>
                                <td><b>components</b></td>
                                <td><b>people</b></td>
                            </tr>
                            {workList.map(work =>
                                <tr style={{opacity: work.archived ? .5 : 1}}>
                                    <td>
                                        <Link to={"/work/" + work.id}>{work.id}</Link>
                                    </td>
                                    <td>{work.label}</td>
                                    <td>{work.type}</td>
                                    <td>
                                        {work.jiraUrl
                                            ? (<a href={work.jiraUrl} target="_blank">{work.jira}</a>)
                                            : (work.jira)
                                        }
                                    </td>
                                    <td>{work.status}</td>
                                    <td>
                                        {work.components.map(p =>
                                            <span><Link to={"/component/" + p.id}>{p.label}</Link> </span>
                                        )}
                                    </td>
                                    <td>
                                        {work.people.map(p =>
                                            <span><Link to={"/user/" + p.id}>{p.shortName}</Link> </span>
                                        )}
                                    </td>
                                </tr>
                            )}
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
            <div><i>&lt;{work.id}&gt;</i></div>
            <div><i>&lt;{work.type}&gt;</i></div>
            <div><i>&lt;{work.jira}&gt;</i></div>
            <div><i>&lt;{work.status}&gt;</i></div>
            <hr/>
            <div>
                {work.people.map(p =>
                    <span style={{color: 'blue'}}>
                        <Link to={"/user/" + p.id}>{p.shortName} </Link>
                    </span>
                )}
            </div>
            <hr/>
            <div>
                {work.components.map(c =>
                    <span style={{color: 'blue'}}>{c.label} </span>
                )}
            </div>
        </div>
    )
}

export {WorkApp}