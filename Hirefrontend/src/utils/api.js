/**
 * API 统一管理
 */

// 基础 URL（根据环境配置）
const BASE_URL = process.env.VUE_APP_API_BASE_URL || 'http://localhost:8094';

// API 接口集合
export const api = {
  // 用户相关
  user: {
    login: `${BASE_URL}/user/login`,
    register: `${BASE_URL}/user/`,
    getInfo: (id) => `${BASE_URL}/user/current?id=${id}`,
    update: (id) => `${BASE_URL}/user/${id}`,
    getAll: `${BASE_URL}/user/`
  },
  
  // 职位相关
  job: {
    list: `${BASE_URL}/job/list`,
    detail: (id) => `${BASE_URL}/job/${id}`,
    create: `${BASE_URL}/job`,
    update: (id) => `${BASE_URL}/job/${id}`,
    delete: (id) => `${BASE_URL}/job/${id}`,
    filter: `${BASE_URL}/job/filter`,
    companyJobs: (companyName) => `${BASE_URL}/job/company/${companyName}`,
    apply: `${BASE_URL}/job/apply`,
    applications: (userId) => `${BASE_URL}/job-application/user/${userId}`
  },
  
  // 公司相关
  company: {
    list: `${BASE_URL}/company/list`,
    detail: (id) => `${BASE_URL}/company/detail/${id}`,
    create: `${BASE_URL}/company/add`,
    update: (id) => `${BASE_URL}/company/update/${id}`,
    delete: (id) => `${BASE_URL}/company/delete/${id}`,
    filter: `${BASE_URL}/company/filter`
  },
  
  // 简历相关
  resume: {
    save: `${BASE_URL}/resume/save`,
    list: (userId) => `${BASE_URL}/resume/list/${userId}`,
    default: (userId) => `${BASE_URL}/resume/default/${userId}`,
    setDefault: `${BASE_URL}/resume/set-default`,
    delete: (id) => `${BASE_URL}/resume/${id}`
  }
};

/**
 * 统一的 API 请求方法
 * @param {string} url - 请求地址
 * @param {string} method - 请求方法 GET/POST/PUT/DELETE
 * @param {object} data - 请求数据
 * @param {object} config - 其他配置
 */
export const request = async (url, method = 'GET', data = null, config = {}) => {
  try {
    const options = {
      method,
      url,
      ...config
    };

    if (data && (method === 'POST' || method === 'PUT')) {
      options.data = data;
    }

    const response = await window.axios(options);
    
    // 统一处理响应
    if (response.data) {
      return response.data;
    }
    
    return response;
  } catch (error) {
    console.error('API Request Error:', error);
    
    // 统一错误处理
    if (error.response) {
      // 服务器返回错误状态码
      switch (error.response.status) {
        case 401:
          console.error('未授权，请重新登录');
          // 可以在这里跳转到登录页
          break;
        case 403:
          console.error('拒绝访问');
          break;
        case 404:
          console.error('请求资源不存在');
          break;
        case 500:
          console.error('服务器错误');
          break;
        default:
          console.error(`请求失败：${error.response.status}`);
      }
    } else if (error.request) {
      // 请求已发送但没有收到响应
      console.error('网络错误，请检查网络连接');
    } else {
      // 设置请求时发生了错误
      console.error('请求错误:', error.message);
    }
    
    throw error;
  }
};

/**
 * 快捷请求方法
 */
export const http = {
  get: (url, config) => request(url, 'GET', null, config),
  post: (url, data, config) => request(url, 'POST', data, config),
  put: (url, data, config) => request(url, 'PUT', data, config),
  delete: (url, config) => request(url, 'DELETE', null, config)
};

export default {
  api,
  request,
  http
};
