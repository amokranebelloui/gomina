import React from 'react'
import PropTypes from 'prop-types'


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
                                    <li>&nbsp;&nbsp;{f.name} <span style={{color: 'gray'}}>{f.type} {f.usage || ""}</span></li>
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
