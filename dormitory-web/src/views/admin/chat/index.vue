<template>
  <div class="admin-chat">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>会话列表</span>
          </template>
          
          <el-table :data="sessions" style="width: 100%" @row-click="selectSession" 
                   highlight-current-row v-loading="loading">
            <el-table-column prop="studentName" label="学生" />
            <el-table-column prop="studentNumber" label="学号" width="120" />
            <el-table-column prop="lastMessage" label="最后消息" show-overflow-tooltip />
            <el-table-column prop="createTime" label="时间" width="150">
              <template #default="{ row }">
                {{ formatTime(row.createTime) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="chat-header">
              <span>聊天详情</span>
              <div v-if="currentSession">
                <el-button type="success" @click="acceptSession" v-if="currentSession.status === 0">
                  接入会话
                </el-button>
                <el-button type="info" @click="closeSession" v-if="currentSession.status === 1">
                  结束会话
                </el-button>
                <el-button type="primary" @click="reopenSession" v-if="currentSession.status === 2">
                  重新接入
                </el-button>
              </div>
            </div>
          </template>
          
          <div class="chat-history" ref="chatHistoryRef" v-if="currentSession">
            <div v-for="(msg, index) in messages" :key="index" 
                 class="message"
                 :class="msg.senderType === 3 ? 'admin' : 'user'">
              <div class="avatar">
                {{ msg.senderType === 3 ? '我' : '学生' }}
              </div>
              <div class="content">{{ msg.messageContent || msg.content }}</div>
            </div>
          </div>
          
          <div class="empty" v-else>
            <span>请选择会话</span>
          </div>
          
          <div class="chat-input" v-if="currentSession && currentSession.status !== 2">
            <el-input
              v-model="inputMessage"
              placeholder="请输入回复..."
              @keyup.enter="sendMessage"
            />
            <el-button type="primary" @click="sendMessage">发送</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const chatHistoryRef = ref(null)
const sessions = ref([])
const messages = ref([])
const currentSession = ref(null)
const inputMessage = ref('')
const loading = ref(false)
let timer = null

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

const scrollToBottom = async () => {
  await nextTick()
  if (chatHistoryRef.value) {
    chatHistoryRef.value.scrollTop = chatHistoryRef.value.scrollHeight
  }
}

const getSessions = async () => {
  try {
    const res = await request.get('/chat/admin/sessions')
    sessions.value = res.data || []
  } catch (e) {
    console.error(e)
  }
}

const selectSession = async (row) => {
  currentSession.value = row
  await loadMessages()
}

const loadMessages = async () => {
  if (!currentSession.value) return
  try {
    const res = await request.get(`/chat/messages/${currentSession.value.sessionId}`)
    messages.value = res.data || []
    scrollToBottom()
  } catch (e) {
    console.error(e)
  }
}

const acceptSession = async () => {
  if (!currentSession.value) return
  const sessionId = currentSession.value.sessionId
  try {
    await request.post('/chat/admin/accept', null, {
      params: {
        sessionId: currentSession.value.sessionId
      }
    })
    ElMessage.success('已接入会话')
    currentSession.value.status = 1
    
    const res = await request.get('/chat/admin/sessions')
    sessions.value = res.data || []
    const session = sessions.value.find(s => s.sessionId === sessionId)
    if (session) {
      currentSession.value = session
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('接入失败')
  }
}

const closeSession = async () => {
  if (!currentSession.value) return
  const sessionId = currentSession.value.sessionId
  try {
    await request.post('/chat/admin/close', null, {
      params: {
        sessionId: currentSession.value.sessionId
      }
    })
    ElMessage.success('会话已结束')
    currentSession.value.status = 2
    
    const res = await request.get('/chat/admin/sessions')
    sessions.value = res.data || []
    const session = sessions.value.find(s => s.sessionId === sessionId)
    if (session) {
      currentSession.value = session
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('操作失败')
  }
}

const reopenSession = async () => {
  if (!currentSession.value) return
  try {
    await request.post('/chat/admin/accept', null, {
      params: {
        sessionId: currentSession.value.sessionId
      }
    })
    ElMessage.success('已重新接入会话')
    currentSession.value.status = 1
    
    const res = await request.get('/chat/admin/sessions')
    sessions.value = res.data || []
    const session = sessions.value.find(s => s.sessionId === currentSession.value.sessionId)
    if (session) {
      currentSession.value = session
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('操作失败')
  }
}

const sendMessage = async () => {
  if (!inputMessage.value.trim() || !currentSession.value) return
  
  const content = inputMessage.value.trim()
  inputMessage.value = ''
  
  messages.value.push({
    sessionId: currentSession.value.sessionId,
    senderType: 2,
    content: content
  })
  scrollToBottom()
  
  try {
    await request.post('/chat/message', null, {
      params: {
        sessionId: currentSession.value.sessionId,
        senderType: 3,
        senderId: userStore.userInfo.id,
        content: content
      }
    })
    await loadMessages()
  } catch (e) {
    console.error(e)
    ElMessage.error('发送失败')
  }
}

onMounted(() => {
  getSessions()
  timer = setInterval(getSessions, 5000)
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<style scoped lang="scss">
.admin-chat {
  padding: 20px;
  
  .chat-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .chat-history {
    height: 400px;
    overflow-y: auto;
    padding: 15px;
    background: #f5f7fa;
    border-radius: 8px;
    margin-bottom: 15px;
    
    .message {
      display: flex;
      margin-bottom: 15px;
      align-items: flex-start;
      
      &.admin {
        flex-direction: row-reverse;
        
        .content {
          background: #409eff;
          color: white;
        }
      }
      
      &.user {
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
  
  .empty {
    height: 400px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f5f7fa;
    border-radius: 8px;
    color: #999;
  }
  
  .chat-input {
    display: flex;
    gap: 10px;
    
    .el-input {
      flex: 1;
    }
  }
}
</style>
