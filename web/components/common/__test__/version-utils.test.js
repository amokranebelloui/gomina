import {compareVersions, compareVersionsRevisions} from "../version-utils";

test('Version compare', () => {
    expect(compareVersions("2.2.1", "2.2.2")).toBe(-1);
    expect(compareVersions("2.2.2", "2.2.2")).toBe(0);
    expect(compareVersions("2.2.2", "2.2.1")).toBe(1);
});

test('Version compare with revisions', () => {
    expect(compareVersionsRevisions("2.2.2", "12", "2.2.2", "14")).toBe(-2);
});