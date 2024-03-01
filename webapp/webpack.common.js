const path = require('path');
const ESLintPlugin = require('eslint-webpack-plugin');
const { VueLoaderPlugin } = require('vue-loader')

let config = {
  context: path.resolve(__dirname, '.'),
  // set the entry point of the application
  // can use multiple entry
  entry: {
    newsTargetSelectorComponent :'./src/main/webapp/components/targetSelector/main.js',
    newsActivityComposer :'./src/main/webapp/news-activity-composer-app/main.js',
    newsDetails :'./src/main/webapp/news-details/main.js',
    latestNews:'./src/main/webapp/latest-news/main.js',
    news :'./src/main/webapp/news/main.js',
    newsSearchCard: './src/main/webapp/news-search/main.js',
    newsDetailsApp: './src/main/webapp/news-details-app/main.js',
    scheduleNewsDrawer: './src/main/webapp/schedule-news-drawer/main.js',
    newsListView: './src/main/webapp/news-list-view/main.js',
    newsPublishTargetsManagement: './src/main/webapp/news-publish-targets-management/main.js',
    newsActivityStreamExtensions: './src/main/webapp/news-extensions/activity-stream-extensions/main.js',
    newsFavoriteDrawerExtensions: './src/main/webapp/news-extensions/favorite-drawer-extensions/main.js',
    newsAnalyticsExtensions: './src/main/webapp/news-extensions/analytics-extensions/main.js',
    newsNotificationExtensions: './src/main/webapp/news-extensions/notification-extensions/main.js',
	engagementCenterExtensions: './src/main/webapp/engagementCenterExtensions/extensions.js',
  },
  output: {
    filename: 'js/[name].bundle.js',
    libraryTarget: 'amd'
  },
  plugins: [
    new ESLintPlugin({
      files: [
        './src/main/webapp/**/*.js',
        './src/main/webapp/**/*.vue',
        './src/main/webapp/**/**/*.js',
      ],
    }),
    new VueLoaderPlugin()
  ],
  module: {
    rules: [
      {
        test: /\.js$/,
        exclude: /node_modules/,
        use: [
          'babel-loader',
        ]
      },
      {
        test: /\.vue$/,
        use: [
          'vue-loader',
        ]
      }
    ]
  }
};

module.exports = config;
