
import React from 'react';

import './module.css'

class C1 extends React.Component {
    render() {
        return (
            <div className="hello" style={{padding: '2px', margin: this.props.margin, display: this.props.block ? null : 'inline-block',
                backgroundColor: '#F8F8F8', border: '1px solid #E3E3E3', borderRadius: '5px'}}>
                Hello World 6
            </div>
        )
    }
}

export default C1;