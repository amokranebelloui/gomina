
var path = require('path');
module.exports = {
    /*
    resolve: {
        extensions: ["", ".js"],
        moduleDirectories: ["web"]
    },
    */
    entry: "./web/components/app.js",
    output: {
        path: path.resolve(__dirname, 'web/dist'),
        filename: 'bundle.js'
    },
    module: {
        loaders: [
            {
                test: /\.(js|jsx)$/,
                exclude: /node_modules/,
                loader: 'babel-loader',
                query: {
                    presets: ['react', 'es2015']
                }
            },
            {
                test: /\.css$/,
                use: ['style-loader', 'css-loader']
            }
        ]
    }
};
