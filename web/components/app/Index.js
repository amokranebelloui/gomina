// @flow
import React, {Fragment} from "react"
import axios from "axios/index"
import type {SystemType} from "../system/Systems";
import {Systems} from "../system/Systems";
import {ApplicationLayout} from "./common/ApplicationLayout";

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
        document.title = 'Gomina';
        this.retrieveSystems()
    }
    render() {

        return (
            <ApplicationLayout title='Index'
                   main={() =>
                       <Fragment>
                           An overview of the different managed systems<br/><br/>
                           <Systems systems={this.state.systems} />
                       </Fragment>
                   }
            />
        );
    }
}

export { Index }
