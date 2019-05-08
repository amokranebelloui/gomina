// @flow
import * as React from "react"
import {Badge} from "../common/Badge";
import {library} from '@fortawesome/fontawesome-svg-core'
import {faThumbsUp} from '@fortawesome/free-solid-svg-icons'
import {faThumbsDown} from '@fortawesome/free-solid-svg-icons'
import {FontAwesomeIcon} from '@fortawesome/react-fontawesome/index.es';

library.add(faThumbsUp, faThumbsDown);

type Props = {
    session: string,
    connected: boolean
}

class FixStatus extends React.Component<Props> {
    constructor(props: Props) {
        super(props);
    }
    render() {
        return (
            <Badge>
                <span style={{marginRight: '1px'}}>{this.props.session}</span>
                {this.props.connected
                    ? <FontAwesomeIcon icon="thumbs-up" color={'#69876f'}/>
                    : <FontAwesomeIcon icon="thumbs-down" color={'#840513'}/>
                }
            </Badge>
        );
    }
}

export { FixStatus }