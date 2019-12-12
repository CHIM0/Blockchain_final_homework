<template>
  <div class="top">
    <div class="container">
      <div class="sidebar">
        <div class="message" :style="{'text-align': 'left'}">
          <div :style="{'font-size':'1rem'}"></div>
        </div>
        <Button class="button" @click="changeStatus('myCompany')">我的公司</Button>
        <Button class="button" @click="changeStatus('myReceipt')">我的账单</Button>
        <Button class="button" @click="changeStatus('createReceipt')">创建账单</Button>
        <Button class="button" @click="changeStatus('bankfinancing')">融资申请</Button>
        <Button class="button" @click="changeStatus('send')">转账</Button>
      </div>
      <div v-show="status==='bankfinancing'" class="bankfinancing content">
        <h1> 融资申请 </h1>
        <hr>
        <Form class="form" ref="bankFinancingContext" :model="bankFinancingContext" :rules="bankFinancingVal" :label-width="110">
          <FormItem label="账单公司名称" prop="fromCompany">
              <Input v-model="bankFinancingContext.fromCompany" placeholder="待收款账单付款公司名称"/>
          </FormItem>
          <FormItem label="金额" prop="amount">
              <Input v-model="bankFinancingContext.amount" placeholder="融资金额(整数)"/>
          </FormItem>
          <FormItem>
              <Button type="primary" @click="handleSubmit('bankFinancingContext')">确认</Button>
          </FormItem>
        </Form>
      </div>
      <div v-show="status==='myCompany'" class="myCompany content">
        <h1> 我的公司 </h1>
        <hr>
        <List item-layout="vertical" class="list">
          <ListItem :style="{'text-align':'left'}" v-for="(item, key) in companyMessage" :key="item.name">
              <ListItemMeta :title="key" :description="item" />
          </ListItem><br>
          <Button @click="loadCompanyMessage(1)">刷新</Button>
        </List>
      </div>
      <div v-show="status==='myReceipt'" class="myReceipt content">
        <h1> 我的账单 </h1>
        <hr>
        <List item-layout="vertical" class="list">
          <ListItem :style="{'text-align':'left'}" v-for="receipt in myReceipts" :key="receipt.index">
              <ListItemMeta :title="receipt.from === companyMessage['公司名称'] ? '欠债': '外债:'" :description="'欠款方:' + receipt.from + '····债主: ' + receipt.to + '····金额: ' + receipt.amount" />
              <Button v-show="receipt.from === companyMessage['公司名称']" @click="payBack(receipt.to)" :style="{position:'relative',left:'80%'}">还款</Button>
              <Button v-show="receipt.to === companyMessage['公司名称']" @click="transferReceipt(receipt.from)" :style="{position:'relative',left:'80%'}">账单转移</Button>
          </ListItem><br>
          <Button @click="loadMyReceipt(1)">刷新</Button>
        </List>
      </div>
      <div v-show="status==='createReceipt'" class="createReceipt content">
        <h1> 创建账单 </h1>
        <hr>
        <Form class="form" ref="createReceiptContext" :model="createReceiptContext" :rules="createReceiptVal" :label-width="80">
          <FormItem label="收款公司" prop="toCompany">
              <Input v-model="createReceiptContext.toCompany" placeholder="账单收款公司名称"/>
          </FormItem>
          <FormItem label="金额" prop="amount">
              <Input v-model="createReceiptContext.amount" placeholder="金额(整数)"/>
          </FormItem>
          <FormItem>
              <Button type="primary" @click="handleSubmit('createReceiptContext')">确认</Button>
          </FormItem>
        </Form>
      </div>
      <div v-show="status==='send'" class="send content">
        <h1> 转账 </h1>
        <hr>
        <Form class="form" ref="sendContext" :model="sendContext" :rules="sendVal" :label-width="80">
          <FormItem label="收款公司" prop="toCompany">
              <Input v-model="sendContext.toCompany" placeholder="收款公司名称"/>
          </FormItem>
          <FormItem label="金额" prop="amount">
              <Input v-model="sendContext.amount" placeholder="金额(整数)"/>
          </FormItem>
          <FormItem>
              <Button type="primary" @click="handleSubmit('sendContext')">确认</Button>
          </FormItem>
        </Form>
      </div>
    </div>
    <Modal
      v-model="showPayBack"
      title='还款'
      scrollable
      @on-ok="handleSubmit('payBackContext')"
      :loading="loadState"
      >
      <Form class="form" ref="payBackContext" :model="payBackContext" :rules="payBackVal" :label-width="80">
          <FormItem label="金额" prop="amount">
              <Input v-model="payBackContext.amount" placeholder="金额(整数)"/>
          </FormItem>
        </Form>
    </Modal>
    <Modal
      v-model="showTransferReceipt"
      title='账单转移'
      @on-ok="handleSubmit('transferReceiptContext')"
      scrollable
      :loading="loadState"
      >
      <Form class="form" ref="transferReceiptContext" :model="transferReceiptContext" :rules="transferReceiptVal" :label-width="80">
          <FormItem label="转移至" prop="toCompany">
              <Input v-model="transferReceiptContext.toCompany" placeholder="公司名称"/>
          </FormItem>
          <FormItem label="金额" prop="amount">
              <Input v-model="transferReceiptContext.amount" placeholder="金额(整数)"/>
          </FormItem>
        </Form>
    </Modal>
  </div>
</template>

<script>
export default {
  name: 'home',
  data: function () {
    const amountVal = (rule, value, callback) => {
      var reg = new RegExp('^[0-9]*$')
      if (!reg.test(value)) {
        callback(new Error('请输入正确的金额'))
      }
      callback()
    }
    return {
      loadState: true,
      privateKey: '',
      companyMessage: {},
      myReceipts: [],
      showTransferReceipt: false,
      showPayBack: false,
      showBlog: {
        title: '',
        content: ''
      },
      status: '',
      sendContext: {
        toCompany: '',
        amount: ''
      },
      createReceiptContext: {
        toCompany: '',
        amount: ''
      },
      bankFinancingContext: {
        fromCompany: '',
        amount: ''
      },
      transferReceiptContext: {
        toCompany: '',
        amount: ''
      },
      payBackContext: {
        amount: ''
      },
      transferReceiptVal: {
        toCompany: [
          { required: true, type: 'string', message: '请输入账单转移目标公司名称', trigger: 'blur' }
        ],
        amount: [
          { required: true, type: 'string', message: '请输入正确金额', trigger: 'blur' },
          { validator: amountVal, trigger: 'blur' }
        ]
      },
      payBackVal: {
        amount: [
          { required: true, type: 'string', message: '请输入正确金额', trigger: 'blur' },
          { validator: amountVal, trigger: 'blur' }
        ]
      },
      sendVal: {
        toCompany: [
          { required: true, type: 'string', message: '请输入收款公司名称', trigger: 'blur' }
        ],
        amount: [
          { required: true, type: 'string', message: '请输入正确金额', trigger: 'blur' },
          { validator: amountVal, trigger: 'blur' }
        ]
      },
      createReceiptVal: {
        toCompany: [
          { required: true, type: 'string', message: '请输入收款公司名称', trigger: 'blur' }
        ],
        amount: [
          { required: true, type: 'string', message: '请输入正确金额', trigger: 'blur' },
          { validator: amountVal, trigger: 'blur' }
        ]
      },
      bankFinancingVal: {
        fromCompany: [
          { required: true, type: 'string', message: '请输入待还款公司名称', trigger: 'blur' }
        ],
        amount: [
          { required: true, type: 'string', message: '请输入金额', trigger: 'blur' },
          { validator: amountVal, trigger: 'blur' }
        ]
      }
    }
  },
  created () {
    this.privateKey = this.$cookies.get('privateKey')
    this.loadCompanyMessage(0)
    this.loadMyReceipt(1)
    this.status = 'myCompany'
    if (!this.companyMessage) {
      console.log('no company is login')
      this.$router.replace('/')
    }
  },
  methods: {
    payBack: function (toCompany) {
      this.loadState = true
      this.payBackContext = {}
      this.showPayBack = true
      this.payBackContext.toCompany = toCompany
    },
    transferReceipt: function (fromCompany) {
      this.loadState = true
      this.transferReceiptContext = {}
      this.transferReceiptContext.fromCompany = fromCompany
      this.showTransferReceipt = true
    },
    changeStatus: function (s) {
      this.status = s
    },
    handleSubmit: function (key, tmp) {
      this.loadState = false
      this.$refs[key].validate((valid) => {
        if (valid) {
          // 转账
          if (key === 'sendContext') {
            this.$http.post('/api/company/send', this.$qs.stringify({
              privateKey: this.privateKey,
              toCompany: this.sendContext.toCompany,
              amount: this.sendContext.amount
            }), {
              headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
              }
            }).then(res => {
              console.log(res)
              // var sendEvent = res.data['SendEvent(string,string,uint256)']
              var errEvent = res.data['errEvent(string)']
              if (errEvent === undefined) {
                this.$Message.success('转账成功')
              } else {
                this.$Message.error(errEvent[0][0].data)
              }
            }).catch(err => {
              this.$Message.error('服务器出错,请稍后再试')
              console.log(err)
            })
            // 创建账单
          } else if (key === 'createReceiptContext') {
            this.$http.post('/api/company/createReceipt', this.$qs.stringify({
              privateKey: this.privateKey,
              toCompany: this.createReceiptContext.toCompany,
              amount: this.createReceiptContext.amount
            }), {
              headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
              }
            }).then(res => {
              var errEvent = res.data['errEvent(string)']
              if (errEvent === undefined) {
                this.$Message.success('账单创建成功')
              } else {
                this.$Message.error(errEvent[0][0].data)
              }
            }).catch(err => {
              this.$Message.error('服务器出错,请稍后再试')
              console.log(err)
            })
            // 银行融资
          } else if (key === 'bankFinancingContext') {
            this.$http.post('/api/company/bankFinancing', this.$qs.stringify({
              privateKey: this.privateKey,
              fromCompany: this.bankFinancingContext.fromCompany,
              amount: this.bankFinancingContext.amount
            }), {
              headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
              }
            }).then(res => {
              console.log(res.data.ok)
              var errEvent = res.data['errEvent(string)']
              if (errEvent === undefined && res.data.ok !== false) {
                this.$Message.success('融资成功')
              } else {
                this.$Message.error('融资失败')
              }
            }).catch(err => {
              this.$Message.error('服务器出错,请稍后再试')
              console.log(err)
            })
            // 还款
          } else if (key === 'payBackContext') {
            console.log('payBack')
            this.$http.post('/api/company/payBack', this.$qs.stringify({
              privateKey: this.privateKey,
              toCompany: this.payBackContext.toCompany,
              amount: this.payBackContext.amount
            }), {
              headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
              }
            }).then(res => {
              console.log(res.data.ok)
              var errEvent = res.data['errEvent(string)']
              if (errEvent === undefined && res.data.ok !== false) {
                this.$Message.success('还款成功')
                this.loadMyReceipt(1)
              } else {
                this.$Message.error('还款失败')
              }
              this.showPayBack = false
            }).catch(err => {
              this.$Message.error('服务器出错,请稍后再试')
              console.log(err)
            })
            // 账单转移
          } else if (key === 'transferReceiptContext') {
            console.log('transferReceipt')
            this.$http.post('/api/company/transferReceipt', this.$qs.stringify({
              privateKey: this.privateKey,
              fromCompany: this.transferReceiptContext.fromCompany,
              amount: this.transferReceiptContext.amount,
              newToCompany: this.transferReceiptContext.toCompany
            }), {
              headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
              }
            }).then(res => {
              console.log(res.data.ok)
              var errEvent = res.data['errEvent(string)']
              if (errEvent === undefined && res.data.ok !== false) {
                this.$Message.success('账单转移成功')
                this.loadMyReceipt(1)
              } else {
                this.$Message.error('账单转移失败')
              }
              this.showTransferReceipt = false
            }).catch(err => {
              this.$Message.error('服务器出错,请稍后再试')
              console.log(err)
            })
          }
        }
      })
    },
    loadCompanyMessage: function (refresh) {
      if (refresh === 1) {
        this.$http.post('/api/login', this.$qs.stringify({
          privateKey: this.privateKey
        }), {
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
          }
        }).then(res => {
          var companyMessage = res.data['Check(string,address,uint256,uint256)'][0]
          console.log(companyMessage)
          if (companyMessage[0].data === '') {
            this.$Message.error('公司尚未注册')
          } else {
            this.$Message.success('刷新成功')
            this.$cookies.set('companyName', companyMessage[0])
            this.$cookies.set('companyAddr', companyMessage[1])
            this.$cookies.set('companyMoney', companyMessage[2])
            this.$cookies.set('companyType', companyMessage[3])
            this.companyMessage = {
              '公司名称': companyMessage[0]['data'],
              '公司链上地址': companyMessage[1]['data'],
              '公司余额': companyMessage[2]['data'].toString()
            }
          }
        }).catch(err => {
          this.$Message.error('服务器出错,请稍后再试')
          console.log(err)
        })
      } else {
        this.companyMessage = {
          '公司名称': this.$cookies.get('companyName')['data'],
          '公司链上地址': this.$cookies.get('companyAddr')['data'],
          '公司余额': this.$cookies.get('companyMoney')['data'].toString()
        }
      }
    },
    loadMyReceipt: function (refresh) {
      let that = this
      if (refresh === 1) {
        this.$http.post('/api/company/showMyReceipt', this.$qs.stringify({
          privateKey: this.privateKey
        }), {
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
          }
        }).then(res => {
          var tempMyReceipts = res.data['showReceiptEvent(string,string,uint256,bool)']
          that.myReceipts = []
          if (tempMyReceipts !== undefined) {
            for (var tempReceipt of tempMyReceipts) {
              console.log(tempReceipt)
              var receipt = {}
              for (var item of tempReceipt) {
                receipt[item.name] = item.data
              }
              that.myReceipts.push(receipt)
            }
          }
          this.$Message.success('账单加载成功')
        }).catch(err => {
          this.$Message.error('服务器出错,请稍后再试')
          console.log(err)
        })
      }
    }
  },
  watch: {
    // status: function () {
    //   if (this.status === 'myblog') {
    //     this.loadMyBlogs()
    //   } else if (this.status === 'blogground') {
    //     this.loadGroudBlogs()
    //   }
    // }
  }
}
</script>

<style scoped>
.top {
  padding-top: 2.5vmin;
}
h1{
  margin-top: 5%;
  word-wrap: break-word;
  margin-bottom: 5%;
}
.container {
  height: 95vmin;
  width: 95vmax;
  border-radius: 20px;
  background-color: white;
  margin: auto;
  box-shadow: 0 0 15px gainsboro;
  overflow: hidden;
}
.sidebar{
  float: left;
  height: 100%;
  background-color: rgb(231, 231, 231);
  width: 15%;
  box-shadow: 0px 0px 10px gray;
}
.message{
  padding: 10%;
  text-align: center;
  height: 25%;
  background-color: rgb(9, 63, 80);
  color: white;
}
.button{
  width: 105%;
  border: none;
  border-radius: 0px;
  height: 50px;
  margin: 1px;
  box-shadow: 5px 0px 5px rgb(40, 103, 128);
  border-radius: 0px 10px 10px 0px;
}
.content{
  float: left;
  width: 85%;
  height: 100%;
}
.form{
  margin-top: 5%;
  padding: 0 0% 0 10%;
  width: 85%;
}
.list{
  padding: 0 0 0 5%;
  height: 87%;
  overflow-y: scroll;
}
span{
  color: gainsboro;
}
</style>
