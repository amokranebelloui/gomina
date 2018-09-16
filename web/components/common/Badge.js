import React from "react";

import './Badge.css'
import PropTypes from "prop-types";

class Badge extends React.Component {
    constructor(props) {
        super(props);
    }
    onClick(value, e) {
        const multi = e.metaKey || e.ctrlKey;
        this.props.onSelected && this.props.onSelected(value, multi)
    }
    render() {
        const baseStyle = {
            color: this.props.color,
            backgroundColor: this.props.backgroundColor,
            border: this.props.border ? '1px solid ' + this.props.border : ''
        };
        const style = Object.assign({}, baseStyle, this.props.style);
        return (
            <span title={this.props.title}
                  className='badge'
                  style={style}
                  onClick={e => this.onClick(this.props.value, e)}>
                {this.props.children}
            </span>
        )
    }
}

Badge.propTypes = {
    title: PropTypes.string,
    value: PropTypes.string,
    style: PropTypes.object,
    color: PropTypes.string,
    backgroundColor: PropTypes.string,
    border: PropTypes.string,
    children: PropTypes.any.isRequired,
    onSelected: PropTypes.func
};

export {Badge}
