import {sortApiSource} from "./ApiSource";

test('Version compare', () => {
    expect(sortApiSource(["auto", "code", "manual", "repo"])).toEqual(["auto", "code", "repo", "manual"]);
    expect(sortApiSource(["auto", "code", "manual", "repo"])).toEqual(["auto", "code", "repo", "manual"]);
    expect(sortApiSource(["manual", "code", "repo", "auto"])).toEqual(["auto", "code", "repo", "manual"]);
    expect(sortApiSource([])).toEqual([]);
    expect(sortApiSource(undefined)).toEqual(undefined);
    expect(sortApiSource(null)).toEqual(null);
});

