import axios from "axios/index";
import {AppLayout, PrimarySecondaryLayout} from "./common/layout";
import React from "react";
import {Well} from "../common/Well";
import Link from "react-router-dom/es/Link";
import {Badge} from "../common/Badge";
import {CommitLog} from "../commitlog/CommitLog";

class WorkApp extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            workList: [],
            workDetail: [],
            workId: this.props.match.params.id,
        };
        this.retrieveWorkList = this.retrieveWorkList.bind(this);
        this.retrieveWorkDetail = this.retrieveWorkDetail.bind(this);
        console.info("workApp !constructor ");
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
        console.info("workApp !did mount ");
        this.retrieveWorkList();
        this.retrieveWorkDetail(this.props.workId);
    }

    componentWillReceiveProps(nextProps) {
        const newWorkId = nextProps.match.params.id;
        console.info("workApp !did willRecProps ", newWorkId, nextProps);
        this.setState({workId: newWorkId});
        if (this.props.match.params.id != newWorkId && newWorkId) {
            this.retrieveWorkDetail(newWorkId)
        }
    }
    
    render()  {
        const workList = this.state.workList || [];
        const workDetail = this.state.workDetail;
        console.info("workDetail", workDetail);
        return (
            <AppLayout title="Work List">
                <PrimarySecondaryLayout>
                    <div>
                        {workDetail && workDetail.work ?
                            <div>
                                {workDetail.work &&
                                    <Work key={workDetail.work.id} work={workDetail.work}></Work>
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
                            </div>
                        }
                    </div>
                    <div>
                        <Well block>
                            Filtering
                        </Well>

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
                                <tr>
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
                                            <span><Link to={"/component/" + p}>{p}</Link> </span>
                                        )}
                                    </td>
                                    <td>
                                        {work.people.map(p =>
                                            <span><Link to={"/user/" + p}>{p}</Link> </span>
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
                    <h3 style={{display: 'inline-block'}}>{work.label}</h3>
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
                        <Link to={"/user/" + p}>{p} </Link>
                    </span>
                )}
            </div>
            <hr/>
            <div>
                {work.components.map(p =>
                    <span style={{color: 'blue'}}>{p} </span>
                )}
            </div>
        </div>
    )
}

export {WorkApp}