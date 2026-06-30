
module.exports = {
  devServer: {
    port: 8080,
    proxy: {
      '/user': {
        target: 'http://localhost:8094',
        changeOrigin: true,
        pathRewrite: {
          '^/user': '/user'
        }
      },
      '/job': {
        target: 'http://localhost:8094',
        changeOrigin: true,
        pathRewrite: {
          '^/job': '/job'
        }
      },
      '/company': {
        target: 'http://localhost:8094',
        changeOrigin: true,
        pathRewrite: {
          '^/company': '/company'
        }
      }
    }
  }
};