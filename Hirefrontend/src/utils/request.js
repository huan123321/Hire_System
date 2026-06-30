import axios from 'axios';
import { Message } from 'element-ui';

const BASE_URL = process.env.VUE_APP_API_BASE_URL || 'http://localhost:8094';

const request = axios.create({
  baseURL: BASE_URL,
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' }
});

request.interceptors.request.use(
  config => {
    const userStr = sessionStorage.getItem('user');
    if (userStr) {
      try {
        const user = JSON.parse(userStr);
        if (user.id) {
          config.headers['X-User-Id'] = user.id;
        }
      } catch (e) {
        // ignore parse error
      }
    }
    return config;
  },
  error => Promise.reject(error)
);

request.interceptors.response.use(
  response => {
    const res = response.data;
    if (res.code && res.code !== 200) {
      Message.error(res.message || '请求失败');
      return Promise.reject(new Error(res.message || '请求失败'));
    }
    return response;
  },
  error => {
    const message = error.response?.data?.message || error.message || '网络错误';
    if (error.response) {
      switch (error.response.status) {
        case 401:
          Message.error('登录已过期，请重新登录');
          sessionStorage.removeItem('user');
          window.location.href = '/';
          break;
        case 403:
          Message.error('无权限访问');
          break;
        case 404:
          Message.error('请求的资源不存在');
          break;
        case 500:
          Message.error('服务器错误');
          break;
        default:
          Message.error(message);
      }
    } else {
      Message.error('网络连接失败，请检查网络');
    }
    return Promise.reject(error);
  }
);

export default request;
