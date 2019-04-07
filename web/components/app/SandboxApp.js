import React from "react";
import {AppLayout} from "./common/layout";
import axios from "axios/index";

class SandboxApp extends React.Component {
    constructor(props) {
        super(props);
    }

    addApi() {
        console.info("Add Api");
        axios.post('/data/dependencies/api/' + 'torkjell' + '/add', {name: 'dispatch-ride', type: 'command'})
    }
    removeApi() {
        console.info("Remove Api");
        axios.delete('/data/dependencies/api/' + 'torkjell' + '/remove', { data: {name: 'dispatch-ride', type: 'command'} })
    }
    addUsage() {
        console.info("Add Usage");
        axios.post('/data/dependencies/usage/' + 'torkjell-dispatch' + '/add', {name: 'dispatch-ride', type: 'command', usage: 'async'})
    }
    removeUsage() {
        console.info("Remove Usage");
        axios.delete('/data/dependencies/usage/' + 'torkjell-dispatch' + '/remove', { data: {name: 'dispatch-ride', type: 'command'} })
    }

    render() {
        return (
            <AppLayout title={'Sandbox'}>
                <h3>Sandbox</h3>

                <button onClick={() => this.addApi()}>Add API</button>
                <br/>
                <button onClick={() => this.removeApi()}>Remove API</button>
                <br/>

                <button onClick={() => this.addUsage()}>Add Usage</button>
                <br/>
                <button onClick={() => this.removeUsage()}>Remove Usage</button>
                <br/>

                <hr/>

                <button onClick={() => {
                    console.info("Add Work");
                    axios.post('/data/work/add', {
                        label: null,
                        //label: "Event based dispatching",
                        type: "Dev",
                        jira: "",
                        people: ["amokrane.belloui"],
                        components: ["torkjell", 'torkjell-dispatch'],
                    })
                }}>
                Add Work</button>
                <br/>

                <button onClick={() => {
                    console.info("Update Work");
                    axios.put('/data/work/' + '1' + '/update', {
                        label: "Location services",
                        type: "Dev",
                        jira: "",
                        people: ["amokrane.belloui"],
                        components: ["torkjell"],
                    })

                }}>Update Work</button>
                <br/>

                <button onClick={() => {
                    console.info("Delete Work");
                    axios.delete('/data/work/' + '1' + '/archive')
                }}>Archive Work</button>
                <br/>

            </AppLayout>
        );
    }
}

export {SandboxApp};
