// pages/order/order.js
const app = getApp()

Page({
  data: {
    orderList: [],
    statusMap: {
      0: '待支付',
      1: '待服务',
      2: '服务中',
      3: '已完成',
      4: '已取消'
    },
    statusColorMap: {
      0: '#f39c12',
      1: '#4A90D9',
      2: '#27ae60',
      3: '#95a5a6',
      4: '#e74c3c'
    }
  },

  onShow() {
    this.loadOrders()
  },

  onPullDownRefresh() {
    this.loadOrders()
  },

  // 获取认证请求头
  getAuthHeader() {
    const token = wx.getStorageSync('token')
    return { 'Authorization': `Bearer ${token}` }
  },

  // 加载订单列表
  loadOrders() {
    const token = wx.getStorageSync('token')
    if (!token) {
      this.setData({ orderList: [] })
      return
    }

    wx.request({
      url: `${app.globalData.baseUrl}/api/order/my`,
      method: 'GET',
      header: this.getAuthHeader(),
      success: (res) => {
        if (res.data.code === 200) {
          this.setData({ orderList: res.data.data })
        }
      },
      complete: () => {
        wx.stopPullDownRefresh()
      }
    })
  },

  // 模拟支付
  payOrder(e) {
    const id = e.currentTarget.dataset.id

    wx.showModal({
      title: '确认支付',
      content: '确定要支付该订单吗？（开发环境模拟支付）',
      success: (res) => {
        if (res.confirm) {
          wx.showLoading({ title: '支付中...' })
          wx.request({
            url: `${app.globalData.baseUrl}/api/pay/mock/${id}`,
            method: 'POST',
            header: this.getAuthHeader(),
            success: (res) => {
              wx.hideLoading()
              if (res.data.code === 200) {
                wx.showToast({ title: '支付成功', icon: 'success' })
                this.loadOrders()
              } else {
                wx.showToast({ title: res.data.message, icon: 'none' })
              }
            },
            fail: () => {
              wx.hideLoading()
              wx.showToast({ title: '网络错误', icon: 'none' })
            }
          })
        }
      }
    })
  },

  // 取消订单
  cancelOrder(e) {
    const id = e.currentTarget.dataset.id

    wx.showModal({
      title: '提示',
      content: '确定要取消该订单吗？',
      success: (res) => {
        if (res.confirm) {
          wx.request({
            url: `${app.globalData.baseUrl}/api/order/cancel/${id}`,
            method: 'POST',
            header: this.getAuthHeader(),
            success: (res) => {
              if (res.data.code === 200) {
                wx.showToast({ title: '已取消', icon: 'success' })
                this.loadOrders()
              } else {
                wx.showToast({ title: res.data.message, icon: 'none' })
              }
            }
          })
        }
      }
    })
  }
})
