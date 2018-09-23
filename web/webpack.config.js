var path = require("path");
var webpack = require('webpack');
var ExtractTextPlugin = require('extract-text-webpack-plugin'); // fetch the ExtractTextPlugin used to bundle the css in a single file
var HtmlWebpaclPlugin = require('html-webpack-plugin'); //fetch the html webpack plugin
var CleanWebpackPlugin = require('clean-webpack-plugin'); //fetch clean plugin to remove the dist folder before a new build

var extractPlugin = new ExtractTextPlugin({ //create an instance of the extract text plugin and name the output css file
   filename: 'main.css'
});

var log = function(s) {
    console.log(s + '\n+------------------------------------+')
};


module.exports = {
    entry: './src/js/app.js',
    output: {
        path: path.join(__dirname + '/dist'),
        filename: 'bundle.js'
    },
    module: {
        rules: [
            {
                test: /\.js$/, //test all js files to build the bundle
                exclude: /node_modules/,
                use: [
                    {
                        loader: 'babel-loader', //use babel to transpile es6 code to es5 for browser compatibility
                        options: {
                            presets: ['es2015']
                        }
                    }
                ]
            },
            {
                test: /\.scss$/, //test all scss files to build in a single one
                use: extractPlugin.extract({
                    use: ['css-loader', 'sass-loader']
                })
            },
            {
                test: /\.html$/, //test all html files
                use: ['html-loader']
            },
            {
                test: /\.(jpg|png)$/, //test all images files
                use: [
                    {
                        loader: 'file-loader',
                        options: {
                            name: '[name].[ext]', //keep the same name of the file
                            outputPath: 'img/' //Copy the images in a subfolder
                        }
                    }
                ]
            },
            {
                test: /\.hbs$/,
                loader: "handlebars-loader"
            },
            {
                test: /\.json$/,
                loader: 'json-loader'
            }
        ]
    },
    // Allow debugging in browser
    devtool:'inline-source-map',

    plugins: [
        new webpack.ProvidePlugin({
            $: 'jquery',
            jQuery: 'jquery'
        }),
        extractPlugin,
        new HtmlWebpaclPlugin({ //initiate the html plugin
            template: 'src/index.html' //use the index html as template
        }),
        new CleanWebpackPlugin (['dist'])
    ],
    devServer: {
        contentBase: path.join(__dirname, "src"),
      }
};