import React from "react";
import './TextLines.css'

function propertiesLine(line) {
    if (line) {
        if (line.match(/^\s*#.*/)) {
            return <span className='comment'>{line}</span>
        }
        else if (line.match(/.+=.*/) ) {
            let i = line.indexOf('=');
            return [
                <span key="key" className='key'>{line.substring(0, i)}</span>,
                <span key="equals">=</span>,
                <span key="value" className='value'>{line.substring(i + 1)}</span>
            ]
        }
    }
    return line
}

/**
 * lines: list of line
 *      num: line num <can be empty for alignment>
 *      text: text
 *      highlight: how to highlight the line
 */
class TextLines extends React.Component {
    constructor(props) {
        super(props);
    }
    render() {
        const processLine = propertiesLine;
        return (
            <div className="text-lines">
                <pre>
                    <table width="100%" border="0" style={{borderCollapse: 'collapse'}}>
                        <tbody>
                            {this.props.lines && this.props.lines.map((line, i) =>
                                <TextLine key={i} num={line.num} line={processLine(line.text)} highlight={line.highlight} />
                            )}
                        </tbody>
                    </table>
                </pre>
            </div>
        )
    }
}

function TextLine(props) {
    let clazz = "line";
    if (props.highlight) {
        clazz = clazz + " highlighted-" + props.highlight
    }
    return (
        <tr className={clazz}>
            <td className="line-num">{props.num}</td><td>{props.line || <span>&nbsp;</span>}</td>
        </tr>
    );
}

export { TextLines }