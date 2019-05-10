import React from "react";


class Well extends React.Component {
    render() {
        let margin = this.props.margin;
        let padding = this.props.padding || '4px';
        return (
            <div style={{
                padding: padding, margin: margin, display: this.props.block ? null : 'inline-block',
                backgroundColor: '#F8F8F8', border: '1px solid #E3E3E3', borderRadius: '5px'
            }}>
                {this.props.children}
            </div>
        )
    }
}

export {Well}
