import React from "react";
import './AnnotatedText.css'

function toHtml(text, format) {
    switch (format) {
        case "properties": return propertiesToHtml(text);
        default: return text
    }
}

function propertiesToHtml(text) {
    return text.split('\n').map(line => {
        if (line.match(/^\s*#.*/)) {
            return "<span class='comment'>" + line + "</span><br/>"
        }
        else if (line.match(/.+=.*/) ) {
            let i = line.indexOf('=');
            return "<span class='key'>" + line.substring(0, i) + "</span>=<span class='value'>" + line.substring(i+1) + "</span><br/>"
        }
        else {
            return line + "<br/>"
        }
    }).join("");
}

class AnnotatedText extends React.Component {
    constructor(props) {
        super(props);
        //console.info("txt !constructor", props.text);
    }
    componentWillMount() {
        //console.info("txt !willMount");
    }
    componentDidMount() {
        //console.info("txt !didMount");
        this.content.innerHTML = toHtml(this.props.text, this.props.format);
    }
    componentWillUnmount() {
        //console.info("txt !willUnmount");
    }
    componentWillReceiveProps(nextProps) {
        //console.info("txt !willReceiveProps");
        this.content.innerHTML = toHtml(nextProps.text, nextProps.format);
    }
    componentDidUpdate() {
        //console.info("txt !didUpdate ");
    }
    render() {
        return (
            <div className="annotated-text">
                <b>Text</b>
                <pre>
                <div ref={(ref) => { this.content = ref; }}></div>
                </pre>
            </div>
        )
    }
}

export { AnnotatedText }