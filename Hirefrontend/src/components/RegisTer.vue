<template>
  <div class="wrapper">
    <div id="building">
      <div class="loginBody">
        <div class="loginDiv">
          <div class="login-content">
            <el-button type="primary" @click="back" style="margin-left: 0px" >返回</el-button>
            <h1 class="login-title">注册</h1>

            <div class="demo-image">
              <el-form :model="loginForm" label-width="100px"
                       :rules="rules" ref="loginForm">
                <el-form-item label="用户名" prop="username">
                  <el-input style="width: 250px" type="text" v-model="loginForm.username"
                            autocomplete="off" size="small" @keyup.enter="register"></el-input>
                </el-form-item>
                <el-form-item label="密码" prop="password">
                  <el-input style="width: 250px" type="password" v-model="loginForm.password"
                            show-password autocomplete="off" size="small" @keyup.enter="register"></el-input>
                </el-form-item>
                <el-form-item label="确认密码" prop="repassword">
                  <el-input style="width: 250px" type="password" v-model="loginForm.repassword"
                            show-password autocomplete="off" size="small" @keyup.enter="register"></el-input>
                </el-form-item>
                
                <!-- 验证码 -->
                <el-form-item label="验证码" prop="captchaCode">
                  <div style="display: flex; align-items: center; gap: 10px;">
                    <el-input 
                      style="width: 150px" 
                      type="text" 
                      v-model="loginForm.captchaCode"
                      placeholder="请输入验证码"
                      autocomplete="off" 
                      size="small"
                      @keyup.enter.native="register">
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
                
                <!-- 添加角色选择 -->
                <el-form-item label="角色" prop="role">
                  <el-select v-model="loginForm.role" placeholder="请选择角色" style="width: 250px" @change="handleRoleChange">
                    <el-option label="求职者" :value="0"></el-option>
                    <el-option label="HR" :value="1"></el-option>
                  </el-select>
                </el-form-item>

                <!-- HR 企业认证表单 -->
                <div v-if="showCertification" class="certification-section">
                  <el-divider>企业认证信息</el-divider>
                  
                  <el-form-item label="企业名称" prop="companyName">
                    <el-input style="width: 250px" type="text" v-model="loginForm.companyName"
                              autocomplete="off" size="small" placeholder="请输入企业全称"></el-input>
                  </el-form-item>
                  
                  <el-form-item label="统一社会信用代码" prop="creditCode">
                    <el-input style="width: 250px" type="text" v-model="loginForm.creditCode"
                              autocomplete="off" size="small" placeholder="请输入 18 位统一社会信用代码"></el-input>
                  </el-form-item>
                  
                  <el-form-item label="法人代表" prop="legalPerson">
                    <el-input style="width: 250px" type="text" v-model="loginForm.legalPerson"
                              autocomplete="off" size="small" placeholder="请输入法人姓名"></el-input>
                  </el-form-item>
                  
                  <el-form-item label="注册地址" prop="registerAddress">
                    <el-input style="width: 250px" type="text" v-model="loginForm.registerAddress"
                              autocomplete="off" size="small" placeholder="请输入注册地址"></el-input>
                  </el-form-item>
                  
                  <el-form-item label="联系电话" prop="contactPhone">
                    <el-input style="width: 250px" type="text" v-model="loginForm.contactPhone"
                              autocomplete="off" size="small" placeholder="请输入联系电话"></el-input>
                  </el-form-item>
                </div>

                <el-form-item>
                  <el-button type="success" @click="register" style="margin-left: 80px" :loading="loading">注 册</el-button>
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
  name: "RegisterView",
  data(){
    // 确认密码验证规则
    var validatePass2 = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请再次输入密码'));
      } else if (value !== this.loginForm.password) {
        callback(new Error('两次输入密码不一致!'));
      } else {
        callback();
      }
    };
    
    return{
      loading: false,
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
        repassword:'',
        role: 0, // 默认选择求职者
        companyName: '',
        creditCode: '',
        legalPerson: '',
        registerAddress: '',
        contactPhone: '',
        captchaCode: '' // 验证码
      },
      sessionId: '', // 会话 ID
      captchaImageUrl: '', // 验证码图片 URL
      retryCount: 0, // 重试次数
      showCertification: false, // 控制企业认证表单显示
      rules:{
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 3, max: 15, message: '用户名长度在 3 到 15 个字符', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 3, max: 15, message: '密码长度在 3 到 15 个字符', trigger: 'blur' }
        ],
        repassword: [
          { required: true, message: '请再次输入密码', trigger: 'blur' },
          { validator: validatePass2, trigger: 'blur' }
        ],
        role: [
          { required: true, message: '请选择角色', trigger: 'change' }
        ],
        companyName: [
          { required: true, message: '请输入企业名称', trigger: 'blur' }
        ],
        creditCode: [
          { required: true, message: '请输入统一社会信用代码', trigger: 'blur' },
          { min: 18, max: 18, message: '统一社会信用代码为 18 位', trigger: 'blur' }
        ],
        legalPerson: [
          { required: true, message: '请输入法人代表', trigger: 'blur' }
        ],
        registerAddress: [
          { required: true, message: '请输入注册地址', trigger: 'blur' }
        ],
        contactPhone: [
          { required: true, message: '请输入联系电话', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
        ],
        captchaCode: [
          { required: true, message: '请输入验证码', trigger: 'blur' },
          { min: 4, max: 4, message: '验证码为 4 位字符', trigger: 'blur' }
        ]
      }

    }
  },
  mounted() {
    // 页面加载时生成验证码
    this.generateSessionId();
    this.refreshCaptcha();
  },
  methods:{
    back(){
      this.$router.replace('/');
    },
    // 生成会话 ID
    generateSessionId() {
      this.sessionId = 'register_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
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
    handleRoleChange(value) {
      // 当选择 HR 角色时，显示企业认证表单
      this.showCertification = (value === 1);
    },
    register(){
      this.$refs.loginForm.validate((valid) => {
        if (valid) {
          this.loading = true;
          // 构造注册用户对象
          const user = {
            username: this.loginForm.username,
            password: this.loginForm.password,
            role: this.loginForm.role
          };
            
          // 如果是 HR，需要包含企业认证信息
          if (this.loginForm.role === 1) {
            user.companyName = this.loginForm.companyName;
            user.creditCode = this.loginForm.creditCode;
            user.legalPerson = this.loginForm.legalPerson;
            user.registerAddress = this.loginForm.registerAddress;
            user.contactPhone = this.loginForm.contactPhone;
          }
            
          // 发送注册请求，携带验证码参数
          this.$axios.post(
            this.$apiBaseUrl + '/user/?captchaCode=' + encodeURIComponent(this.loginForm.captchaCode) + 
            '&sessionId=' + encodeURIComponent(this.sessionId),
            user
          )
            .then(res => {
              this.loading = false;
              if (res.data.success) {
                this.$message.success("注册成功！");
                // 注册成功后跳转到登录页面
                this.$router.replace('/');
              } else {
                // 如果验证码错误，刷新验证码
                if (res.data.message && res.data.message.includes('验证码')) {
                  this.refreshCaptcha();
                  this.loginForm.captchaCode = ''; // 清空验证码输入框
                }
                this.$message.error("注册失败：" + (res.data.message || '未知错误'));
              }
            })
            .catch(error => {
              this.loading = false;
              console.error('Registration error:', error);
              this.$message.error("注册失败：" + (error.response?.data?.message || error.message || '网络错误'));
            });
        } else {
          this.$message.error("请正确填写注册信息！");
          return false;
        }
      });
    }
  }
}

</script>

<style scoped >
.wrapper {
  /*渐变的背景色*/
  /*height: 100vh;
  background-image: linear-gradient(to bottom right, #FC466B, #3F5EF8);
  overflow: hidden;*/
  /*背景图*/
  background: url("../assets/e44dc62f-007d-47b0-b1ae-94ce94105790.png");
  width: 100%;
  height: 100%;
  position: fixed;
  background-size: 100% 100%;
}

#building {
  /*设置透明度，0为完全透明，1为不透明*/
  opacity: 0.9;
}

.loginBody {
  width: 100%;
  height: 100%;
  background-color: #B3C0D1;
}
.loginDiv {
  position: absolute;
  top: 50%;
  left: 50%;
  margin-top: -200px;
  margin-left: -250px;
  width: 480px;
  height: 500px;
  background: #fff;
  border-radius: 5%;


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

.certification-section {
  margin-top: 20px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.certification-section .el-form-item {
  margin-bottom: 15px;
}

.loginDiv {
  height: auto;
  min-height: 500px;
  max-height: 800px;
  overflow-y: auto;
}
</style>