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
    addUsage() {
        console.info("Add Usage");
        axios.post('/data/dependencies/usage/' + 'torkjell-dispatch' + '/add', {name: 'dispatch-ride', type: 'command', usage: 'async'})
    }

    render() {
        return (
            <AppLayout title={'Sandbox'}>
                <h3>Sandbox</h3>

                <button onClick={() => this.addApi()}>Add API</button>
                <br/>

                <button onClick={() => this.addUsage()}>Add Usage</button>
                <br/>
                
                
            </AppLayout>
        );
    }
}

export {SandboxApp};
