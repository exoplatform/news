const webpack = require('webpack');
const path = require('path');

let config = {
  context: path.resolve(__dirname, '.'),
  // set the entry point of the application
  // can use multiple entry
  entry: {
    shareNewsActivityDrawer :'./src/main/webapp/news-share-activity-drawer/main.js',
    snackbarComponent :'./src/main/webapp/components/snackbar/main.js',
    newsActivityComposer :'./src/main/webapp/news-activity-composer-app/main.js',
    newsDetails :'./src/main/webapp/news-details/main.js',
    latestNews:'./src/main/webapp/latest-news/main.js',
    news :'./src/main/webapp/news/main.js'
  },
  output: {
    filename: 'js/[name].bundle.js',
    libraryTarget: 'amd'
  },
  module: {
    rules: [
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
  }
};

module.exports = config;