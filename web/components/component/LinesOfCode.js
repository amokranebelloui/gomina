import React from "react";

class LinesOfCode extends React.Component {
    render() {
        const base = {padding: '2px', borderRadius: '3px', display: 'block'};
        const high = {width: 70, backgroundColor: 'black', color: 'white'};
        const medium = {width: 47, backgroundColor: 'gray', color: 'white'};
        const low = {width: 23, backgroundColor: 'lightgray', color: 'white'};
        const unknown = {width: 70, backgroundColor: null, color: 'lightgray'};

        const loc = this.props.loc;
        const style = loc > 10000 ? high : loc > 5000 ? medium : loc > 0 ? low : unknown;
        return (
            <span style={{display: 'inline-block', width: 72, backgroundColor: '#F9F9F9', borderRadius: '3px'}}>
                <span style={Object.assign(base, style)}>{loc || '?'}</span>
            </span>
        );
    }
}

export {LinesOfCode};
