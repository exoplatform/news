const path = require('path');
const { merge } = require('webpack-merge');
const webpackCommonConfig = require('./webpack.common.js');

// the display name of the war
const app = 'news';

const config = merge(webpackCommonConfig, {
  mode: 'production',
  output: {
    path: path.resolve(__dirname, `./target/${app}/`)
  }
});

module.exports = config;
