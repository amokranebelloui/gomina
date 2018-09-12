import {uniq, uniqCount} from "./utils";

test("uniq", () => {
    const arr = ['a', 'b', 'a', 'a', 'c', 'd', 'b'];
    expect(uniq(arr).length).toBe(4)
});

test("uniq with count", () => {
    const arr = ['a', 'b', 'a', 'a', 'c', 'd', 'b'];
    let uc = uniqCount(arr);
    expect(uc.length).toBe(4);
    expect(uc).toContainEqual({"count": 3, "value": "a"})
});