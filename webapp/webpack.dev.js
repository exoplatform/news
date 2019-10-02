const path = require('path');
const merge = require('webpack-merge');
const webpackCommonConfig = require('./webpack.common.js');

// the display name of the war
const app = 'news';

// add the server path to your server location path
const exoServerPath = "/home/exo/integration_19_08_2019/plf_news_last/plfent-5.3.x-news-20190830.200450-286/platform-5.3.x-news-SNAPSHOT";

let config = merge(webpackCommonConfig, {
  output: {
    path: path.resolve(`${exoServerPath}/webapps/${app}/`),
    filename: 'js/[name].bundle.js'
  },
  devtool: 'inline-source-map'
});

module.exports = config;
