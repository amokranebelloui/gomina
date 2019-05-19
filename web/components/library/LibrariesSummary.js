// @flow
import * as React from "react"
import type {LibraryType} from "./LibraryType";
import {LibraryCatalog} from "./Libraries";

type Props = {
    libraries: ?Array<LibraryType>
}

class LibrariesSummary extends React.Component<Props> {
    constructor(props: Props) {
        super(props);
    }
    render() {
        const libraries = this.props.libraries || [];
        const nbVersionsPerLibrary = {};
        libraries.forEach(l => {
            const nbVersions = l.versions.length;
            nbVersionsPerLibrary[nbVersions] = (nbVersionsPerLibrary[nbVersions]||0) + 1
        });
        const nbVersions = Object.keys(nbVersionsPerLibrary).sort((a, b) => b > a ? 1 : -1);
        const fragmentationThreshold = nbVersions.length > 1 ? nbVersions[1] : nbVersions.length > 0 ? nbVersions[0] : 999;
        const mostFragmented = libraries
            .filter(l => l.versions.length >= fragmentationThreshold)
            .sort((l1, l2) => l2.versions.length - l1.versions.length || l1.artifactId - l2.artifactId );
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
                            <li>{nb} version(s), {nbVersionsPerLibrary[nb]} component(s)</li>
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
            </div>
        );
    }
}

export { LibrariesSummary }