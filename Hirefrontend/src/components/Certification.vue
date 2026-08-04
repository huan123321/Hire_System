<!-- src/views/EnterpriseCertification.vue -->
<template>
  <div class="enterprise-certification-container">
    <!-- 顶部导航 -->
    <AppHeader />

    <!-- 页面内容 -->
    <div class="container">
      <div class="certification-content">
        <div class="section">
          <div class="section-header">
            <h2 class="section-title">企业认证</h2>
          </div>

          <div class="certification-form-container">
            <el-form
                :model="certificationForm"
                :rules="rules"
                ref="certificationForm"
                label-width="120px"
                class="certification-form">

              <el-form-item label="企业名称" prop="companyName">
                <el-input
                    v-model="certificationForm.companyName"
                    placeholder="请输入企业全称">
                </el-input>
              </el-form-item>

              <el-form-item label="统一社会信用代码" prop="creditCode">
                <el-input
                    v-model="certificationForm.creditCode"
                    placeholder="请输入18位统一社会信用代码">
                </el-input>
              </el-form-item>

              <el-form-item label="法人代表" prop="legalPerson">
                <el-input
                    v-model="certificationForm.legalPerson"
                    placeholder="请输入企业法人代表姓名">
                </el-input>
              </el-form-item>

              <el-form-item label="注册地址" prop="registerAddress">
                <el-input
                    v-model="certificationForm.registerAddress"
                    placeholder="请输入企业注册地址">
                </el-input>
              </el-form-item>

              <el-form-item label="营业期限" prop="businessTerm">
                <el-date-picker
                    v-model="certificationForm.businessTerm"
                    type="daterange"
                    range-separator="至"
                    start-placeholder="开始日期"
                    end-placeholder="结束日期"
                    format="yyyy-MM-dd"
                    value-format="yyyy-MM-dd">
                </el-date-picker>
              </el-form-item>

              <el-form-item label="营业执照" prop="businessLicense">
                <el-upload
                    class="upload-demo"
                    drag
                    action="/api/upload"
                    :on-success="handleLicenseSuccess"
                    :on-error="handleLicenseError"
                    :file-list="licenseFileList"
                    accept=".jpg,.jpeg,.png,.pdf">
                  <i class="el-icon-upload"></i>
                  <div class="el-upload__text">将营业执照拖到此处，或<em>点击上传</em></div>
                  <div class="el-upload__tip" slot="tip">只能上传jpg/png/pdf文件，且不超过5MB</div>
                </el-upload>
              </el-form-item>

              <el-form-item label="联系人姓名" prop="contactName">
                <el-input
                    v-model="certificationForm.contactName"
                    placeholder="请输入联系人姓名">
                </el-input>
              </el-form-item>

              <el-form-item label="联系电话" prop="contactPhone">
                <el-input
                    v-model="certificationForm.contactPhone"
                    placeholder="请输入联系电话">
                </el-input>
              </el-form-item>

              <el-form-item label="联系邮箱" prop="contactEmail">
                <el-input
                    v-model="certificationForm.contactEmail"
                    placeholder="请输入联系邮箱">
                </el-input>
              </el-form-item>

              <el-form-item>
                <el-button type="primary" @click="submitForm('certificationForm')">提交认证</el-button>
                <el-button @click="resetForm('certificationForm')">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import AppHeader from '@/components/common/AppHeader.vue'

export default {
  name: 'EnterpriseCertification',
  components: {
    AppHeader
  },
  data() {
    return {
      certificationForm: {
        companyName: '',
        creditCode: '',
        legalPerson: '',
        registerAddress: '',
        businessTerm: [],
        businessLicense: '',
        contactName: '',
        contactPhone: '',
        contactEmail: ''
      },
      rules: {
        companyName: [
          { required: true, message: '请输入企业名称', trigger: 'blur' }
        ],
        creditCode: [
          { required: true, message: '请输入统一社会信用代码', trigger: 'blur' },
          { min: 18, max: 18, message: '统一社会信用代码为18位', trigger: 'blur' }
        ],
        legalPerson: [
          { required: true, message: '请输入法人代表', trigger: 'blur' }
        ],
        registerAddress: [
          { required: true, message: '请输入注册地址', trigger: 'blur' }
        ],
        businessTerm: [
          { required: true, message: '请选择营业期限', trigger: 'change' }
        ],
        contactName: [
          { required: true, message: '请输入联系人姓名', trigger: 'blur' }
        ],
        contactPhone: [
          { required: true, message: '请输入联系电话', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
        ],
        contactEmail: [
          { required: true, message: '请输入联系邮箱', trigger: 'blur' },
          { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
        ]
      },
      licenseFileList: []
    }
  },
  methods: {
    handleLicenseSuccess(response) {
      if (response && response.code === 200) {
        this.certificationForm.businessLicense = response.data.url;
        this.$message.success('营业执照上传成功');
      } else {
        this.$message.error('营业执照上传失败');
      }
    },
    handleLicenseError(error) {
      console.error('上传失败:', error);
      this.$message.error('营业执照上传失败，请重试');
    },
    submitForm(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          // 这里可以调用后端API提交认证信息
          this.$message.success('企业认证信息提交成功，等待审核');
          this.$router.push('/ComPany');
        } else {
          this.$message.error('请完善表单信息');
          return false;
        }
      });
    },
    resetForm(formName) {
      this.$refs[formName].resetFields();
    },
    goToHome() {
      this.$router.push('/HomePage');
    },
    position() {
      this.$router.push('/Position');
    },
    goToCompany() {
      this.$router.push('/ComPany');
    },
    person() {
      this.$router.push('/PerSon');
    },
    /**
     * 跳转到消息中心
     */
    goToMessageCenter() {
      this.$router.push({
        name: 'MessageCenter'
      });
    },
    logout() {
      this.$router.push('/');
    }
  }
}
</script>

<style scoped>
.enterprise-certification-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  font-family: "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB", Arial, sans-serif;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 15px;
}

.certification-content {
  margin-top: 30px;
  flex: 1;
}

.section {
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
}

.section-title {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
}

.certification-form-container {
  max-width: 800px;
  margin: 0 auto;
}

.certification-form {
  padding: 20px;
}

@media (max-width: 768px) {
  .certification-form {
    padding: 10px;
  }

  .section {
    padding: 15px;
  }
}
</style>
