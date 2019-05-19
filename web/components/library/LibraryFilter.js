// @flow
import React, {Fragment} from "react"
import type {LibraryType} from "./LibraryType";

type Props = {
    search?: ?string,
    onChange?: string => void
}

class LibraryFilter extends React.Component<Props> {
    constructor(props: Props) {
        super(props);
    }
    render() {
        return (
            <Fragment>
                <b>Search: </b>
                <input type="text" name="search"
                       value={this.props.search}
                       onChange={e => this.props.onChange && this.props.onChange(e.target.value)}
                       autoComplete="off"
                />
            </Fragment>
        );
    }
}

function libraryMatchesSearch(library: LibraryType|string, search: ?string) {
    if (typeof library === "string") {
        return library.match(new RegExp(search || "", "i"));
    }
    else {
        return library.artifactId.match(new RegExp(search || "", "i"));
    }
}

function filterLibraries<T: LibraryType|string>(libraries: ?Array<T>, search: ?string): ?Array<T> {
    return libraries && libraries.filter(l => libraryMatchesSearch(l, search))
}


export { LibraryFilter, libraryMatchesSearch, filterLibraries }