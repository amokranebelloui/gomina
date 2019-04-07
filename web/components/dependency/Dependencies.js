import React from 'react'
import PropTypes from 'prop-types'
import {Badge} from "../common/Badge";


class Dependencies extends React.Component {
    constructor(props) {
        super(props)
    }
    render() {
        const deps = this.props.dependencies || [];
        return (
            <div>
                {deps.map(sd =>
                    <ul>
                        <li><b>{sd.from} -> {sd.to}</b>
                            <ul>
                                {sd.functions.map(f =>
                                    <li>
                                        &nbsp;&nbsp;
                                        {f.name} <span style={{color: 'gray'}}>{f.type} {f.usage || ""}</span>
                                        &nbsp;
                                        {(f.sources||[]).map(s =>
                                            <Badge backgroundColor={s === 'manual' ? "#f2df76" : "#CCCCCC"}
                                                   style={{marginLeft: '1px', marginRight: '1px'}}>
                                                {s}
                                            </Badge>
                                        )}
                                    </li>
                                )}
                            </ul>
                        </li>
                    </ul>
                )}
            </div>
        )
    }
}

Dependencies.propTypes = {
    dependencies: PropTypes.object
};

export { Dependencies }
