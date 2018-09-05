import React from "react"
import {TextLines} from "./TextLines";
import "./DiffView.css"
import * as JsDiff from "diff";

class DiffView extends React.Component {
    constructor(props) {
        super(props)
    }
    render() {
        const diff = JsDiff.diffLines(this.props.left, this.props.right);
        const lines = diffToLines(diff);
        return ([
            <div key="diff" className="diff">
                <div key="left" className="component">
                    <TextLines lines={lines.left} />
                </div>
                <div key="right" className="component">
                    <TextLines lines={lines.right} />
                </div>
            </div>
        ])
    }
}

function diffToLines(diff) {
    let left = [];
    let right = [];
    let leftIdx = 1;
    let rightIdx = 1;
    let leftLine = 1;
    let rightLine = 1;
    function process(l, added, removed) {
        //if (l !== "") {
        if (added === true) {
            right.push({num: rightLine++, text: l, highlight: 'added'});
            rightIdx++
        }
        else if (removed === true) {
            left.push({num: leftLine++, text: l, highlight: 'removed'});
            leftIdx++
        }
        else {
            let count = leftIdx - rightIdx;
            console.info(">>", l, added, removed, leftIdx, rightIdx, count);
            let to = Math.abs(count);
            for (var i = 0; i < to; i++) {
                if (count > 0) {
                    right.push({num: null, text: null}); rightIdx++

                }
                else if (count < 0) {
                    left.push({num: null, text: null}); leftIdx++
                }
            }
            left.push({num: leftLine++, text: l});
            right.push({num: rightLine++, text: l});
        }
        //}

    }
    diff.forEach(d => {
        let endsWithLF = d.value.endsWith('\n');
        let lines = d.value.split('\n');
        if (endsWithLF == true) { lines.pop() }
        lines.forEach(l => {
            console.info(d.added, d.removed, l);
            process(l, d.added, d.removed)
        });
        console.info("end-lf", endsWithLF);

    });
    return {left: left, right: right}
}

export { DiffView }