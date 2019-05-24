// @flow
import * as React from "react"
import {LinesOfCode} from "../component/LinesOfCode";
import {Coverage} from "../component/Coverage";


type SystemType = {
    id: string,
    components: number,
    loc?: ?number,
    locBasis?: number,
    coverage?: ?number,
    coverageBasis?: number,
}

type Props = {
    systems?: ?Array<SystemType>
}

class Systems extends React.Component<Props> {
    constructor(props: Props) {
        super(props);
    }
    render() {
        const systems = this.props.systems || [];
        systems.sort((s1, s2) => s2.id > s1.id ? -1 : 1);
        return (
            <table>
                <tbody>
                {systems.map(s =>
                    <tr key={s.id}>
                        <td>{s.id}</td>
                        <td align="center" style={{width: '50px'}}>{s.components}</td>
                        <td style={{width: '150px'}}><LinesOfCode loc={s.loc}/>/{s.locBasis}</td>
                        <td style={{width: '150px'}}><Coverage coverage={s.coverage}/>/{s.coverageBasis}</td>
                    </tr>
                )}
                </tbody>
            </table>
        );
    }
}

export {Systems}
export type {SystemType}