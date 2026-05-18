<template>
  <div class="student-chat">
    <el-card class="chat-container">
      <template #header>
        <div class="chat-header">
          <span>智能客服</span>
          <el-select v-model="chatType" @change="switchChatType" style="width: 120px">
            <el-option label="智能客服" :value="1" />
            <el-option label="人工客服" :value="2" />
          </el-select>
        </div>
      </template>
      
      <div class="chat-history" ref="chatHistoryRef">
        <div v-for="(msg, index) in messages" :key="index" 
             class="message"
             :class="msg.senderType === 1 ? 'user' : 'ai'">
          <div class="avatar">
            {{ msg.senderType === 1 ? '我' : '客服' }}
          </div>
          <div class="content">{{ msg.messageContent || msg.content }}</div>
        </div>
        
        <div v-if="loading" class="message ai">
          <div class="avatar">客服</div>
          <div class="content">正在思考中...</div>
        </div>
      </div>
      
      <div class="chat-input">
        <el-input
          v-model="inputMessage"
          placeholder="请输入您的问题..."
          @keyup.enter="sendMessage"
          :disabled="loading"
        />
        <el-button type="primary" @click="sendMessage" :loading="loading">发送</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const chatHistoryRef = ref(null)
const messages = ref([])
const inputMessage = ref('')
const loading = ref(false)
const chatType = ref(1)
const currentSessionId = ref(0)

const scrollToBottom = async () => {
  await nextTick()
  if (chatHistoryRef.value) {
    chatHistoryRef.value.scrollTop = chatHistoryRef.value.scrollHeight
  }
}

const createSession = async (type) => {
  try {
    const res = await request.post('/chat/session', null, {
      params: {
        studentId: userStore.userInfo.id,
        chatType: type
      }
    })
    currentSessionId.value = res.data?.id || 0
    messages.value = []
  } catch (e) {
    console.error(e)
  }
}

const loadMessages = async () => {
  if (!currentSessionId.value) return
  try {
    const res = await request.get(`/chat/messages/${currentSessionId.value}`)
    messages.value = res.data || []
    scrollToBottom()
  } catch (e) {
    console.error(e)
  }
}

const switchChatType = async () => {
  const type = chatType.value
  const sessions = await request.get(`/chat/sessions/${userStore.userInfo.id}`)
  const existingSession = (sessions.data || []).find(s => s.chatType === type)
  
  if (existingSession) {
    currentSessionId.value = existingSession.sessionId || 0
    await loadMessages()
  } else {
    await createSession(type)
  }
}

const sendMessage = async () => {
  if (!inputMessage.value.trim()) return
  if (!currentSessionId.value) {
    await createSession(chatType.value)
  }
  
  const content = inputMessage.value.trim()
  inputMessage.value = ''
  loading.value = true
  
  messages.value.push({
    sessionId: currentSessionId.value,
    senderType: 1,
    senderId: userStore.userInfo.id,
    content: content
  })
  scrollToBottom()
  
  try {
    await request.post('/chat/message', null, {
      params: {
        sessionId: currentSessionId.value,
        senderType: 1,
        senderId: userStore.userInfo.id,
        content: content
      }
    })
    await loadMessages()
  } catch (e) {
    console.error(e)
    ElMessage.error('发送失败')
  } finally {
    loading.value = false
  }
}

const loadSessions = async () => {
  try {
    const res = await request.get(`/chat/sessions/${userStore.userInfo.id}`)
    const sessions = res.data || []
    if (sessions.length > 0) {
      const latestSession = sessions[0]
      currentSessionId.value = latestSession.sessionId || 0
      chatType.value = latestSession.chatType
      await loadMessages()
    } else {
      await createSession(1)
    }
  } catch (e) {
    console.error(e)
    await createSession(1)
  }
}

onMounted(() => {
  loadSessions()
})
</script>

<style scoped lang="scss">
.student-chat {
  padding: 20px;
  height: calc(100vh - 120px);
  min-height: 500px;
  
  .chat-container {
    height: 100%;
    display: flex;
    flex-direction: column;
    
    .chat-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    
    .chat-history {
      height: 400px;
      min-height: 300px;
      overflow-y: auto;
      padding: 15px;
      background: #f5f7fa;
      border-radius: 8px;
      margin-bottom: 15px;
      
      .message {
        display: flex;
        margin-bottom: 15px;
        align-items: flex-start;
        
        &.user {
          flex-direction: row-reverse;
          
          .content {
            background: #409eff;
            color: white;
          }
        }
        
        &.ai {
          .content {
            background: white;
            color: #333;
          }
        }
        
        .avatar {
          width: 36px;
          height: 36px;
          border-radius: 50%;
          background: #ddd;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 12px;
          margin: 0 10px;
          flex-shrink: 0;
        }
        
        .content {
          max-width: 70%;
          padding: 10px 15px;
          border-radius: 8px;
          line-height: 1.5;
          word-break: break-word;
        }
      }
    }
    
    .chat-input {
      display: flex;
      gap: 10px;
      
      .el-input {
        flex: 1;
      }
    }
  }
}
</style>
