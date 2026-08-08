// pages/index/index.js
const app = getApp()

Page({
  data: {
    categories: [
      { id: '', name: '全部' },
      { id: 'cleaning', name: '日常保洁' },
      { id: 'repair', name: '家电维修' },
      { id: 'moving', name: '搬家服务' }
    ],
    currentCategory: '',
    serviceList: [],
    loading: false
  },

  onLoad() {
    this.loadServices()
  },

  onPullDownRefresh() {
    this.loadServices()
  },

  // 切换分类
  switchCategory(e) {
    const category = e.currentTarget.dataset.id
    this.setData({ currentCategory: category })
    this.loadServices()
  },

  // 加载服务列表
  loadServices() {
    this.setData({ loading: true })
    const params = this.data.currentCategory ? `?category=${this.data.currentCategory}` : ''

    wx.request({
      url: `${app.globalData.baseUrl}/api/service/list${params}`,
      method: 'GET',
      success: (res) => {
        if (res.data.code === 200) {
          this.setData({ serviceList: res.data.data })
        }
      },
      fail: () => {
        wx.showToast({ title: '加载失败', icon: 'none' })
      },
      complete: () => {
        this.setData({ loading: false })
        wx.stopPullDownRefresh()
      }
    })
  },

  // 跳转到详情页
  goDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({
      url: `/pages/detail/detail?id=${id}`
    })
  }
})
