import React from "react";
import {AppLayout} from "./common/layout";

class SandboxApp extends React.Component {
    constructor(props) {
        super(props);
    }

    render() {
        return (
            <AppLayout title={'Sandbox'}>
                Sandbox
            </AppLayout>
        );
    }
}

export {SandboxApp};
