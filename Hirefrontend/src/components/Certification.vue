<!-- src/views/EnterpriseCertification.vue -->
<template>
  <div class="app-wrapper">
    <AppHeader />
<div class="enterprise-certification-container">
    <!-- �������� -->

    <!-- ҳ������ -->
    <div class="container">
      <div class="certification-content">
        <div class="section">
          <div class="section-header">
            <h2 class="section-title">��ҵ��֤</h2>
          </div>

          <div class="certification-form-container">
            <el-form
                :model="certificationForm"
                :rules="rules"
                ref="certificationForm"
                label-width="120px"
                class="certification-form">

              <el-form-item label="��ҵ����" prop="companyName">
                <el-input
                    v-model="certificationForm.companyName"
                    placeholder="��������ҵȫ��">
                </el-input>
              </el-form-item>

              <el-form-item label="ͳһ������ô���" prop="creditCode">
                <el-input
                    v-model="certificationForm.creditCode"
                    placeholder="������18λͳһ������ô���">
                </el-input>
              </el-form-item>

              <el-form-item label="���˴���" prop="legalPerson">
                <el-input
                    v-model="certificationForm.legalPerson"
                    placeholder="��������ҵ���˴�������">
                </el-input>
              </el-form-item>

              <el-form-item label="ע���ַ" prop="registerAddress">
                <el-input
                    v-model="certificationForm.registerAddress"
                    placeholder="��������ҵע���ַ">
                </el-input>
              </el-form-item>

              <el-form-item label="Ӫҵ����" prop="businessTerm">
                <el-date-picker
                    v-model="certificationForm.businessTerm"
                    type="daterange"
                    range-separator="��"
                    start-placeholder="��ʼ����"
                    end-placeholder="��������"
                    format="yyyy-MM-dd"
                    value-format="yyyy-MM-dd">
                </el-date-picker>
              </el-form-item>

              <el-form-item label="Ӫҵִ��" prop="businessLicense">
                <el-upload
                    class="upload-demo"
                    drag
                    action="/api/upload"
                    :on-success="handleLicenseSuccess"
                    :on-error="handleLicenseError"
                    :file-list="licenseFileList"
                    accept=".jpg,.jpeg,.png,.pdf">
                  <i class="el-icon-upload"></i>
                  <div class="el-upload__text">��Ӫҵִ���ϵ��˴�����<em>����ϴ�</em></div>
                  <div class="el-upload__tip" slot="tip">ֻ���ϴ�jpg/png/pdf�ļ����Ҳ�����5MB</div>
                </el-upload>
              </el-form-item>

              <el-form-item label="��ϵ������" prop="contactName">
                <el-input
                    v-model="certificationForm.contactName"
                    placeholder="��������ϵ������">
                </el-input>
              </el-form-item>

              <el-form-item label="��ϵ�绰" prop="contactPhone">
                <el-input
                    v-model="certificationForm.contactPhone"
                    placeholder="��������ϵ�绰">
                </el-input>
              </el-form-item>

              <el-form-item label="��ϵ����" prop="contactEmail">
                <el-input
                    v-model="certificationForm.contactEmail"
                    placeholder="��������ϵ����">
                </el-input>
              </el-form-item>

              <el-form-item>
                <el-button type="primary" @click="submitForm('certificationForm')">�ύ��֤</el-button>
                <el-button @click="resetForm('certificationForm')">����</el-button>
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
  name: 'EnterpriseCertification',
  components: {},
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
          { required: true, message: '��������ҵ����', trigger: 'blur' }
        ],
        creditCode: [
          { required: true, message: '������ͳһ������ô���', trigger: 'blur' },
          { min: 18, max: 18, message: 'ͳһ������ô���Ϊ18λ', trigger: 'blur' }
        ],
        legalPerson: [
          { required: true, message: '�����뷨�˴���', trigger: 'blur' }
        ],
        registerAddress: [
          { required: true, message: '������ע���ַ', trigger: 'blur' }
        ],
        businessTerm: [
          { required: true, message: '��ѡ��Ӫҵ����', trigger: 'change' }
        ],
        contactName: [
          { required: true, message: '��������ϵ������', trigger: 'blur' }
        ],
        contactPhone: [
          { required: true, message: '��������ϵ�绰', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '��������ȷ���ֻ�����', trigger: 'blur' }
        ],
        contactEmail: [
          { required: true, message: '��������ϵ����', trigger: 'blur' },
          { type: 'email', message: '��������ȷ�������ַ', trigger: 'blur' }
        ]
      },
      licenseFileList: []
    }
  },
  methods: {
    handleLicenseSuccess(response) {
      if (response && response.code === 200) {
        this.certificationForm.businessLicense = response.data.url;
        this.$message.success('Ӫҵִ���ϴ��ɹ�');
      } else {
        this.$message.error('Ӫҵִ���ϴ�ʧ��');
      }
    },
    handleLicenseError(error) {
      console.error('�ϴ�ʧ��:', error);
      this.$message.error('Ӫҵִ���ϴ�ʧ�ܣ�������');
    },
    submitForm(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          // ������Ե��ú��API�ύ��֤��Ϣ
          this.$message.success('��ҵ��֤��Ϣ�ύ�ɹ����ȴ����');
          this.$router.push('/ComPany');
        } else {
          this.$message.error('�����Ʊ����Ϣ');
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
     * ��ת����Ϣ����
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

.header {
  background: linear-gradient(135deg, #3498db, #2c3e50);
  color: white;
  padding: 15px 0;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  font-size: 24px;
  font-weight: bold;
  display: flex;
  align-items: center;
}

.logo i {
  margin-right: 10px;
  font-size: 28px;
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

.footer {
  text-align: center;
  padding: 40px 0;
  margin-top: 50px;
  color: #909399;
  border-top: 1px solid #ebeef5;
  background-color: white;
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
