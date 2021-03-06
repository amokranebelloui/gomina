// @flow
import * as React from "react"

import {library} from '@fortawesome/fontawesome-svg-core'
import {faCircle} from '@fortawesome/free-solid-svg-icons'
import {FontAwesomeIcon} from '@fortawesome/react-fontawesome/index.es';
import type {BranchDetailType} from "../component/ComponentType";
import {Badge} from "../common/Badge";

library.add(faCircle);

type Props = {
    branch: string
}

class BranchColor extends React.Component<Props> {
    constructor(props: Props) {
        super(props);
    }
    render() {
        const branch = this.props.branch;
        return (
            <FontAwesomeIcon title={branch} icon="circle" color={color(simplifyName(branch))}/>
        );
    }
}

type BranchLabelProps = {
    branch: BranchDetailType|string,
    selected: boolean
}

class Branch extends React.Component<BranchLabelProps> {
    constructor(props: BranchLabelProps) {
        super(props);
    }
    render() {
        const branch = this.props.branch;
        const branchName = typeof branch === 'string' ? branch : branch.name;
        const name = simplifyName(branchName);
        return (
            <Badge backgroundColor={this.props.selected ? 'lightgray' : null}>
                <BranchColor branch={name}/>
                &nbsp;
                {branch.dismissed ? <s>{name}</s> : <span>{name}</span>}
            </Badge>
        );
    }
}

function isTrunk(branch: ?string) {
    return branch === "trunk" || branch === "master" || branch === "refs/heads/master" || branch === "refs/remotes/origin/master";
}

function simplifyName(branch: string) {
    return branch.replace(/(^refs\/heads\/|^branches\/|^refs\/remotes\/origin\/)/, '');
}

function color(branch: string) {
// String.prototype.branchColor = function() {
    if (isTrunk(branch)) {
        return "#000000"
    }
    var colors = [
        "#e51c23", "#e91e63", "#9c27b0", "#673ab7",
        "#3f51b5", "#5677fc", "#03a9f4", "#00bcd4",
        "#009688", "#259b24", "#8bc34a", "#afb42b",
        "#ff9800", "#ff5722", "#795548", "#607d8b"
    ];

    var hash = 0;
    if (branch.length === 0) return hash;
    for (var i = 0; i < branch.length; i++) {
        hash = branch.charCodeAt(i) + ((hash << 5) - hash);
        hash = hash & hash;
    }
    hash = ((hash % colors.length) + colors.length) % colors.length;
    return colors[hash];
}

function sortBranchesDetails(branches: Array<BranchDetailType>): Array<BranchDetailType> {
    return branches.sort((a, b) => isTrunk(a.name) ? -1 : isTrunk(b.name) ? 1 : a.name > b.name ? 1 : -1)
}

function sortBranches(branches: Array<string>): Array<string> {
    return branches.sort((a, b) => isTrunk(a) ? -1 : a > b ? 1 : -1)
}

export {BranchColor, Branch, isTrunk, sortBranches, sortBranchesDetails}
