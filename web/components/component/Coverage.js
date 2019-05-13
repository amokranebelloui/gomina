import React from "react";

class Coverage extends React.Component {
    render() {
        const base = {padding: '2px', borderRadius: '3px', display: 'block'};
        const high = {width: 70, backgroundColor: 'green', color: 'white'};
        const medium = {width: 47, backgroundColor: 'orange', color: 'white'};
        const low = {width: 25, backgroundColor: 'red', color: 'white'};
        const unknown = {width: 35, backgroundColor: null, color: 'lightgray'};

        const coverage = this.props.coverage;
        const style = coverage > 20 ? high : coverage > 8 ? medium : coverage > 0 ? low : unknown;
        return (
            <span style={{display: 'inline-block', width: 72, backgroundColor: '#F9F9F9', borderRadius: '3px'}}>
                <span style={{float: 'right', color: '#BBBBBB', fontSize: 7}}>
                    (22/24)
                </span>
                <span style={Object.assign(base, style)}>
                    {coverage ? coverage + ' %' : '?'}
                </span>
            </span>
        );
    }
}

export {Coverage}
