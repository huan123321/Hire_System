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

// BOSS直聘 主题色全局覆盖
const style = document.createElement('style');
style.textContent = `
  .el-button--primary { background-color: #00C2A1 !important; border-color: #00C2A1 !important; }
  .el-button--primary:hover, .el-button--primary:focus { background-color: #00B593 !important; border-color: #00B593 !important; }
  .el-button--primary.is-plain { color: #00C2A1 !important; background: #E6F9F5 !important; border-color: #00C2A1 !important; }
  .el-button--primary.is-plain:hover { background: #00C2A1 !important; border-color: #00C2A1 !important; color: #fff !important; }
  .el-button--text { color: #00C2A1 !important; }
  .el-tabs__item.is-active { color: #00C2A1 !important; }
  .el-tabs__active-bar { background-color: #00C2A1 !important; }
  .el-menu-item.is-active { color: #00C2A1 !important; border-bottom-color: #00C2A1 !important; }
  .el-link { color: #00C2A1 !important; }
  .el-radio__input.is-checked .el-radio__inner { border-color: #00C2A1 !important; background: #00C2A1 !important; }
  .el-checkbox__input.is-checked .el-checkbox__inner { border-color: #00C2A1 !important; background: #00C2A1 !important; }
  .el-checkbox__input.is-checked + .el-checkbox__label { color: #00C2A1 !important; }
  .el-select .el-input.is-focus .el-input__inner { border-color: #00C2A1 !important; }
  .el-pagination.is-background .el-pager li:not(.disabled).active { background-color: #00C2A1 !important; }
  .el-badge__content { background-color: #FF5252 !important; }
`;
document.head.appendChild(style);

// A11y 修复：清除 el-form-item 生成的无效 label for 属性（Element UI 的 for=prop 但控件无对应 id）
const fixLabelFor = () => {
  document.querySelectorAll('label[for]').forEach((label) => {
    const forValue = label.getAttribute('for');
    if (forValue && !document.getElementById(forValue)) {
      label.removeAttribute('for');
    }
  });
};
let labelForTimer = null;
new MutationObserver(() => {
  clearTimeout(labelForTimer);
  labelForTimer = setTimeout(fixLabelFor, 100);
}).observe(document.body, { childList: true, subtree: true });
fixLabelFor();

new Vue({
  render: h => h(App),
  router
}).$mount('#app')
