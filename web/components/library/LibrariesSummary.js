// @flow
import * as React from "react"
import type {LibraryType} from "./LibraryType";
import {LibraryCatalog} from "./Libraries";

type Props = {
    libraries: ?Array<LibraryType>
}

function withDependents(libraries: Array<LibraryType>) {
    return libraries.filter(l => l.dependents > 0)
}

function dependents(library: LibraryType) {
    return library.versions.map(v => v.dependents).reduce((a, b) => a + b, 0)
}

class LibrariesSummary extends React.Component<Props> {
    constructor(props: Props) {
        super(props);
    }
    render() {
        const libraries = this.props.libraries || [];
        const nbVersionsPerLibrary:{[number]: number} = {};
        libraries.forEach(l => {
            const nbVersions = withDependents(l.versions).length;
            nbVersionsPerLibrary[nbVersions] = (nbVersionsPerLibrary[nbVersions]||0) + 1
        });
        //$FlowFixMe
        const nbVersions: Array<number> = Object.keys(nbVersionsPerLibrary).sort((a, b) => b > a ? 1 : -1);
        //$FlowFixMe
        const fragmentationThreshold: number = nbVersions.length > 1 ? nbVersions[1] : nbVersions.length > 0 ? nbVersions[0] : 999;
        const mostFragmented = libraries
            .filter(l => withDependents(l.versions).length >= fragmentationThreshold)
            .sort((l1, l2) => withDependents(l2.versions).length - withDependents(l1.versions).length || l1.artifactId - l2.artifactId );

        const mostUsed = libraries.sort((l1,l2) => {
            const c1 = dependents(l1);
            const c2 = dependents(l2);
            return (c2 === c1 ? 0 : c2 > c1 ? 1 : -1) || (l1.artifactId > l2.artifactId ? 1 : -1);
        }).slice(0, 15);

        return (
            <div>
                <div>
                    <b>{libraries.length} {libraries.length > 1 ? 'libraries' : 'library' }</b>
                </div>
                <br/>
                <div>
                    <b>Fragmentation</b>
                    <ul>
                        {nbVersions.map(nb =>
                            <li key={nb}>{nb} version(s), {nbVersionsPerLibrary[nb]} component(s)</li>
                        )}
                    </ul>
                </div>
                <br/>
                <div>
                    <b>Most Fragmented</b>
                    <ul>
                        <LibraryCatalog libraries={mostFragmented} totalCount={mostFragmented.length}/>
                    </ul>
                </div>
                <br/>
                <div>
                    <b>Most Used</b>
                    <ul>
                        <LibraryCatalog libraries={mostUsed} totalCount={mostUsed.length}/>
                    </ul>
                </div>
            </div>
        );
    }
}

export { LibrariesSummary }