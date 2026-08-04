<template>
  <div class="ai-chat-page">
    <AppHeader />

    <div class="ai-chat-box">
      <div class="chat-header">
        <div class="header-info">
          <i class="el-icon-cpu"></i>
          <span>AI 招聘助手</span>
          <el-tag size="mini" type="warning">DeepSeek</el-tag>
        </div>
        <el-button size="mini" icon="el-icon-close" @click="goBack"></el-button>
      </div>

      <div class="message-list" ref="messageList">
        <div v-if="messages.length === 0" class="welcome-area">
          <i class="el-icon-cpu welcome-icon"></i>
          <h3>你好，我是 AI 招聘助手</h3>
          <p>自由提问，或使用以下工具：</p>
          <div class="quick-actions">
            <el-button size="small" @click="askQuick('如何优化简历？')">优化简历</el-button>
            <el-button size="small" @click="askQuick('常见的面试问题有哪些？')">面试问题</el-button>
            <el-button size="small" @click="askQuick('Java开发需要掌握哪些技能？')">技能建议</el-button>
            <el-button size="small" @click="askQuick('如何谈薪资？')">薪资谈判</el-button>
          </div>
        </div>

        <div v-for="(msg, index) in messages" :key="index"
             :class="['message-item', msg.role === 'user' ? 'self' : 'ai']">
          <div class="message-avatar">
            <i :class="msg.role === 'user' ? 'el-icon-user' : 'el-icon-cpu'"></i>
          </div>
          <div class="message-content">
            <div class="message-bubble" v-html="msg.content"></div>
            <div class="message-time">{{ msg.time }}</div>
          </div>
        </div>

        <div v-if="thinking" class="message-item ai">
          <div class="message-avatar"><i class="el-icon-cpu"></i></div>
          <div class="message-content">
            <div class="message-bubble thinking-bubble">
              <span class="dot"></span><span class="dot"></span><span class="dot"></span>
            </div>
          </div>
        </div>
      </div>

      <div class="tool-bar">
        <el-button size="mini" icon="el-icon-document" @click="showTool('resume')">简历分析</el-button>
        <el-button size="mini" icon="el-icon-connection" @click="showTool('match')">人岗匹配</el-button>
        <el-button size="mini" icon="el-icon-edit" @click="showTool('jd')">JD优化</el-button>
        <el-button size="mini" icon="el-icon-s-data" @click="showTool('feedback')">反馈总结</el-button>
      </div>

      <div class="chat-input">
        <el-input v-model="inputMessage" type="textarea" :rows="2"
                  placeholder="输入问题，Enter 发送..."
                  :disabled="thinking"
                  @keyup.enter.native="sendMessage">
        </el-input>
        <div class="input-actions">
          <el-button size="small" @click="clearMessages">清空对话</el-button>
          <el-button type="primary" size="small" :loading="thinking"
                     @click="sendMessage">{{ thinking ? '思考中...' : '发送' }}</el-button>
        </div>
      </div>
    </div>

    <!-- 工具弹窗 -->
    <el-dialog :title="toolTitle" :visible.sync="toolVisible" width="560px" :close-on-click-modal="false">
      <el-form v-if="toolType === 'resume'" label-width="80px">
        <el-form-item label="目标职位">
          <el-input v-model="toolResume.position" placeholder="如：高级Java工程师"></el-input>
        </el-form-item>
        <el-form-item label="简历内容">
          <el-input v-model="toolResume.text" type="textarea" :rows="8"
                    placeholder="粘贴简历文本..."></el-input>
        </el-form-item>
      </el-form>

      <el-form v-if="toolType === 'match'" label-width="80px">
        <el-form-item label="职位要求">
          <el-input v-model="toolMatch.requirements" type="textarea" :rows="5"
                    placeholder="粘贴职位要求..."></el-input>
        </el-form-item>
        <el-form-item label="简历内容">
          <el-input v-model="toolMatch.resume" type="textarea" :rows="5"
                    placeholder="粘贴简历文本..."></el-input>
        </el-form-item>
      </el-form>

      <el-form v-if="toolType === 'jd'" label-width="80px">
        <el-form-item label="JD草稿">
          <el-input v-model="toolJD.draft" type="textarea" :rows="8"
                    placeholder="粘贴职位描述草稿..."></el-input>
        </el-form-item>
      </el-form>

      <el-form v-if="toolType === 'feedback'" label-width="80px">
        <el-form-item label="候选人">
          <el-input v-model="toolFeedback.name" placeholder="候选人姓名"></el-input>
        </el-form-item>
        <el-form-item label="应聘职位">
          <el-input v-model="toolFeedback.job" placeholder="职位名称"></el-input>
        </el-form-item>
        <el-form-item label="面试反馈">
          <el-input v-model="toolFeedback.feedback" type="textarea" :rows="6"
                    placeholder="粘贴面试评价（多条请换行分隔）..."></el-input>
        </el-form-item>
      </el-form>

      <span slot="footer">
        <el-button @click="toolVisible = false">取消</el-button>
        <el-button type="primary" :loading="thinking" @click="runTool">{{ thinking ? '处理中...' : '开始分析' }}</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import AppHeader from '@/components/common/AppHeader.vue'

export default {
  name: 'AiChat',
  components: { AppHeader },
  data() {
    return {
      messages: [],
      inputMessage: '',
      thinking: false,
      toolVisible: false,
      toolType: '',
      toolResume: { position: '', text: '' },
      toolMatch: { requirements: '', resume: '' },
      toolJD: { draft: '' },
      toolFeedback: { name: '', job: '', feedback: '' }
    };
  },
  computed: {
    toolTitle() {
      const titles = {
        resume: '简历智能分析',
        match: '职位与简历匹配度',
        jd: '职位描述优化',
        feedback: '面试反馈总结'
      };
      return titles[this.toolType] || '';
    }
  },
  mounted() {
    this.$nextTick(() => this.scrollToBottom());
  },
  updated() {
    this.$nextTick(() => this.scrollToBottom());
  },
  methods: {
    async sendMessage() {
      const msg = this.inputMessage.trim();
      if (!msg) return;
      this.messages.push({ role: 'user', content: this.escapeHtml(msg), time: new Date().toLocaleTimeString('zh-CN') });
      this.inputMessage = '';
      this.thinking = true;
      try {
        const response = await this.$axios.post(this.$apiBaseUrl + '/ai/chat', { message: msg });
        if (response.data.code === 200) {
          this.messages.push({ role: 'ai', content: this.formatMessage(response.data.data.reply), time: new Date().toLocaleTimeString('zh-CN') });
        } else {
          this.messages.push({ role: 'ai', content: 'AI 服务异常：' + response.data.message, time: new Date().toLocaleTimeString('zh-CN') });
        }
      } catch (e) {
        this.messages.push({ role: 'ai', content: 'AI 服务不可用，请检查 DEEPSEEK_API_KEY', time: new Date().toLocaleTimeString('zh-CN') });
      } finally {
        this.thinking = false;
      }
    },

    showTool(type) {
      this.toolType = type;
      this.toolResume = { position: '', text: '' };
      this.toolMatch = { requirements: '', resume: '' };
      this.toolJD = { draft: '' };
      this.toolFeedback = { name: '', job: '', feedback: '' };
      this.toolVisible = true;
    },

    async runTool() {
      this.thinking = true;
      let url, body, label;
      try {
        if (this.toolType === 'resume') {
          if (!this.toolResume.text) { this.$message.warning('请输入简历内容'); this.thinking = false; return; }
          url = '/ai/analyze-resume';
          body = { resumeText: this.toolResume.text, targetPosition: this.toolResume.position };
          label = '简历分析';
        } else if (this.toolType === 'match') {
          if (!this.toolMatch.requirements || !this.toolMatch.resume) { this.$message.warning('请填写完整'); this.thinking = false; return; }
          url = '/ai/match-score';
          body = { jobRequirements: this.toolMatch.requirements, resumeText: this.toolMatch.resume };
          label = '人岗匹配';
        } else if (this.toolType === 'jd') {
          if (!this.toolJD.draft) { this.$message.warning('请输入JD草稿'); this.thinking = false; return; }
          url = '/ai/optimize-jd';
          body = { draftJD: this.toolJD.draft };
          label = 'JD优化';
        } else if (this.toolType === 'feedback') {
          if (!this.toolFeedback.name || !this.toolFeedback.feedback) { this.$message.warning('请填写完整'); this.thinking = false; return; }
          url = '/ai/summarize-feedback';
          body = { candidateName: this.toolFeedback.name, jobTitle: this.toolFeedback.job, feedbackList: this.toolFeedback.feedback };
          label = '反馈总结';
        }

        const response = await this.$axios.post(this.$apiBaseUrl + url, body);
        this.toolVisible = false;
        this.messages.push({ role: 'user', content: `<strong>[${label}]</strong> 已提交分析`, time: new Date().toLocaleTimeString('zh-CN') });

        if (response.data.code === 200) {
          const result = response.data.data.result || response.data.data.questions;
          this.messages.push({ role: 'ai', content: this.formatToolResult(label, result), time: new Date().toLocaleTimeString('zh-CN') });
        } else {
          this.messages.push({ role: 'ai', content: '分析失败：' + response.data.message, time: new Date().toLocaleTimeString('zh-CN') });
        }
      } catch (e) {
        this.$message.error('分析失败');
      } finally {
        this.thinking = false;
      }
    },

    formatToolResult(label, jsonStr) {
      let text = `<strong>【${label}结果】</strong><br><br>`;
      try {
        const obj = JSON.parse(jsonStr);
        if (obj.error) return text + obj.error;
        for (const [key, val] of Object.entries(obj)) {
          const cn = { score: '综合评分', overall: '综合匹配度', strengths: '核心优势', weaknesses: '待改进点',
            suggestions: '优化建议', highlight: '亮点提炼', skillMatch: '技能匹配度', experienceMatch: '经验匹配度',
            educationMatch: '学历匹配度', highlights: '匹配亮点', gaps: '不足之处', recommendation: '推荐理由',
            optimized: '优化后JD', changes: '改进说明', keywords: '关键词', rating: '综合评分',
            summary: '总体评价', techAssessment: '技术能力', communication: '沟通能力',
            teamFit: '团队匹配度', hireSuggestion: '录用建议', risks: '风险提示', nextStep: '下一步建议' };
          const labelText = cn[key] || key;
          if (Array.isArray(val)) {
            text += `<strong>${labelText}：</strong><br>${val.map(v => '• ' + v).join('<br>')}<br><br>`;
          } else if (typeof val === 'number') {
            text += `<strong>${labelText}：</strong>${val}<br><br>`;
          } else {
            text += `<strong>${labelText}：</strong>${val}<br><br>`;
          }
        }
      } catch (e) {
        text += this.formatMessage(jsonStr);
      }
      return text;
    },

    askQuick(text) { this.inputMessage = text; this.sendMessage(); },
    clearMessages() { this.messages = []; this.$message.success('已清空'); },
    goBack() { this.$router.go(-1); },
    scrollToBottom() { const el = this.$refs.messageList; if (el) el.scrollTop = el.scrollHeight; },
    formatMessage(text) {
      if (!text) return '';
      let html = this.escapeHtml(text);
      html = html.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>');
      html = html.replace(/`([^`]+)`/g, '<code>$1</code>');
      html = html.replace(/\n/g, '<br>');
      html = html.replace(/^[-*]\s(.+)$/gm, '• $1');
      html = html.replace(/^(\d+)[.)]\s(.+)$/gm, '$1. $2');
      return html;
    },
    escapeHtml(text) { const div = document.createElement('div'); div.textContent = text; return div.innerHTML; }
  }
};
</script>

<style scoped>
.ai-chat-page { min-height: 100vh; background: #f0f2f5; padding: 20px; display: flex; flex-direction: column; }
.ai-chat-box { display: flex; flex-direction: column; width: 750px; max-width: 100%; height: 78vh; background: #fff; border-radius: 12px; box-shadow: 0 4px 20px rgba(0,0,0,0.1); margin: 0 auto; }
.chat-header { display: flex; justify-content: space-between; align-items: center; padding: 14px 20px; border-bottom: 1px solid #e4e7ed; background: linear-gradient(135deg, #409EFF, #36cfc9); color: white; border-radius: 12px 12px 0 0; }
.header-info { display: flex; align-items: center; gap: 10px; font-size: 16px; font-weight: bold; }
.header-info i { font-size: 22px; }
.message-list { flex: 1; overflow-y: auto; padding: 20px; background: #f5f7fa; }
.welcome-area { display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100%; color: #606266; }
.welcome-icon { font-size: 64px; color: #409EFF; margin-bottom: 10px; }
.quick-actions { display: flex; flex-wrap: wrap; justify-content: center; gap: 10px; margin-top: 15px; }
.message-item { display: flex; margin-bottom: 16px; gap: 10px; }
.message-item.self { flex-direction: row-reverse; }
.message-avatar { width: 36px; height: 36px; border-radius: 50%; display: flex; align-items: center; justify-content: center; color: white; font-size: 18px; flex-shrink: 0; }
.message-item.self .message-avatar { background: #409EFF; }
.message-item.ai .message-avatar { background: #36cfc9; }
.message-content { max-width: 78%; }
.message-bubble { padding: 10px 16px; border-radius: 8px; background: white; word-wrap: break-word; line-height: 1.7; font-size: 14px; box-shadow: 0 1px 3px rgba(0,0,0,0.08); }
.message-item.self .message-bubble { background: #409EFF; color: white; }
.message-item.ai .message-bubble ::v-deep code { background: rgba(0,0,0,0.06); padding: 2px 6px; border-radius: 4px; font-size: 13px; }
.message-item.ai .message-bubble ::v-deep strong { color: #303133; }
.message-time { font-size: 11px; color: #909399; margin-top: 4px; text-align: right; }
.thinking-bubble { display: flex; align-items: center; gap: 4px; height: 24px; }
.dot { width: 8px; height: 8px; border-radius: 50%; background: #909399; animation: dot-bounce 1.4s infinite ease-in-out both; }
.dot:nth-child(1) { animation-delay: -0.32s; }
.dot:nth-child(2) { animation-delay: -0.16s; }
@keyframes dot-bounce { 0%,80%,100% { transform: scale(0); } 40% { transform: scale(1); } }
.tool-bar { display: flex; gap: 8px; padding: 8px 20px; border-top: 1px solid #ebeef5; background: #fafafa; flex-wrap: wrap; }
.chat-input { padding: 12px 20px; border-top: 1px solid #e4e7ed; background: white; border-radius: 0 0 12px 12px; }
.input-actions { display: flex; justify-content: flex-end; align-items: center; gap: 10px; margin-top: 8px; }
</style>
