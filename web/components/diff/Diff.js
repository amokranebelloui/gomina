import React from "react"
import {AnnotatedText} from "./AnnotatedText";
import "./Diff.css"

class Diff extends React.Component {
    constructor(props) {
        super(props)
    }
    render() {
        return (
            <div className="diff">
                <div className="component">
                    <AnnotatedText text={this.props.left} format={this.props.format}/>
                </div>
                <div className="component">
                    <AnnotatedText text={this.props.right} format={this.props.format} />
                </div>
            </div>
        )
    }
}

export { Diff }