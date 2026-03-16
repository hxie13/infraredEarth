/**
 * AI Chat Panel — Standalone Vue 3 mini-app for multi-agent conversation.
 * Self-initializes and mounts on a new #ai-chat-root element appended to <body>.
 * Uses window.apiClient (from csrf-axios.js) for CSRF-compliant API calls.
 * Communicates with Cesium viewer via window.postMessage().
 */
(function () {
  'use strict';

  // Prevent double initialization
  if (document.getElementById('ai-chat-root')) return;

  // ── CSS ──────────────────────────────────────────────────────────────
  const STYLES = `
    #ai-chat-root { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; }

    .ai-fab {
      position: fixed; bottom: 24px; right: 24px; z-index: 9999;
      width: 56px; height: 56px; border-radius: 50%;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: #fff; border: none; cursor: pointer;
      box-shadow: 0 4px 16px rgba(102,126,234,0.4);
      display: flex; align-items: center; justify-content: center;
      font-size: 24px; transition: transform 0.2s, box-shadow 0.2s;
    }
    .ai-fab:hover { transform: scale(1.08); box-shadow: 0 6px 24px rgba(102,126,234,0.5); }
    .ai-fab.hidden { display: none; }

    .ai-panel {
      position: fixed; bottom: 24px; right: 24px; z-index: 10000;
      width: 380px; height: 540px;
      background: #1a1a2e; border-radius: 16px;
      box-shadow: 0 8px 40px rgba(0,0,0,0.4);
      display: flex; flex-direction: column;
      overflow: hidden; transition: opacity 0.2s, transform 0.2s;
    }
    .ai-panel.hidden { opacity: 0; transform: translateY(20px); pointer-events: none; }

    .ai-header {
      display: flex; align-items: center; justify-content: space-between;
      padding: 14px 16px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: #fff; flex-shrink: 0;
    }
    .ai-header-title { font-size: 15px; font-weight: 600; }
    .ai-header-btn {
      background: rgba(255,255,255,0.2); border: none; color: #fff;
      width: 28px; height: 28px; border-radius: 6px; cursor: pointer;
      font-size: 16px; display: flex; align-items: center; justify-content: center;
    }
    .ai-header-btn:hover { background: rgba(255,255,255,0.3); }

    .ai-messages {
      flex: 1; overflow-y: auto; padding: 16px;
      display: flex; flex-direction: column; gap: 12px;
    }
    .ai-messages::-webkit-scrollbar { width: 4px; }
    .ai-messages::-webkit-scrollbar-thumb { background: rgba(255,255,255,0.15); border-radius: 2px; }

    .ai-msg { max-width: 85%; padding: 10px 14px; border-radius: 12px; font-size: 13px; line-height: 1.6; word-break: break-word; }
    .ai-msg-user { align-self: flex-end; background: #667eea; color: #fff; border-bottom-right-radius: 4px; }
    .ai-msg-assistant { align-self: flex-start; background: #16213e; color: #e0e0e0; border-bottom-left-radius: 4px; }
    .ai-msg-system { align-self: center; background: transparent; color: #888; font-size: 12px; text-align: center; }

    .ai-msg-actions { margin-top: 8px; display: flex; flex-wrap: wrap; gap: 6px; }
    .ai-msg-action-btn {
      padding: 4px 10px; border-radius: 12px; font-size: 11px;
      border: 1px solid rgba(102,126,234,0.5); background: rgba(102,126,234,0.1);
      color: #667eea; cursor: pointer; transition: background 0.2s;
    }
    .ai-msg-action-btn:hover { background: rgba(102,126,234,0.25); }

    .ai-data-card {
      margin-top: 8px; padding: 8px 10px; border-radius: 8px;
      background: rgba(255,255,255,0.05); border: 1px solid rgba(255,255,255,0.1);
      font-size: 12px; color: #aaa;
    }
    .ai-data-card summary { cursor: pointer; color: #667eea; font-weight: 500; }

    .ai-typing { display: flex; gap: 4px; padding: 8px 14px; align-self: flex-start; }
    .ai-typing-dot {
      width: 6px; height: 6px; border-radius: 50%; background: #667eea;
      animation: ai-bounce 1.4s infinite ease-in-out both;
    }
    .ai-typing-dot:nth-child(1) { animation-delay: -0.32s; }
    .ai-typing-dot:nth-child(2) { animation-delay: -0.16s; }
    @keyframes ai-bounce {
      0%, 80%, 100% { transform: scale(0); }
      40% { transform: scale(1); }
    }

    .ai-input-area {
      padding: 12px; background: #0f0f23; flex-shrink: 0;
      border-top: 1px solid rgba(255,255,255,0.08);
    }
    .ai-input-row { display: flex; gap: 8px; }
    .ai-input {
      flex: 1; padding: 10px 14px; border-radius: 20px;
      background: #1a1a2e; border: 1px solid rgba(255,255,255,0.15);
      color: #e0e0e0; font-size: 13px; outline: none;
    }
    .ai-input::placeholder { color: #666; }
    .ai-input:focus { border-color: #667eea; }
    .ai-send-btn {
      width: 40px; height: 40px; border-radius: 50%;
      background: linear-gradient(135deg, #667eea, #764ba2);
      border: none; color: #fff; cursor: pointer; font-size: 16px;
      display: flex; align-items: center; justify-content: center;
      transition: opacity 0.2s;
    }
    .ai-send-btn:disabled { opacity: 0.4; cursor: not-allowed; }

    .ai-chips {
      display: flex; flex-wrap: wrap; gap: 6px; margin-top: 8px;
    }
    .ai-chip {
      padding: 5px 12px; border-radius: 14px; font-size: 11px;
      background: rgba(102,126,234,0.1); border: 1px solid rgba(102,126,234,0.3);
      color: #667eea; cursor: pointer; transition: background 0.2s;
    }
    .ai-chip:hover { background: rgba(102,126,234,0.25); }

    @media (max-width: 480px) {
      .ai-panel { width: calc(100vw - 16px); height: calc(100vh - 100px); right: 8px; bottom: 8px; }
    }
  `;

  const styleEl = document.createElement('style');
  styleEl.textContent = STYLES;
  document.head.appendChild(styleEl);

  // ── Container ────────────────────────────────────────────────────────
  const container = document.createElement('div');
  container.id = 'ai-chat-root';
  document.body.appendChild(container);

  // ── Vue App ──────────────────────────────────────────────────────────
  const { createApp, ref, nextTick, onMounted } = Vue;

  const AiChatApp = createApp({
    setup() {
      const isOpen = ref(false);
      const inputText = ref('');
      const isTyping = ref(false);
      const conversationId = ref(null);
      const messagesEl = ref(null);

      const messages = ref([
        { role: 'system', content: '你好！我是红外地球AI助手。你可以用自然语言告诉我你想做什么，例如搜索卫星数据、查询灾害事件、提交分析任务等。' }
      ]);

      const quickChips = [
        '搜索卫星数据',
        '查询灾害事件',
        '查看可用算法',
        '平台功能介绍'
      ];

      function toggle() {
        isOpen.value = !isOpen.value;
      }

      function scrollToBottom() {
        nextTick(() => {
          if (messagesEl.value) {
            messagesEl.value.scrollTop = messagesEl.value.scrollHeight;
          }
        });
      }

      async function sendMessage(text) {
        if (!text || !text.trim()) return;
        const msg = text.trim();
        inputText.value = '';

        messages.value.push({ role: 'user', content: msg });
        scrollToBottom();

        isTyping.value = true;

        try {
          const apiClient = window.apiClient || window.axios;
          if (!apiClient) {
            messages.value.push({ role: 'assistant', content: 'API客户端未加载，请刷新页面重试。' });
            return;
          }

          const response = await apiClient.post('/infrared/rest/ai/chat', {
            message: msg,
            conversation_id: conversationId.value
          });

          const result = response.data;

          if (result.status === 'Success') {
            conversationId.value = result.conversationId;

            const assistantMsg = {
              role: 'assistant',
              content: result.reply || '处理完成。',
              data: result.data,
              actions: result.actions
            };
            messages.value.push(assistantMsg);

            // Execute visualization actions
            if (result.actions && result.actions.length > 0) {
              executeActions(result.actions);
            }
          } else {
            messages.value.push({ role: 'assistant', content: '请求失败: ' + (result.errorCode || '未知错误') });
          }
        } catch (err) {
          console.error('AI chat error:', err);
          messages.value.push({
            role: 'assistant',
            content: '网络请求失败，请检查服务是否启动。'
          });
        } finally {
          isTyping.value = false;
          scrollToBottom();
        }
      }

      function executeActions(actions) {
        for (const action of actions) {
          const p = action.params || {};
          switch (action.type) {
            case 'LOAD_IMAGE': {
              // Validate coordinates before sending to Cesium — NaN crashes the renderer
              const lup = p.leftUpPos;
              const rbp = p.rightBottomPos;
              if (lup && rbp &&
                  isFinite(lup.lon) && isFinite(lup.lat) &&
                  isFinite(rbp.lon) && isFinite(rbp.lat) && p.imgAddr) {
                window.postMessage({
                  operation: 'loadImgResult',
                  imgAddr: p.imgAddr,
                  leftUpPos: lup,
                  rightBottomPos: rbp,
                  id: p.id
                }, '*');
              } else {
                console.warn('AI: skipping LOAD_IMAGE — invalid coordinates', p);
              }
              break;
            }
            case 'FLY_TO': {
              const lon = parseFloat(p.longitude);
              const lat = parseFloat(p.latitude);
              if (isFinite(lon) && isFinite(lat)) {
                window.postMessage({
                  operation: 'flyToCoordinates',
                  longitude: lon,
                  latitude: lat
                }, '*');
              }
              break;
            }
            case 'CLEAR_MAP':
              window.postMessage({ operation: 'cleanup' }, '*');
              break;
            case 'HIGHLIGHT':
              if (p.id != null) {
                window.postMessage({ operation: 'active', id: p.id }, '*');
              }
              break;
            case 'SHOW_TABLE': {
              // Update the page Vue app's data list
              const tableData = p.data;
              if (tableData && Array.isArray(tableData)) {
                // Try multiple common Vue app references
                const app = window._vueApp || window.__vueApp;
                if (app && app.userList !== undefined) {
                  app.userList = tableData;
                }
              }
              break;
            }
          }
        }
      }

      function handleKeydown(e) {
        if (e.key === 'Enter' && !e.shiftKey) {
          e.preventDefault();
          sendMessage(inputText.value);
        }
      }

      function chipClick(text) {
        sendMessage(text);
      }

      function formatDataSummary(data) {
        if (!data) return '';
        const totalRecords = data.totalRecords || data.total || '';
        const list = data.list || data.ncList;
        if (list && Array.isArray(list)) {
          return '共 ' + list.length + ' 条数据' + (totalRecords ? '（总计 ' + totalRecords + ' 条）' : '');
        }
        return JSON.stringify(data).substring(0, 200);
      }

      return {
        isOpen, inputText, isTyping, messages, messagesEl,
        quickChips, toggle, sendMessage, handleKeydown, chipClick,
        formatDataSummary, executeActions
      };
    },

    template: `
      <button class="ai-fab" :class="{ hidden: isOpen }" @click="toggle" title="AI 助手">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M12 2a7 7 0 0 1 7 7c0 2.38-1.19 4.47-3 5.74V17a2 2 0 0 1-2 2H10a2 2 0 0 1-2-2v-2.26C6.19 13.47 5 11.38 5 9a7 7 0 0 1 7-7z"/>
          <path d="M10 21v1a2 2 0 0 0 4 0v-1"/><path d="M9 17h6"/>
        </svg>
      </button>

      <div class="ai-panel" :class="{ hidden: !isOpen }">
        <div class="ai-header">
          <span class="ai-header-title">AI 助手</span>
          <button class="ai-header-btn" @click="toggle" title="最小化">—</button>
        </div>

        <div class="ai-messages" ref="messagesEl">
          <template v-for="(msg, i) in messages" :key="i">
            <div :class="['ai-msg', 'ai-msg-' + msg.role]">
              <div v-html="msg.content"></div>

              <details v-if="msg.data" class="ai-data-card">
                <summary>{{ formatDataSummary(msg.data) }}</summary>
                <pre style="max-height:120px;overflow:auto;font-size:11px;margin-top:6px;">{{ JSON.stringify(msg.data, null, 2).substring(0, 1000) }}</pre>
              </details>

              <div v-if="msg.actions && msg.actions.length" class="ai-msg-actions">
                <button v-for="(a, j) in msg.actions" :key="j" class="ai-msg-action-btn"
                        @click="executeActions([a])">
                  {{ a.type === 'FLY_TO' ? '地图定位' : a.type === 'LOAD_IMAGE' ? '加载影像' : a.type === 'CLEAR_MAP' ? '清除地图' : a.type }}
                </button>
              </div>
            </div>
          </template>

          <div v-if="isTyping" class="ai-typing">
            <div class="ai-typing-dot"></div>
            <div class="ai-typing-dot"></div>
            <div class="ai-typing-dot"></div>
          </div>
        </div>

        <div class="ai-input-area">
          <div class="ai-input-row">
            <input class="ai-input" v-model="inputText" @keydown="handleKeydown"
                   placeholder="输入你的问题..." :disabled="isTyping" />
            <button class="ai-send-btn" @click="sendMessage(inputText)" :disabled="isTyping || !inputText.trim()">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor">
                <path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"/>
              </svg>
            </button>
          </div>
          <div class="ai-chips" v-if="messages.length <= 2">
            <span v-for="chip in quickChips" :key="chip" class="ai-chip" @click="chipClick(chip)">{{ chip }}</span>
          </div>
        </div>
      </div>
    `
  });

  AiChatApp.mount('#ai-chat-root');
})();
