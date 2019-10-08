const webpack = require('webpack');
const path = require('path');
const ExtractTextWebpackPlugin = require('extract-text-webpack-plugin');

let config = {
  context: path.resolve(__dirname, '.'),
  // set the entry point of the application
  // can use multiple entry
  entry: {
    suggesterComponent :'./src/main/webapp/components/suggester/main.js',
    modalComponent :'./src/main/webapp/components/modal/main.js',
    fileDropComponent :'./src/main/webapp/components/fileDrop/main.js',
    newsActivityComposer :'./src/main/webapp/news-activity-composer-app/main.js',
    newsDetails :'./src/main/webapp/news-details/main.js',
    news :'./src/main/webapp/news/main.js'
  },
  module: {
    rules: [
      {
        test: /\.css$/,
        use: ['vue-style-loader', 'css-loader']
      },
      {
        test: /\.less$/,
        use: ExtractTextWebpackPlugin.extract({
          fallback: 'vue-style-loader',
          use: [
            {
              loader: 'css-loader',
              options: {
                sourceMap: true,
                minimize: true
              }
            },
            {
              loader: 'less-loader',
              options: {
                sourceMap: true
              }
            }
          ]
        })
      },
      {
        test: /\.js$/,
        exclude: /node_modules/,
        use: [
          'babel-loader',
          'eslint-loader',
        ]
      },
      {
        test: /\.vue$/,
        use: [
          'vue-loader',
          'eslint-loader',
        ]
      }
    ]
  },
  plugins: [
    // we use ExtractTextWebpackPlugin to extract the css code on a css file
    new ExtractTextWebpackPlugin('css/news.css'),
    new webpack.IgnorePlugin(/SHARED\/.*/)
  ]
};

module.exports = config;