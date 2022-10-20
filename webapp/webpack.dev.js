const path = require('path');
const { merge } = require('webpack-merge');
const webpackCommonConfig = require('./webpack.common.js');

// the display name of the war
const app = 'news';

// add the server path to your server location path

const exoServerPath = "G:/eXo/Feature-projects/Maintenance/platform-6.4.x-maintenance-SNAPSHOT";


let config = merge(webpackCommonConfig, {
  mode: 'development',
  output: {
    path: path.resolve(`${exoServerPath}/webapps/${app}/`)
  },
  devtool: 'eval-source-map'
});

module.exports = config;
