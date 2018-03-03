
var path = require('path');

const webpack = require('webpack');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const CopyWebpackPlugin = require('copy-webpack-plugin');
const CleanWebpackPlugin = require('clean-webpack-plugin');
const BundleAnalyzerPlugin = require('webpack-bundle-analyzer').BundleAnalyzerPlugin;

function isVendor({ resource }) {
    return resource &&
        resource.indexOf('node_modules') >= 0 &&
        resource.match(/\.js$/);
}

module.exports = {
    /*
    resolve: {
        extensions: ["", ".js"],
        moduleDirectories: ["web"]
    },
    */
    //entry: "./web/components/app.js",
    //devtool: 'source-map',
    //devtool: 'eval',
    entry: {
        //components: './web/components/components.js',
        app: './web/components/application.js'
        //react: ['react', 'react-router', 'react-dom']
    },
    plugins: [
        new CleanWebpackPlugin(['dist']),
        new HtmlWebpackPlugin({
            title: 'Gomina!',
            template: 'web/index.template.ejs',
            inject: false,
            filename: 'index.html'
        }),
        new CopyWebpackPlugin([{from: "web/*.ico", to: "", flatten: true}]),
        new webpack.optimize.CommonsChunkPlugin({name: 'shared'}),
        /*
        new webpack.optimize.CommonsChunkPlugin({
            name: 'vendor',
            minChunks: module => isVendor(module)
        }),
        new webpack.optimize.CommonsChunkPlugin({name: 'shared', chunks: ['app', 'components']}),
        */
        /*
        new webpack.optimize.CommonsChunkPlugin({
            name: 'node-static',
            //filename: 'node-static.js',
            minChunks(module, count) {
                var context = module.context;
                return context && context.indexOf('node_modules') >= 0;
            },
        }),
        */
        /*
        new webpack.optimize.CommonsChunkPlugin({name: 'react', chunks: ['react']}),
        new webpack.optimize.CommonsChunkPlugin({
            name: 'vendor',
            chunks: ['app', 'components', 'react'], minChunks: isVendor
        }),
        new webpack.optimize.CommonsChunkPlugin({name: 'shared', chunks: ['app', 'components']}),
        */
        ///new webpack.optimize.CommonsChunkPlugin({name: 'shared', chunks: ['components', 'app']}),
        ///new webpack.optimize.CommonsChunkPlugin({name: 'vendor2', chunks: ['react', 'shared'] }),
        //new webpack.optimize.CommonsChunkPlugin({name: 'vendor', chunks: ['components', 'app', 'vendor2'], minChunks: isVendor}),

        //new webpack.optimize.CommonsChunkPlugin({name: 'vendor', chunks: ['components', 'app']}),
        //new webpack.optimize.CommonsChunkPlugin({name: 'shared'}),

        // PROD Plugins
        /*
        new webpack.DefinePlugin({
            'process.env': {
                NODE_ENV: JSON.stringify('production')
            }
        }),
        new webpack.optimize.UglifyJsPlugin({
            compress: { warnings: false }
        }),
        new BundleAnalyzerPlugin({analyzerMode: 'static'})
        */
    ],
    output: {
        path: path.resolve(__dirname, 'dist'),
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
