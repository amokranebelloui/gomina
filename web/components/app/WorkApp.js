import axios from "axios/index";
import {AppLayout, PrimarySecondaryLayout} from "./common/layout";
import React from "react";
import {Well} from "../common/Well";
import Link from "react-router-dom/es/Link";
import {Badge} from "../common/Badge";

class WorkApp extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            workList: [],
            workId: this.props.match.params.id,
        };
        this.retrieveWorkList = this.retrieveWorkList.bind(this);
        console.info("workApp !constructor ");
    }

    retrieveWorkList() {
        console.log("workApp Retr Hosts ... ");
        const thisComponent = this;
        axios.get('/data/work')
            .then(response => {
                console.log("workApp data workList", response.data);
                thisComponent.setState({workList: response.data});
            })
            .catch(function (error) {
                console.log("workApp error", error);
                thisComponent.setState({workList: []});
            });
    }

    componentDidMount() {
        console.info("workApp !did mount ");
        this.retrieveWorkList()
    }

    componentWillReceiveProps(nextProps) {
        const newWorkId = nextProps.match.params.id;
        console.info("workApp !did willRecProps ", newWorkId, nextProps);
        this.setState({workId: newWorkId});
        if (this.props.match.params.id != newWorkId && newWorkId) {
            // do something with newWorkId
        }
    }
    
    render()  {
        const workList = this.state.workList || [];
        const workId = this.state.workId;
        const work = workList.find(w => w.id == workId);
        return (
            <AppLayout title="Work List">
                <PrimarySecondaryLayout>
                    <div>
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
                                            <span>{p} </span>
                                        )}
                                    </td>
                                </tr>
                            )}
                        </table>
                    </div>
                    <div>
                        <Well block>
                            <h3>Detail</h3>
                            {work &&
                                <Work key={work.id} work={work}></Work>
                            }
                        </Well>
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
                    <span style={{color: 'blue'}}>{p} </span>
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