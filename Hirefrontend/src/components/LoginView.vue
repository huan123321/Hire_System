<template>

  <div class="wrapper">
    <div id="building">


  <div class="loginBody">

    <div class="loginDiv">

      <div class="login-content">

        <h1 class="login-title" style="color:#00C2A1">欢迎来到招聘系统! 请登录</h1>

        <div class="demo-image">

        <el-form :model="loginForm" label-width="100px"
                 :rules="rules" ref="loginForm">


          <el-form-item label="用户名" prop="username">
            <el-input style="width: 200px" type="text" v-model="loginForm.username"
                      autocomplete="off" size="small"></el-input>
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input style="width: 200px" type="password" v-model="loginForm.password"
                      show-password autocomplete="off" size="small"></el-input>
          </el-form-item>
          <el-form-item label="验证码" prop="captchaCode">
            <div style="display: flex; align-items: center; gap: 10px;">
              <el-input 
                style="width: 120px" 
                type="text" 
                v-model="loginForm.captchaCode"
                placeholder="请输入验证码"
                autocomplete="off" 
                size="small"
                @keyup.enter.native="login">
              </el-input>
              <img 
                :src="captchaImageUrl" 
                alt="验证码" 
                style="width: 100px; height: 40px; cursor: pointer; border: 1px solid #dcdfe6; border-radius: 4px;"
                @click="refreshCaptcha"
                @error="onCaptchaError"
                title="点击刷新验证码">
            </div>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="login" style="margin-left: 0px">登 录</el-button>
            <el-button type="success" @click="register" style="margin-left: 50px" >注 册</el-button>
            <el-row>
            </el-row>
          </el-form-item>

        </el-form>
        </div>
      </div>
    </div>
    </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "LoginView",
  data(){
    return{
      form: {
        name: '',
        region: '',
        date1: '',
        date2: '',
        delivery: false,
        type: [],
        resource: '',
        desc: ''
      },
      loginForm:{
        username:'',
        password:'',
        captchaCode: '', // 验证码
        role: 0 // 默认选择求职者角色

      },
      sessionId: '', // 会话 ID
      captchaImageUrl: '', // 验证码图片 URL
      retryCount: 0, // 重试次数
      rules:{
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 3, max: 15, message: '用户名长度在 3 到 15 个字符', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 3, max: 15, message: '密码长度在 3 到 15 个字符', trigger: 'blur' }
        ],
        captchaCode: [
          { required: true, message: '请输入验证码', trigger: 'blur' },
          { min: 4, max: 4, message: '验证码为 4 位字符', trigger: 'blur' }
        ],
        role: [
          { required: true, message: '请选择角色', trigger: 'change' }
        ],
      }

    }
  },
  mounted() {
    // 页面加载时生成验证码
    this.generateSessionId();
    this.refreshCaptcha();
  },
  methods: {
    // 生成会话 ID
    generateSessionId() {
      this.sessionId = 'login_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
    },
    
    // 刷新验证码
    refreshCaptcha() {
      if (!this.sessionId) {
        this.generateSessionId();
      }
      // 直接使用 URL 方式加载图片（更简单可靠）
      this.captchaImageUrl = this.$apiBaseUrl + '/captcha/image?sessionId=' + this.sessionId + '&t=' + Date.now();
      console.log('验证码 URL:', this.captchaImageUrl);
      console.log('Session ID:', this.sessionId);
    },
    
    // 验证码图片加载失败时的处理
    onCaptchaError() {
      console.error('验证码图片加载失败');
      // 防止无限重试，只重试一次
      if (!this.retryCount) {
        this.retryCount = 0;
      }
      if (this.retryCount < 1) {
        this.retryCount++;
        this.$message.warning('验证码加载失败，正在重试...');
        // 1 秒后重试一次
        setTimeout(() => {
          this.refreshCaptcha();
        }, 1000);
      } else {
        this.$message.error('验证码加载失败，请检查后端服务是否启动 (端口 8094)');
      }
    },
    
    register() {
      this.$router.push('/RegisTer')
    },
    login() {
      this.$refs.loginForm.validate((valid) => {
        if (valid) {
          //valid 成功为 true，失败为 false
          this.loading = true;
              
          // 构建登录请求参数
          const loginData = {
            username: this.loginForm.username,
            password: this.loginForm.password,
            role: this.loginForm.role
          };
              
          // 发送登录请求，携带验证码参数
          this.$axios.post(
            this.$apiBaseUrl + '/user/login?captchaCode=' + encodeURIComponent(this.loginForm.captchaCode) + 
            '&sessionId=' + encodeURIComponent(this.sessionId),
            loginData
          ).then(res=>res.data).then(res=>{
            this.loading = false;
            if(res.code === 200){ //
              // 后端返回结构：res.data = { user, token }
              const loginData = res.data || {};
              const user = loginData.user;
              // 存储用户对象（供路由守卫/页面读取）
              sessionStorage.setItem('user', JSON.stringify(user));
              // 存储登录 token（后续请求由 request.js 自动附加 Authorization 头）
              if (loginData.token) {
                sessionStorage.setItem('token', loginData.token);
              }
              // 根据后端返回的角色信息进行跳转
              if (res.redirect) {
                this.$router.push(res.redirect);
              } else {
                // 默认跳转到主页

                  if (user && user.role === 1) {  // HR角色
                    this.$router.push('/HrHomePage')
                  } else {
                    this.$router.push('/HomePage')
                  }
                }

              this.$message.success('登录成功');
            }else{
              // 如果验证码错误，刷新验证码
              if (res.message && res.message.includes('验证码')) {
                this.refreshCaptcha();
                this.loginForm.captchaCode = ''; // 清空验证码输入框
              }
              this.$message.error(res.message || '登录失败，用户或密码错误！'); // 修改错误信息
              return false;
            }
          }).catch(error => {
            this.loading = false;
            console.error('Login error:', error);
            this.$message.error('登录请求失败：' + (error.response?.data?.message || error.message || '网络错误'));
          })
    
        }
      });
    
    },



}
}
</script>

<style scoped >
.wrapper {
  /* 真实写字楼照片背景 + 深色遮罩（保证白色登录卡片可读性） */
  background:
    linear-gradient(rgba(0, 0, 0, 0.30), rgba(0, 0, 0, 0.42)),
    url("../assets/sean-pollock-PhYq704ffdA-unsplash.jpg") center / cover no-repeat;
  width: 100%;
  height: 100%;
  position: fixed;
  top: 0;
  left: 0;
  overflow: hidden;
}

#building {
  /*设置透明度，0为完全透明，1为不透明*/
  opacity: 1;
}

.loginBody {
  width: 100%;
  height: 100%;
  background-color: transparent;
}
.loginDiv {
  position: absolute;
  top: 50%;
  left: 50%;
  margin-top: -200px;
  margin-left: -250px;
  width: 460px;
  height: 360px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 24px 64px rgba(0, 0, 0, 0.22);
}
.login-title {

  margin: 20px ;
  text-align: center;
}
.login-content {
  width: 450px;
  height: 250px;
  position: absolute;
  top: 25px;
  left: 25px;
}
</style>