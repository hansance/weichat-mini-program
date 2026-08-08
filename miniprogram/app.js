// app.js
App({
  onLaunch() {
    this.login()
  },

  login() {
    wx.login({
      success: (res) => {
        if (res.code) {
          wx.request({
            url: `${this.globalData.baseUrl}/api/auth/login`,
            method: 'POST',
            data: { code: res.code },
            success: (loginRes) => {
              if (loginRes.data.code === 200) {
                this.globalData.openId = loginRes.data.data.openId
                this.globalData.token = loginRes.data.data.token
                wx.setStorageSync('openId', loginRes.data.data.openId)
                wx.setStorageSync('token', loginRes.data.data.token)
                console.log('登录成功, openId =', loginRes.data.data.openId)
              } else {
                console.error('登录失败:', loginRes.data.message)
              }
            },
            fail: (err) => {
              console.error('登录请求失败:', err)
            }
          })
        }
      }
    })
  },

  globalData: {
    baseUrl: 'http://localhost:8080', // 开发环境后端地址
    openId: '',
    token: ''
  }
})
