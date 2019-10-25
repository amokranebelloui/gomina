// @flow
import React, {Fragment} from "react"
import type {LibraryType} from "./LibraryType";
import {compareVersionsReverse} from "../common/version-utils";
import Link from "react-router-dom/es/Link";

type Props = {
    libraries: Array<LibraryType>,
    totalCount: number
}

class LibraryCatalog extends React.Component<Props> {
    constructor(props: Props) {
        super(props);
    }
    render() {
        const libraries = this.props.libraries || [];
        const count = libraries.length;
        const totalCount = this.props.totalCount;
        const all = count === totalCount;
        const suffix = totalCount > 1 ? 'libraries' : 'library';
        return (
            <div>
                <div>
                    <i style={{color: 'gray'}}>{all ? count + ' ' + suffix : count + ' of ' + totalCount + ' ' + suffix}</i>
                </div>
                {libraries.map(l =>
                    <div key={l.artifactId}>
                        <Link to={'/library/' + l.artifactId}>{l.artifactId}</Link>
                        &nbsp;&nbsp;
                        {l.versions
                            .sort((a,b) => compareVersionsReverse(a.version, b.version))
                            .map(v =>
                                <b key={v.version} style={{textDecoration: v.dismissed ? "line-through": null}}>{v.version}<i style={{color: 'lightgray'}}>({v.dependents})</i> &nbsp;&nbsp;</b>
                            )
                        }
                    </div>
                )}
            </div>
        );
    }
}

type LibraryProps = {
    libraries: Array<string>,
    totalCount: number
}

class Libraries extends React.Component<LibraryProps> {
    constructor(props: LibraryProps) {
        super(props);
    }
    render() {
        const count = this.props.libraries.length;
        const totalCount = this.props.totalCount;
        const all = count === totalCount;
        const suffix = totalCount > 1 ? 'libraries' : 'library';
        return (
            <Fragment>
                <div>
                    <i style={{color: 'gray'}}>{all ? count + ' ' + suffix : count + ' of ' + totalCount + ' ' + suffix}</i>
                </div>
                {this.props.libraries && this.props.libraries.map(library =>
                    <div key={library}>{library}</div>
                )}
            </Fragment>
        );
    }
}

export { Libraries, LibraryCatalog }