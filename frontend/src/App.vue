<script setup>
import { computed, onMounted, reactive, ref } from 'vue'

const token = ref(localStorage.getItem('env-token') || '')
const currentUser = ref(JSON.parse(localStorage.getItem('env-user') || 'null'))
const activeModule = ref('dashboard')
const loading = ref(false)
const toast = ref('')

const loginForm = reactive({ username: 'admin', password: '123456' })
const users = ref([])
const areas = ref([])
const devices = ref([])
const reports = ref([])
const warnings = ref([])
const announcements = ref([])
const warningRules = ref([])
const tasks = ref([])
const logs = ref([])
const notifications = ref([])
const mapPoints = ref([])
const overview = ref({})
const selectedReportDetail = ref(null)
const selectedDispatchReport = ref(null)
const selectedDevice = ref(null)
const deviceReadings = ref([])
const publicProgressList = ref([])
const attachmentFile = ref(null)

const newArea = reactive({
  name: '',
  code: '',
  parentId: '',
  manager: '',
  riskLevel: '中',
  address: '',
})

const newDevice = reactive({
  name: '',
  code: '',
  type: '空气微站',
  areaName: '',
  status: '在线',
  lastOnline: '',
  pm25: 0,
  voc: 0,
})

const newReport = reactive({
  reporterName: '',
  phone: '',
  areaName: '',
  location: '',
  category: '异味扰民',
  description: '',
  source: '后台录入',
})

const publicReport = reactive({
  reporterName: '',
  phone: '',
  areaName: '',
  location: '',
  category: '异味扰民',
  description: '',
})

const newWarning = reactive({
  title: '',
  level: '中',
  areaName: '',
  sourceType: '设备监测',
  advice: '',
})

const newRule = reactive({
  name: '',
  metricName: 'VOC',
  operator: '>',
  threshold: 1,
  level: '中',
  enabled: true,
  advice: '',
})

const reportFilters = reactive({
  keyword: '',
  areaName: '',
  status: '',
  category: '',
})

const newRecord = reactive({
  action: '现场核查',
  remark: '',
})

const newTask = reactive({
  reportId: '',
  title: '',
  assignee: '执法监督员',
  deadline: '',
  priority: '中',
})

const publicQuery = reactive({
  phone: '',
  reportId: '',
})

const navItems = [
  { key: 'dashboard', label: '数据看板', roles: ['ADMIN', 'INSPECTOR', 'ANALYST'] },
  { key: 'users', label: '用户权限', roles: ['ADMIN'] },
  { key: 'areas', label: '区域管理', roles: ['ADMIN'] },
  { key: 'devices', label: '设备对接', roles: ['ADMIN', 'ANALYST'] },
  { key: 'map', label: '监管地图', roles: ['ADMIN', 'INSPECTOR', 'ANALYST'] },
  { key: 'reports', label: '监督数据', roles: ['ADMIN', 'INSPECTOR'] },
  { key: 'tasks', label: '任务派单', roles: ['ADMIN', 'INSPECTOR'] },
  { key: 'analysis', label: '数据分析', roles: ['ADMIN', 'ANALYST'] },
  { key: 'warnings', label: '监督预警', roles: ['ADMIN', 'INSPECTOR', 'ANALYST'] },
  { key: 'notifications', label: '消息通知', roles: ['ADMIN', 'INSPECTOR', 'ANALYST'] },
  { key: 'logs', label: '操作日志', roles: ['ADMIN'] },
  { key: 'public', label: '公众模块', roles: ['ADMIN', 'INSPECTOR', 'ANALYST'] },
]

const moduleTitle = computed(() => navItems.find((item) => item.key === activeModule.value)?.label || '')
const currentRole = computed(() => currentUser.value?.role || '')
const visibleNavItems = computed(() => navItems.filter((item) => item.roles.includes(currentRole.value)))
const canManage = computed(() => ['ADMIN', 'INSPECTOR'].includes(currentRole.value))
const canAdmin = computed(() => currentRole.value === 'ADMIN')
const unreadNotifications = computed(() => notifications.value.filter((item) => !item.readFlag).length)
const overdueTasks = computed(() => {
  const now = new Date()
  return tasks.value.filter((task) => task.status !== '已完成' && new Date(task.deadline.replace(' ', 'T')) < now)
})

const onlineRate = computed(() => {
  if (!overview.value.devices) return 0
  return Math.round((overview.value.onlineDevices / overview.value.devices) * 100)
})

const pendingReports = computed(() => reports.value.filter((item) => item.status !== '已办结').length)
const unresolvedWarnings = computed(() => warnings.value.filter((item) => item.status !== '已处置').length)
const filteredReports = computed(() => {
  const keyword = reportFilters.keyword.trim().toLowerCase()
  return reports.value.filter((item) => {
    const matchedKeyword =
      !keyword ||
      [item.reporterName, item.phone, item.areaName, item.location, item.category, item.description, item.source]
        .filter(Boolean)
        .some((value) => String(value).toLowerCase().includes(keyword))
    const matchedArea = !reportFilters.areaName || item.areaName === reportFilters.areaName
    const matchedStatus = !reportFilters.status || item.status === reportFilters.status
    const matchedCategory = !reportFilters.category || item.category === reportFilters.category
    return matchedKeyword && matchedArea && matchedStatus && matchedCategory
  })
})

async function request(path, options = {}) {
  const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) }
  if (token.value) headers.Authorization = `Bearer ${token.value}`
  const response = await fetch(path, {
    ...options,
    headers,
    body: options.body ? JSON.stringify(options.body) : undefined,
  })
  const payload = await response.json()
  if (!response.ok || !payload.success) {
    throw new Error(payload.message || '请求失败')
  }
  return payload.data
}

function showToast(text) {
  toast.value = text
  window.setTimeout(() => {
    if (toast.value === text) toast.value = ''
  }, 2400)
}

async function login() {
  loading.value = true
  try {
    const data = await request('/api/auth/login', { method: 'POST', body: loginForm })
    token.value = data.token
    currentUser.value = data.user
    localStorage.setItem('env-token', data.token)
    localStorage.setItem('env-user', JSON.stringify(data.user))
    await loadAll()
    showToast('登录成功')
  } catch (error) {
    showToast(error.message)
  } finally {
    loading.value = false
  }
}

function logout() {
  token.value = ''
  currentUser.value = null
  localStorage.removeItem('env-token')
  localStorage.removeItem('env-user')
}

async function loadAll() {
  loading.value = true
  try {
    const role = currentRole.value
    const canLoadUsers = role === 'ADMIN'
    const canLoadDevices = ['ADMIN', 'ANALYST'].includes(role)
    const canLoadReports = ['ADMIN', 'INSPECTOR'].includes(role)
    const canLoadTasks = ['ADMIN', 'INSPECTOR'].includes(role)
    const canLoadLogs = role === 'ADMIN'
    const [
      overviewData,
      userData,
      areaData,
      deviceData,
      mapData,
      reportData,
      taskData,
      warningData,
      ruleData,
      logData,
      notificationData,
      announcementData,
    ] = await Promise.all([
      request('/api/analysis/overview'),
      loadIf(canLoadUsers, '/api/users'),
      request('/api/areas'),
      loadIf(canLoadDevices, '/api/devices'),
      request('/api/map-points'),
      loadIf(canLoadReports, '/api/reports'),
      loadIf(canLoadTasks, '/api/tasks'),
      request('/api/warnings'),
      request('/api/warnings/rules'),
      loadIf(canLoadLogs, '/api/logs'),
      request('/api/notifications'),
      request('/api/public/announcements'),
    ])
    overview.value = overviewData
    users.value = userData
    areas.value = areaData
    devices.value = deviceData
    mapPoints.value = mapData
    reports.value = reportData
    tasks.value = taskData
    warnings.value = warningData
    warningRules.value = ruleData
    logs.value = logData
    notifications.value = notificationData
    announcements.value = announcementData
    if (!newDevice.areaName && areaData.length) newDevice.areaName = areaData[0].name
    if (!newReport.areaName && areaData.length) newReport.areaName = areaData[0].name
    if (!publicReport.areaName && areaData.length) publicReport.areaName = areaData[0].name
    if (!newWarning.areaName && areaData.length) newWarning.areaName = areaData[0].name
    if (!visibleNavItems.value.some((item) => item.key === activeModule.value)) {
      activeModule.value = 'dashboard'
    }
  } catch (error) {
    showToast(error.message)
  } finally {
    loading.value = false
  }
}

async function loadIf(condition, path, fallback = []) {
  return condition ? request(path) : fallback
}

async function createArea() {
  await submit('/api/areas', newArea, () => {
    Object.assign(newArea, { name: '', code: '', parentId: '', manager: '', riskLevel: '中', address: '' })
  })
}

async function createDevice() {
  await submit('/api/devices', newDevice, () => {
    Object.assign(newDevice, {
      name: '',
      code: '',
      type: '空气微站',
      status: '在线',
      lastOnline: '',
      pm25: 0,
      voc: 0,
    })
  })
}

async function createReport() {
  await submit('/api/reports', newReport, () => {
    Object.assign(newReport, {
      reporterName: '',
      phone: '',
      location: '',
      category: '异味扰民',
      description: '',
      source: '后台录入',
    })
  })
}

async function submitPublicReport() {
  await submit('/api/public/reports', publicReport, () => {
    Object.assign(publicReport, {
      reporterName: '',
      phone: '',
      location: '',
      category: '异味扰民',
      description: '',
    })
  })
}

async function createWarning() {
  await submit('/api/warnings', newWarning, () => {
    Object.assign(newWarning, { title: '', level: '中', sourceType: '设备监测', advice: '' })
  })
}

async function submit(path, form, reset) {
  loading.value = true
  try {
    const body = { ...form }
    if (body.parentId === '') body.parentId = null
    await request(path, { method: 'POST', body })
    reset()
    await loadAll()
    showToast('保存成功')
  } catch (error) {
    showToast(error.message)
  } finally {
    loading.value = false
  }
}

async function updateReportStatus(report, status) {
  await request(`/api/reports/${report.id}/status`, {
    method: 'PATCH',
    body: { status, handler: currentUser.value?.realName || '系统管理员' },
  })
  await loadAll()
  showToast('监督数据状态已更新')
}

async function updateWarningStatus(item, status) {
  await request(`/api/warnings/${item.id}/status`, { method: 'PATCH', body: { status } })
  await loadAll()
  showToast('预警状态已更新')
}

async function updateDeviceStatus(item, status) {
  await request(`/api/devices/${item.id}/status`, { method: 'PATCH', body: { status } })
  await loadAll()
  showToast('设备状态已更新')
}

function openDispatchTask(report) {
  selectedDispatchReport.value = report
  Object.assign(newTask, {
    reportId: report.id,
    title: `${report.areaName}${report.category}现场核查`,
    assignee: currentUser.value?.realName || '执法监督员',
    deadline: '',
    priority: report.status === '待受理' ? '中' : '高',
  })
}

async function createTask() {
  if (!newTask.deadline) {
    showToast('请选择任务期限')
    return
  }
  await request('/api/tasks', { method: 'POST', body: newTask })
  selectedDispatchReport.value = null
  await loadAll()
  showToast('任务已派发')
}

async function updateTaskStatus(task, status) {
  await request(`/api/tasks/${task.id}/status`, {
    method: 'PATCH',
    body: { status, handler: currentUser.value?.realName || '系统管理员' },
  })
  await loadAll()
  showToast('任务状态已更新')
}

async function checkOverdueTasks() {
  const count = await request('/api/tasks/check-overdue', { method: 'POST' })
  await loadAll()
  showToast(`已生成 ${count} 条逾期预警`)
}

async function queryPublicProgress() {
  const params = new URLSearchParams()
  if (publicQuery.reportId) params.set('reportId', publicQuery.reportId)
  if (publicQuery.phone) params.set('phone', publicQuery.phone)
  if (!params.toString()) {
    showToast('请输入举报编号或手机号')
    return
  }
  publicProgressList.value = await request(`/api/public/progress?${params}`)
  showToast(`查询到 ${publicProgressList.value.length} 条办理记录`)
}

async function exportCsv(type) {
  const response = await fetch(`/api/exports/${type}`, {
    headers: token.value ? { Authorization: `Bearer ${token.value}` } : {},
  })
  if (!response.ok) {
    showToast('导出失败')
    return
  }
  const blob = await response.blob()
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `${type}-${Date.now()}.csv`
  link.click()
  URL.revokeObjectURL(url)
}

async function openReportDetail(item) {
  selectedReportDetail.value = await request(`/api/reports/${item.id}`)
  Object.assign(newRecord, { action: '现场核查', remark: '' })
}

async function addReportRecord() {
  if (!selectedReportDetail.value || !newRecord.remark.trim()) {
    showToast('请填写处理说明')
    return
  }
  await request(`/api/reports/${selectedReportDetail.value.report.id}/records`, {
    method: 'POST',
    body: {
      action: newRecord.action,
      operator: currentUser.value?.realName || '系统管理员',
      remark: newRecord.remark,
    },
  })
  await openReportDetail(selectedReportDetail.value.report)
  await loadAll()
  showToast('处理记录已追加')
}

function handleAttachmentChange(event) {
  attachmentFile.value = event.target.files?.[0] || null
}

async function uploadAttachment() {
  if (!selectedReportDetail.value || !attachmentFile.value) {
    showToast('请选择要上传的图片')
    return
  }
  const formData = new FormData()
  formData.append('file', attachmentFile.value)
  formData.append('usageType', '现场证据')
  formData.append('uploadedBy', currentUser.value?.realName || '系统管理员')
  const response = await fetch(`/api/reports/${selectedReportDetail.value.report.id}/attachments`, {
    method: 'POST',
    headers: token.value ? { Authorization: `Bearer ${token.value}` } : {},
    body: formData,
  })
  const payload = await response.json()
  if (!response.ok || !payload.success) {
    showToast(payload.message || '上传失败')
    return
  }
  attachmentFile.value = null
  await openReportDetail(selectedReportDetail.value.report)
  showToast('附件已上传')
}

async function openDeviceTrend(item) {
  selectedDevice.value = item
  deviceReadings.value = await request(`/api/devices/${item.id}/readings`)
}

async function createRule() {
  await submit('/api/warnings/rules', newRule, () => {
    Object.assign(newRule, {
      name: '',
      metricName: 'VOC',
      operator: '>',
      threshold: 1,
      level: '中',
      enabled: true,
      advice: '',
    })
  })
}

async function toggleRule(rule) {
  await request(`/api/warnings/rules/${rule.id}/enabled`, {
    method: 'PATCH',
    body: { enabled: !rule.enabled },
  })
  await loadAll()
  showToast(rule.enabled ? '规则已停用' : '规则已启用')
}

async function generateWarnings() {
  const count = await request('/api/warnings/generate', { method: 'POST' })
  await loadAll()
  showToast(`已按规则生成 ${count} 条预警`)
}

async function markNotificationRead(item) {
  if (item.readFlag) return
  await request(`/api/notifications/${item.id}/read`, { method: 'PATCH' })
  await loadAll()
  showToast('消息已标记为已读')
}

function barWidth(value, list) {
  const max = Math.max(...list.map((item) => statValue(item)), 1)
  return `${Math.max((Number(value) / max) * 100, 8)}%`
}

function statValue(item) {
  return Number(item.countValue ?? item.count_value ?? item.value ?? 0)
}

function chartPoints(list) {
  if (!list.length) return ''
  const values = list.map((item) => Number(item.metricValue))
  const max = Math.max(...values)
  const min = Math.min(...values)
  const span = max - min || 1
  return values
    .map((value, index) => {
      const x = list.length === 1 ? 50 : (index / (list.length - 1)) * 100
      const y = 88 - ((value - min) / span) * 72
      return `${x},${y}`
    })
    .join(' ')
}

function pointClass(point) {
  return [
    'map-point',
    point.pointType === '设备' ? 'device' : point.pointType === '举报' ? 'report' : 'warning',
    point.riskLevel === '高' ? 'high' : '',
  ]
}

onMounted(() => {
  if (currentUser.value) loadAll()
})
</script>

<template>
  <main v-if="!currentUser" class="login-screen">
    <section class="login-panel">
      <div>
        <p class="eyebrow">环保公众监督系统</p>
        <h1>环境问题闭环监督平台</h1>
        <p class="login-copy">覆盖公众举报、区域网格、设备接入、监督预警和分析研判。</p>
      </div>
      <form class="login-form" @submit.prevent="login">
        <label>
          用户名
          <input v-model="loginForm.username" autocomplete="username" />
        </label>
        <label>
          密码
          <input v-model="loginForm.password" type="password" autocomplete="current-password" />
        </label>
        <button type="submit" :disabled="loading">{{ loading ? '登录中' : '登录系统' }}</button>
        <p class="hint">演示账号：admin / 123456</p>
      </form>
    </section>
  </main>

  <div v-else class="app-shell">
    <aside class="sidebar">
      <div class="brand">
        <span class="brand-mark">环</span>
        <div>
          <strong>公众监督</strong>
          <small>Environmental Supervision</small>
        </div>
      </div>
      <nav>
        <button
          v-for="item in visibleNavItems"
          :key="item.key"
          :class="{ active: activeModule === item.key }"
          @click="activeModule = item.key"
        >
          {{ item.label }}
        </button>
      </nav>
    </aside>

    <section class="workspace">
      <header class="topbar">
        <div>
          <p class="eyebrow">NEUSOFT 环保行业解决方案</p>
          <h1>{{ moduleTitle }}</h1>
        </div>
        <div class="user-box">
          <span>{{ currentUser.realName }} · {{ currentUser.role }}</span>
          <span class="status">{{ currentRole }}</span>
          <button class="ghost badge-button" type="button" @click="activeModule = 'notifications'">
            消息 {{ unreadNotifications }}
          </button>
          <button class="ghost" type="button" @click="loadAll">刷新</button>
          <button class="ghost danger" type="button" @click="logout">退出</button>
        </div>
      </header>

      <section v-if="activeModule === 'dashboard'" class="module">
        <div class="metric-grid">
          <article class="metric-card">
            <span>区域网格</span>
            <strong>{{ overview.areas || 0 }}</strong>
            <small>市区街道分级监管</small>
          </article>
          <article class="metric-card">
            <span>接入设备</span>
            <strong>{{ overview.devices || 0 }}</strong>
            <small>在线率 {{ onlineRate }}%</small>
          </article>
          <article class="metric-card">
            <span>监督线索</span>
            <strong>{{ overview.reports || 0 }}</strong>
            <small>{{ pendingReports }} 条待闭环</small>
          </article>
          <article class="metric-card alert">
            <span>有效预警</span>
            <strong>{{ overview.activeWarnings || 0 }}</strong>
            <small>{{ unresolvedWarnings }} 条需关注</small>
          </article>
        </div>

        <div class="split">
          <section class="panel">
            <h2>最新监督线索</h2>
            <div class="timeline">
              <article v-for="item in reports.slice(0, 4)" :key="item.id">
                <strong>{{ item.category }} · {{ item.areaName }}</strong>
                <span>{{ item.createTime }} / {{ item.status }}</span>
                <p>{{ item.description }}</p>
              </article>
            </div>
          </section>
          <section class="panel">
            <h2>预警处置</h2>
            <div class="timeline">
              <article v-for="item in warnings.slice(0, 4)" :key="item.id">
                <strong>{{ item.title }}</strong>
                <span>{{ item.createdAt }} / {{ item.level }}风险</span>
                <p>{{ item.advice }}</p>
              </article>
            </div>
          </section>
        </div>
      </section>

      <section v-if="activeModule === 'users'" class="module">
        <div class="panel">
          <h2>用户权限列表</h2>
          <table>
            <thead>
              <tr>
                <th>账号</th>
                <th>姓名</th>
                <th>角色</th>
                <th>区域</th>
                <th>状态</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in users" :key="item.id">
                <td>{{ item.username }}</td>
                <td>{{ item.realName }}</td>
                <td>{{ item.role }}</td>
                <td>{{ item.areaName }}</td>
                <td><span class="status good">{{ item.status }}</span></td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section v-if="activeModule === 'areas'" class="module">
        <form v-if="canAdmin" class="toolbar-form" @submit.prevent="createArea">
          <input v-model="newArea.name" placeholder="区域名称" required />
          <input v-model="newArea.code" placeholder="区域编码" required />
          <input v-model="newArea.manager" placeholder="责任单位" />
          <select v-model="newArea.riskLevel">
            <option>低</option>
            <option>中</option>
            <option>高</option>
          </select>
          <input v-model="newArea.address" placeholder="地址" />
          <button type="submit">新增区域</button>
        </form>
        <div class="panel">
          <h2>区域网格</h2>
          <table>
            <thead>
              <tr>
                <th>名称</th>
                <th>编码</th>
                <th>责任单位</th>
                <th>风险</th>
                <th>地址</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in areas" :key="item.id">
                <td>{{ item.name }}</td>
                <td>{{ item.code }}</td>
                <td>{{ item.manager }}</td>
                <td><span :class="['status', item.riskLevel === '高' ? 'bad' : '']">{{ item.riskLevel }}</span></td>
                <td>{{ item.address }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section v-if="activeModule === 'devices'" class="module">
        <form v-if="canAdmin" class="toolbar-form" @submit.prevent="createDevice">
          <input v-model="newDevice.name" placeholder="设备名称" required />
          <input v-model="newDevice.code" placeholder="设备编码" required />
          <select v-model="newDevice.type">
            <option>空气微站</option>
            <option>VOC 监测</option>
            <option>水质监测</option>
            <option>噪声监测</option>
          </select>
          <select v-model="newDevice.areaName">
            <option v-for="area in areas" :key="area.id">{{ area.name }}</option>
          </select>
          <select v-model="newDevice.status">
            <option>在线</option>
            <option>离线</option>
            <option>维护中</option>
          </select>
          <button type="submit">接入设备</button>
        </form>
        <div class="panel">
          <h2>设备运行状态</h2>
          <table>
            <thead>
              <tr>
                <th>设备</th>
                <th>类型</th>
                <th>区域</th>
                <th>状态</th>
                <th>PM2.5</th>
                <th>VOC</th>
                <th v-if="canAdmin">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in devices" :key="item.id">
                <td>{{ item.name }}<small>{{ item.code }}</small></td>
                <td>{{ item.type }}</td>
                <td>{{ item.areaName }}</td>
                <td><span :class="['status', item.status === '在线' ? 'good' : item.status === '离线' ? 'bad' : '']">{{ item.status }}</span></td>
                <td>{{ item.pm25 }}</td>
                <td>{{ item.voc }}</td>
                <td v-if="canAdmin" class="actions">
                  <button type="button" @click="updateDeviceStatus(item, '在线')">上线</button>
                  <button type="button" @click="updateDeviceStatus(item, '维护中')">维护</button>
                  <button type="button" @click="openDeviceTrend(item)">趋势</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <section v-if="selectedDevice" class="panel">
          <h2>{{ selectedDevice.name }} 历史趋势</h2>
          <div class="trend-wrap">
            <svg viewBox="0 0 100 100" preserveAspectRatio="none">
              <polyline :points="chartPoints(deviceReadings)" />
            </svg>
            <div class="reading-list">
              <span v-for="item in deviceReadings" :key="item.id">
                {{ item.recordedAt.slice(11) }} · {{ item.metricName }} {{ item.metricValue }}{{ item.unit }}
              </span>
            </div>
          </div>
        </section>
      </section>

      <section v-if="activeModule === 'map'" class="module">
        <section class="panel">
          <div class="panel-head">
            <h2>监管点位示意图</h2>
            <div class="map-legend">
              <span><i class="device-dot"></i>设备</span>
              <span><i class="report-dot"></i>举报</span>
              <span><i class="warning-dot"></i>预警</span>
            </div>
          </div>
          <div class="map-board">
            <div class="map-region north">沈北新区</div>
            <div class="map-region center">和平区</div>
            <div class="map-region south">浑南区</div>
            <button
              v-for="point in mapPoints"
              :key="point.id"
              :class="pointClass(point)"
              :style="{ left: `${point.xPercent}%`, top: `${point.yPercent}%` }"
              type="button"
              :title="`${point.name}：${point.description}`"
            >
              <span>{{ point.pointType.slice(0, 1) }}</span>
              <strong>{{ point.name }}</strong>
            </button>
          </div>
        </section>
        <div class="warning-grid">
          <article v-for="point in mapPoints" :key="point.id" class="warning-card">
            <div>
              <span :class="['level', point.riskLevel === '高' ? 'high' : point.riskLevel === '中' ? 'mid' : 'low']">
                {{ point.pointType }}
              </span>
              <strong>{{ point.name }}</strong>
            </div>
            <p>{{ point.description }}</p>
            <footer>
              <span>{{ point.areaName }} · {{ point.status }}</span>
              <span>{{ point.riskLevel }}风险</span>
            </footer>
          </article>
        </div>
      </section>

      <section v-if="activeModule === 'reports'" class="module">
        <form v-if="canManage" class="toolbar-form wide" @submit.prevent="createReport">
          <input v-model="newReport.reporterName" placeholder="举报人" required />
          <input v-model="newReport.phone" placeholder="联系电话" />
          <select v-model="newReport.areaName">
            <option v-for="area in areas" :key="area.id">{{ area.name }}</option>
          </select>
          <select v-model="newReport.category">
            <option>异味扰民</option>
            <option>扬尘污染</option>
            <option>污水排放</option>
            <option>噪声扰民</option>
          </select>
          <input v-model="newReport.location" placeholder="问题地点" required />
          <input v-model="newReport.description" placeholder="问题描述" required />
          <button type="submit">录入线索</button>
        </form>
        <form class="filter-form" @submit.prevent>
          <input v-model="reportFilters.keyword" placeholder="搜索举报人、地点、描述" />
          <select v-model="reportFilters.areaName">
            <option value="">全部区域</option>
            <option v-for="area in areas" :key="area.id">{{ area.name }}</option>
          </select>
          <select v-model="reportFilters.category">
            <option value="">全部类型</option>
            <option>异味扰民</option>
            <option>扬尘污染</option>
            <option>污水排放</option>
            <option>噪声扰民</option>
          </select>
          <select v-model="reportFilters.status">
            <option value="">全部状态</option>
            <option>待受理</option>
            <option>处理中</option>
            <option>已办结</option>
          </select>
          <button type="button" @click="exportCsv('reports')">导出举报台账</button>
        </form>
        <div class="panel">
          <h2>监督数据台账</h2>
          <table>
            <thead>
              <tr>
                <th>类别</th>
                <th>区域地点</th>
                <th>描述</th>
                <th>来源</th>
                <th>状态</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in filteredReports" :key="item.id">
                <td>{{ item.category }}<small>{{ item.reporterName }} {{ item.phone }}</small></td>
                <td>{{ item.areaName }}<small>{{ item.location }}</small></td>
                <td>{{ item.description }}</td>
                <td>{{ item.source }}</td>
                <td><span :class="['status', item.status === '已办结' ? 'good' : item.status === '待受理' ? 'bad' : '']">{{ item.status }}</span></td>
                <td class="actions">
                  <button type="button" @click="openReportDetail(item)">详情</button>
                  <button v-if="canManage" type="button" @click="openDispatchTask(item)">派单</button>
                  <button v-if="canManage" type="button" @click="updateReportStatus(item, '处理中')">受理</button>
                  <button v-if="canManage" type="button" @click="updateReportStatus(item, '已办结')">办结</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section v-if="activeModule === 'tasks'" class="module">
        <div class="metric-grid">
          <article class="metric-card alert">
            <span>逾期任务</span>
            <strong>{{ overdueTasks.length }}</strong>
            <small>未完成且超过截止时间</small>
          </article>
          <article class="metric-card">
            <span>任务总数</span>
            <strong>{{ tasks.length }}</strong>
            <small>派单台账累计</small>
          </article>
          <article class="metric-card">
            <span>已完成</span>
            <strong>{{ tasks.filter((item) => item.status === '已完成').length }}</strong>
            <small>任务闭环数量</small>
          </article>
          <article class="metric-card">
            <span>待跟进</span>
            <strong>{{ tasks.filter((item) => item.status !== '已完成').length }}</strong>
            <small>包含处理中和逾期</small>
          </article>
        </div>
        <form class="toolbar-form wide" @submit.prevent="createTask">
          <select v-model="newTask.reportId" required>
            <option value="">选择关联举报</option>
            <option v-for="report in reports" :key="report.id" :value="report.id">
              #{{ report.id }} {{ report.areaName }} {{ report.category }}
            </option>
          </select>
          <input v-model="newTask.title" placeholder="任务标题" required />
          <input v-model="newTask.assignee" placeholder="负责人" required />
          <input v-model="newTask.deadline" type="datetime-local" required />
          <select v-model="newTask.priority">
            <option>低</option>
            <option>中</option>
            <option>高</option>
          </select>
          <button type="submit">创建任务</button>
          <button type="button" @click="exportCsv('tasks')">导出任务台账</button>
          <button type="button" @click="checkOverdueTasks">逾期检查</button>
        </form>
        <div class="task-board">
          <article v-for="task in tasks" :key="task.id" class="task-card">
            <header>
              <span>{{ task.taskNo }}</span>
              <strong>{{ task.title }}</strong>
            </header>
            <p>负责人：{{ task.assignee }} / 截止：{{ task.deadline }}</p>
            <footer>
              <span :class="['status', task.status === '已完成' ? 'good' : task.priority === '高' ? 'bad' : '']">
                {{ task.priority }} · {{ task.status }}
              </span>
              <div class="actions">
                <button type="button" @click="updateTaskStatus(task, '处理中')">开始</button>
                <button type="button" @click="updateTaskStatus(task, '已完成')">完成</button>
              </div>
            </footer>
          </article>
        </div>
      </section>

      <section v-if="activeModule === 'logs'" class="module">
        <div class="panel">
          <div class="panel-head">
            <h2>操作日志</h2>
            <button type="button" @click="loadAll">刷新日志</button>
          </div>
          <table>
            <thead>
              <tr>
                <th>时间</th>
                <th>操作人</th>
                <th>模块</th>
                <th>动作</th>
                <th>对象</th>
                <th>说明</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in logs" :key="item.id">
                <td>{{ item.createdAt }}</td>
                <td>{{ item.operator }}</td>
                <td>{{ item.module }}</td>
                <td>{{ item.action }}</td>
                <td>{{ item.target }}</td>
                <td>{{ item.detail }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section v-if="activeModule === 'notifications'" class="module">
        <div class="metric-grid">
          <article class="metric-card alert">
            <span>未读消息</span>
            <strong>{{ unreadNotifications }}</strong>
            <small>需要及时处理的系统提醒</small>
          </article>
          <article class="metric-card">
            <span>消息总数</span>
            <strong>{{ notifications.length }}</strong>
            <small>举报、任务、预警自动汇总</small>
          </article>
          <article class="metric-card">
            <span>预警提醒</span>
            <strong>{{ notifications.filter((item) => item.type === 'WARNING').length }}</strong>
            <small>规则引擎和逾期检查生成</small>
          </article>
          <article class="metric-card">
            <span>任务提醒</span>
            <strong>{{ notifications.filter((item) => item.type === 'TASK').length }}</strong>
            <small>派单后自动推送</small>
          </article>
        </div>
        <section class="panel">
          <div class="panel-head">
            <h2>系统消息中心</h2>
            <button type="button" @click="loadAll">刷新消息</button>
          </div>
          <div class="notification-list">
            <article
              v-for="item in notifications"
              :key="item.id"
              :class="{ unread: !item.readFlag }"
            >
              <div>
                <span>{{ item.type }}</span>
                <strong>{{ item.title }}</strong>
                <p>{{ item.content }}</p>
                <small>{{ item.createdAt }}</small>
              </div>
              <button type="button" :disabled="item.readFlag" @click="markNotificationRead(item)">
                {{ item.readFlag ? '已读' : '标记已读' }}
              </button>
            </article>
          </div>
        </section>
      </section>

      <section v-if="activeModule === 'analysis'" class="module">
        <div class="split">
          <section class="panel">
            <h2>举报类型分布</h2>
            <div class="bar-list">
              <div v-for="item in overview.categoryStats || []" :key="item.name" class="bar-row">
                <span>{{ item.name }}</span>
                <div><i :style="{ width: barWidth(statValue(item), overview.categoryStats || []) }"></i></div>
                <strong>{{ statValue(item) }}</strong>
              </div>
            </div>
          </section>
          <section class="panel">
            <h2>状态闭环分析</h2>
            <div class="bar-list">
              <div v-for="item in overview.statusStats || []" :key="item.name" class="bar-row">
                <span>{{ item.name }}</span>
                <div><i :style="{ width: barWidth(statValue(item), overview.statusStats || []) }"></i></div>
                <strong>{{ statValue(item) }}</strong>
              </div>
            </div>
          </section>
        </div>
        <section class="panel">
          <h2>区域问题热度</h2>
          <div class="bar-list">
            <div v-for="item in overview.areaStats || []" :key="item.name" class="bar-row">
              <span>{{ item.name }}</span>
              <div><i :style="{ width: barWidth(statValue(item), overview.areaStats || []) }"></i></div>
              <strong>{{ statValue(item) }}</strong>
            </div>
          </div>
        </section>
      </section>

      <section v-if="activeModule === 'warnings'" class="module">
        <form v-if="canManage" class="toolbar-form wide" @submit.prevent="createWarning">
          <input v-model="newWarning.title" placeholder="预警标题" required />
          <select v-model="newWarning.level">
            <option>低</option>
            <option>中</option>
            <option>高</option>
          </select>
          <select v-model="newWarning.areaName">
            <option v-for="area in areas" :key="area.id">{{ area.name }}</option>
          </select>
          <select v-model="newWarning.sourceType">
            <option>设备监测</option>
            <option>举报聚合</option>
            <option>人工研判</option>
          </select>
          <input v-model="newWarning.advice" placeholder="处置建议" />
          <button type="submit">新增预警</button>
        </form>
        <form v-if="canAdmin" class="toolbar-form wide" @submit.prevent="createRule">
          <input v-model="newRule.name" placeholder="规则名称" required />
          <select v-model="newRule.metricName">
            <option>VOC</option>
            <option>PM2.5</option>
            <option>噪声</option>
            <option>水质指数</option>
          </select>
          <select v-model="newRule.operator">
            <option>&gt;</option>
            <option>&gt;=</option>
            <option>&lt;</option>
            <option>&lt;=</option>
            <option>=</option>
          </select>
          <input v-model.number="newRule.threshold" type="number" step="0.01" placeholder="阈值" required />
          <select v-model="newRule.level">
            <option>低</option>
            <option>中</option>
            <option>高</option>
          </select>
          <input v-model="newRule.advice" placeholder="自动预警建议" />
          <button type="submit">新增规则</button>
        </form>
        <section class="panel">
          <div class="panel-head">
            <h2>预警规则</h2>
            <div class="actions">
              <button v-if="canManage" type="button" @click="generateWarnings">按规则生成预警</button>
              <button type="button" @click="exportCsv('warnings')">导出预警台账</button>
            </div>
          </div>
          <div class="rule-list">
            <article v-for="rule in warningRules" :key="rule.id">
              <strong>{{ rule.name }}</strong>
              <span>{{ rule.metricName }} {{ rule.operator }} {{ rule.threshold }} / {{ rule.level }}风险</span>
              <button type="button" :class="{ muted: !rule.enabled }" @click="toggleRule(rule)">
                {{ rule.enabled ? '启用中' : '已停用' }}
              </button>
            </article>
          </div>
        </section>
        <div class="warning-grid">
          <article v-for="item in warnings" :key="item.id" class="warning-card">
            <div>
              <span :class="['level', item.level === '高' ? 'high' : item.level === '中' ? 'mid' : 'low']">{{ item.level }}</span>
              <strong>{{ item.title }}</strong>
            </div>
            <p>{{ item.advice }}</p>
            <footer>
              <span>{{ item.areaName }} · {{ item.sourceType }}</span>
              <button type="button" @click="updateWarningStatus(item, item.status === '已处置' ? '处理中' : '已处置')">
                {{ item.status === '已处置' ? '重新处置' : '标记处置' }}
              </button>
            </footer>
          </article>
        </div>
      </section>

      <section v-if="activeModule === 'public'" class="module">
        <div class="split">
          <section class="panel">
            <h2>公众公告</h2>
            <div class="timeline">
              <article v-for="item in announcements" :key="item.id">
                <strong>{{ item.title }}</strong>
                <span>{{ item.publishedAt }}</span>
                <p>{{ item.content }}</p>
              </article>
            </div>
          </section>
          <form class="public-form" @submit.prevent="submitPublicReport">
            <h2>公众举报模拟入口</h2>
            <input v-model="publicReport.reporterName" placeholder="姓名" required />
            <input v-model="publicReport.phone" placeholder="联系电话" />
            <select v-model="publicReport.areaName">
              <option v-for="area in areas" :key="area.id">{{ area.name }}</option>
            </select>
            <select v-model="publicReport.category">
              <option>异味扰民</option>
              <option>扬尘污染</option>
              <option>污水排放</option>
              <option>噪声扰民</option>
            </select>
            <input v-model="publicReport.location" placeholder="问题地点" required />
            <textarea v-model="publicReport.description" placeholder="请描述环境问题" required></textarea>
            <button type="submit">提交举报</button>
          </form>
        </div>
        <section class="panel">
          <div class="panel-head">
            <h2>办理进度查询</h2>
          </div>
          <form class="filter-form" @submit.prevent="queryPublicProgress">
            <input v-model="publicQuery.reportId" placeholder="举报编号，例如 1" />
            <input v-model="publicQuery.phone" placeholder="手机号，例如 13800000001" />
            <button type="submit">查询进度</button>
          </form>
          <div class="progress-list">
            <article v-for="item in publicProgressList" :key="item.report.id">
              <header>
                <strong>#{{ item.report.id }} {{ item.report.category }} · {{ item.report.status }}</strong>
                <span>{{ item.report.areaName }} / {{ item.report.location }}</span>
              </header>
              <div class="record-flow compact">
                <article v-for="record in item.records" :key="record.id">
                  <i></i>
                  <div>
                    <strong>{{ record.action }}</strong>
                    <span>{{ record.createdAt }} · {{ record.operator }}</span>
                    <p>{{ record.remark }}</p>
                  </div>
                </article>
              </div>
              <div v-if="item.tasks.length" class="mini-task-list">
                <span v-for="task in item.tasks" :key="task.id">
                  {{ task.taskNo }} / {{ task.assignee }} / {{ task.status }}
                </span>
              </div>
            </article>
          </div>
        </section>
      </section>
    </section>

    <div v-if="selectedDispatchReport" class="modal-backdrop" @click.self="selectedDispatchReport = null">
      <section class="modal compact-modal">
        <header>
          <div>
            <p class="eyebrow">任务派单</p>
            <h2>#{{ selectedDispatchReport.id }} {{ selectedDispatchReport.category }}</h2>
          </div>
          <button class="ghost" type="button" @click="selectedDispatchReport = null">关闭</button>
        </header>
        <p class="detail-desc">{{ selectedDispatchReport.areaName }} {{ selectedDispatchReport.location }}：{{ selectedDispatchReport.description }}</p>
        <form class="record-form dispatch-form" @submit.prevent="createTask">
          <input v-model="newTask.title" placeholder="任务标题" required />
          <input v-model="newTask.assignee" placeholder="负责人" required />
          <input v-model="newTask.deadline" type="datetime-local" required />
          <select v-model="newTask.priority">
            <option>低</option>
            <option>中</option>
            <option>高</option>
          </select>
          <button type="submit">确认派单</button>
        </form>
      </section>
    </div>

    <div v-if="selectedReportDetail" class="modal-backdrop" @click.self="selectedReportDetail = null">
      <section class="modal">
        <header>
          <div>
            <p class="eyebrow">举报详情</p>
            <h2>{{ selectedReportDetail.report.category }} · {{ selectedReportDetail.report.areaName }}</h2>
          </div>
          <button class="ghost" type="button" @click="selectedReportDetail = null">关闭</button>
        </header>
        <div class="detail-grid">
          <article>
            <span>举报人</span>
            <strong>{{ selectedReportDetail.report.reporterName }}</strong>
          </article>
          <article>
            <span>联系电话</span>
            <strong>{{ selectedReportDetail.report.phone || '未填写' }}</strong>
          </article>
          <article>
            <span>问题地点</span>
            <strong>{{ selectedReportDetail.report.location }}</strong>
          </article>
          <article>
            <span>当前状态</span>
            <strong>{{ selectedReportDetail.report.status }}</strong>
          </article>
        </div>
        <p class="detail-desc">{{ selectedReportDetail.report.description }}</p>
        <div class="record-flow">
          <article v-for="record in selectedReportDetail.records" :key="record.id">
            <i></i>
            <div>
              <strong>{{ record.action }}</strong>
              <span>{{ record.createdAt }} · {{ record.operator }}</span>
              <p>{{ record.remark }}</p>
            </div>
          </article>
        </div>
        <div v-if="selectedReportDetail.tasks.length" class="mini-task-list detail-tasks">
          <span v-for="task in selectedReportDetail.tasks" :key="task.id">
            {{ task.taskNo }} / {{ task.assignee }} / {{ task.deadline }} / {{ task.status }}
          </span>
        </div>
        <section class="attachment-panel">
          <div class="panel-head">
            <h2>证据附件</h2>
          </div>
          <div v-if="selectedReportDetail.attachments.length" class="attachment-grid">
            <a
              v-for="file in selectedReportDetail.attachments"
              :key="file.id"
              :href="file.filePath"
              target="_blank"
              rel="noreferrer"
            >
              <img v-if="file.contentType.startsWith('image/')" :src="file.filePath" :alt="file.fileName" />
              <span v-else>{{ file.fileName }}</span>
              <small>{{ file.usageType }} · {{ file.uploadedAt }}</small>
            </a>
          </div>
          <p v-else class="empty-text">暂无附件，可上传现场照片或整改照片。</p>
          <form class="record-form" @submit.prevent="uploadAttachment">
            <input type="file" accept="image/*" @change="handleAttachmentChange" />
            <button type="submit">上传附件</button>
          </form>
        </section>
        <form class="record-form" @submit.prevent="addReportRecord">
          <select v-model="newRecord.action">
            <option>现场核查</option>
            <option>派单处理</option>
            <option>整改反馈</option>
            <option>回访确认</option>
          </select>
          <input v-model="newRecord.remark" placeholder="填写本次处理说明" required />
          <button type="submit">追加记录</button>
        </form>
      </section>
    </div>

    <div v-if="toast" class="toast">{{ toast }}</div>
  </div>
</template>
