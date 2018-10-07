import {extendSystem, extendSystems} from "./system-utils";

test("Extend System", () => {
    expect(extendSystem('sample')).toEqual(['sample']);
    expect(extendSystem('sample.cart')).toEqual(['sample', 'sample.cart'])
});

test("Extend Systems", () => {
    expect(extendSystems(['sample', 'sample.cart'])).toEqual(['sample', 'sample', 'sample.cart']);
});
