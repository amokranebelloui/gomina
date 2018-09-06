import React from "react";


class Badge extends React.Component {
    constructor(props) {
        super(props);
    }

    render() {
        return (
            <span title={this.props.title}
                  style={{
                      padding: "1px", fontSize: '10px',
                      color: this.props.color, backgroundColor: this.props.backgroundColor,
                      border: this.props.border ? '1px solid ' + this.props.border : '',
                      borderRadius: "5px", display: "inline-block"
                  }}>
                    {this.props.children}
                </span>
        )
    }
}

export {Badge}
