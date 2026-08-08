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

  // 加载订单列表
  loadOrders() {
    const openId = wx.getStorageSync('openId')
    if (!openId) {
      this.setData({ orderList: [] })
      return
    }

    wx.request({
      url: `${app.globalData.baseUrl}/api/order/my?openId=${openId}`,
      method: 'GET',
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

  // 取消订单
  cancelOrder(e) {
    const id = e.currentTarget.dataset.id
    const openId = wx.getStorageSync('openId')

    wx.showModal({
      title: '提示',
      content: '确定要取消该订单吗？',
      success: (res) => {
        if (res.confirm) {
          wx.request({
            url: `${app.globalData.baseUrl}/api/order/cancel/${id}?openId=${openId}`,
            method: 'POST',
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
