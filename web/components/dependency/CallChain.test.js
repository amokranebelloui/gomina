
test('open all', () => {
    function doWith(p1, p2, callback) {
        //console.info("comment:", third);
        callback(p1, p2)
    }

    //function param (p) {console.info("Hello " + p)};
    doWith("test", "something", function (p1, p2) { console.info("Hello " + p1 + " " + p2); });

    //expect(1 + 2).toBe(3);
      /*
    function functionTwo(var1, callback) {
        callback(var1);
    }

    functionTwo(1, function (x) { console.info(x); })
    */
});
