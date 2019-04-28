// @flow
import * as React from "react"
import {AppLayout} from "./common/layout";
import axios from "axios/index"
import type {SystemType} from "../system/Systems";
import {Systems} from "../system/Systems";

type Props = {

}

type State = {
    systems: Array<SystemType>
}

class Index extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);
        this.state = {
            systems: []
        }
    }
    retrieveSystems() {
        const thisComponent = this;
        axios.get('/data/systems')
            .then(response => thisComponent.setState({systems: response.data}))
            .catch(error => thisComponent.setState({systems: []}));
    }
    componentDidMount() {
        this.retrieveSystems()
    }
    render() {

        return (
            <AppLayout title='Index'>
                An overview of the different managed systems<br/><br/>
                <Systems systems={this.state.systems} />
            </AppLayout>
        );
    }
}

export { Index }
