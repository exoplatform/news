const path = require('path');
const merge = require('webpack-merge');
const webpackCommonConfig = require('./webpack.common.js');

// the display name of the war
const app = 'news';

// add the server path to your server location path

const exoServerPath = "/home/thomas/exoplatform/bundles/plf-enterprise-tomcat-standalone-5.3.0-RC02/platform-5.3.0-RC02";


let config = merge(webpackCommonConfig, {
  output: {
    path: path.resolve(`${exoServerPath}/webapps/${app}/`),
    filename: 'js/[name].bundle.js'
  },
  devtool: 'inline-source-map'
});

module.exports = config;
