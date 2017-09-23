
var path = require('path');

const webpack = require('webpack');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const CleanWebpackPlugin = require('clean-webpack-plugin');

module.exports = {
    /*
    resolve: {
        extensions: ["", ".js"],
        moduleDirectories: ["web"]
    },
    */
    //entry: "./web/components/app.js",
    entry: {
        components: './web/components/components.js',
        app: './web/components/application.js'
    },
    plugins: [
        new CleanWebpackPlugin(['web/dist']),
        new HtmlWebpackPlugin({
            title: 'Application',
            template: 'web/index.template.ejs',
            inject: 'body',
            filename: 'index.html'
        }),
        new webpack.optimize.CommonsChunkPlugin({name: 'shared'})
    ],
    output: {
        path: path.resolve(__dirname, 'web/dist'),
        filename: '[name].bundle.js'
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
