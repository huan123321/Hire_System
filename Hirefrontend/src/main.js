import Vue from 'vue'
import App from './App.vue'
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';
import request from './utils/request';
import router from './router'

Vue.config.productionTip = false
Vue.use(ElementUI);
Vue.prototype.$http = request;
Vue.prototype.$axios = request;
Vue.prototype.$apiBaseUrl = process.env.VUE_APP_API_BASE_URL || 'http://localhost:8094'

new Vue({
  render: h => h(App),
  router
}).$mount('#app')
