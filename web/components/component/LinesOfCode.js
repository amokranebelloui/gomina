import React from "react";

class LinesOfCode extends React.Component {
    render() {
        const base = {padding: '2px', borderRadius: '3px', display: 'block'};
        const high = {width: 110, backgroundColor: 'black', color: 'white'};
        const medium = {width: 80, backgroundColor: 'gray', color: 'white'};
        const low = {width: 50, backgroundColor: 'lightgray', color: 'white'};
        const unknown = {width: 110, backgroundColor: '#FFEEEE', color: 'gray'};

        const loc = this.props.loc;
        const style = loc > 10000 ? high : loc > 5000 ? medium : loc > 0 ? low : unknown;
        return (
            <span style={{display: 'inline-block', width: 114, backgroundColor: '#E8E8E8', borderRadius: '3px'}}>
                    <span style={Object.assign(base, style)}>{loc || 'Unknown'}</span>
                </span>
        );
    }
}

export {LinesOfCode};
