// pages/detail/detail.js
const app = getApp()

Page({
  data: {
    service: null,
    contactName: '',
    contactPhone: '',
    address: '',
    appointmentTime: ''
  },

  onLoad(options) {
    if (options.id) {
      this.loadDetail(options.id)
    }
  },

  // 加载服务详情
  loadDetail(id) {
    wx.request({
      url: `${app.globalData.baseUrl}/api/service/detail/${id}`,
      method: 'GET',
      success: (res) => {
        if (res.data.code === 200) {
          this.setData({ service: res.data.data })
        }
      }
    })
  },

  // 表单输入
  onInputName(e) {
    this.setData({ contactName: e.detail.value })
  },
  onInputPhone(e) {
    this.setData({ contactPhone: e.detail.value })
  },
  onInputAddress(e) {
    this.setData({ address: e.detail.value })
  },
  onDateChange(e) {
    this.setData({ appointmentTime: e.detail.value })
  },

  // 提交订单
  submitOrder() {
    const { service, contactName, contactPhone, address, appointmentTime } = this.data

    // 表单验证
    if (!contactName) {
      wx.showToast({ title: '请输入联系人', icon: 'none' })
      return
    }
    if (!contactPhone || !/^1\d{10}$/.test(contactPhone)) {
      wx.showToast({ title: '请输入正确的手机号', icon: 'none' })
      return
    }
    if (!address) {
      wx.showToast({ title: '请输入服务地址', icon: 'none' })
      return
    }
    if (!appointmentTime) {
      wx.showToast({ title: '请选择预约时间', icon: 'none' })
      return
    }

    const openId = wx.getStorageSync('openId')
    if (!openId) {
      wx.showToast({ title: '请先登录', icon: 'none' })
      return
    }

    wx.showLoading({ title: '提交中...' })

    wx.request({
      url: `${app.globalData.baseUrl}/api/order/create`,
      method: 'POST',
      data: {
        openId: openId,
        serviceId: service.id,
        contactName: contactName,
        contactPhone: contactPhone,
        address: address,
        appointmentTime: appointmentTime + 'T00:00:00'
      },
      success: (res) => {
        wx.hideLoading()
        if (res.data.code === 200) {
          wx.showToast({ title: '下单成功', icon: 'success' })
          setTimeout(() => {
            wx.switchTab({ url: '/pages/order/order' })
          }, 1500)
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
})
