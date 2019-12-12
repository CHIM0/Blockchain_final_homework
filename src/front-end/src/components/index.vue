<template>
  <div class='top'>
    <div class='container'>
      <h1>区块链供应链平台</h1>
      <Row type="flex" justify="center">
        <Tabs>
          <TabPane class="contentArea" icon="ios-log-in" label="登录公司" name="login">
            <Form ref="loginInfo" :model="loginInfo" :rules="rules">
              <FormItem prop="privateKey">
                <Input size="large" type="password" v-model="loginInfo.privateKey" prefix="ios-lock-outline" placeholder="输入私钥(64个字符组成）" />
              </FormItem>
              <FormItem class="buttonArea">
                <Button class="button" long @click="login">登录</Button>
              </FormItem>
            </Form>
          </TabPane>
          <TabPane class="contentArea" icon="ios-person-add-outline" label="注册公司" name="register">
            <Form ref="registerInfo" :model="registerInfo" :rules="rules">
              <FormItem prop="companyName">
                  <Input size="large" type="text" v-model="registerInfo.companyName" prefix="ios-briefcase-outline" placeholder="公司名称" />
                </FormItem>
              <FormItem prop="privateKey">
                <Input size="large" type="password" v-model="registerInfo.privateKey" prefix="ios-lock-outline" placeholder="输入私钥（64个字符组成）" />
              </FormItem>
              <FormItem prop="privateKey2">
                <Input size="large" type="password" v-model="registerInfo.privateKey2" prefix="ios-lock-outline" placeholder="再次输入私钥（64个字符组成）" />
              </FormItem>
              <FormItem class="buttonArea">
                <Button class="button" long @click="register">注册公司</Button>
              </FormItem>
            </Form>
          </TabPane>
        </Tabs>
      </Row>
    </div>
  </div>
</template>
<script>
export default {
  name: 'index',
  data: function () {
    const comfirmPass = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请再次输入秘钥'))
      } else if (value !== this.registerInfo.privateKey) {
        callback(new Error('两次秘钥不一致'))
      } else {
        callback()
      }
    }
    return {
      loginInfo: {
        privateKey: ''
      },
      registerInfo: {
        companyName: '',
        privateKey: '',
        privateKey2: ''
      },
      rules: {
        companyName: [
          {required: true, message: '公司名称不能为空', trigger: 'blur'},
          {type: 'string', min: 2, max: 10, message: '长度在2-10字符之间', trigger: 'blur'}
        ],
        privateKey: [
          {required: true, message: '秘钥不能为空', trigger: 'blur'},
          {type: 'string', min: 64, max: 64, message: '长度为64字符', trigger: 'blur'}
        ],
        privateKey2: [
          {required: true, validator: comfirmPass, trigger: 'blur'}
        ]
      }
    }
  },
  methods: {
    toOJ: function (inputIndex) {
      this.$router.push({
        name: 'vote',
        params: {
          access_token: this.access_token,
          env: this.env,
          vote_index: inputIndex
        }
      })
    },
    login: function () {
      let that = this
      this.$refs['loginInfo'].validate((valid) => {
        if (valid) {
          console.log('login')
          this.$http.post('/api/login', this.$qs.stringify({
            privateKey: this.loginInfo.privateKey
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
              this.$Message.success('登陆成功')
              this.$cookies.set('companyName', companyMessage[0])
              this.$cookies.set('companyAddr', companyMessage[1])
              this.$cookies.set('companyMoney', companyMessage[2])
              this.$cookies.set('companyType', companyMessage[3])
              this.$cookies.set('privateKey', that.loginInfo.privateKey)
              this.toNext()
            }
          }).catch(err => {
            this.$Message.error('服务器出错,请稍后再试')
            console.log(err)
          })
        }
      })
    },
    register: function () {
      this.$refs['registerInfo'].validate((valid) => {
        if (valid) {
          console.log('register')
          this.$http.post('/api/regist', this.$qs.stringify({
            companyName: this.registerInfo.companyName,
            privateKey: this.registerInfo.privateKey,
            initCompanyMoney: 1000000
          }), {
            headers: {
              'Content-Type': 'application/x-www-form-urlencoded'
            }
          }).then(res => {
            console.log(res.data)
            var errEvent = res.data['errEvent(string)']
            var registerEvent = res.data['Register(string)']
            if (res.data.ok === 'false') {
              this.$Message.error('参数错误')
            } else if (errEvent !== undefined && errEvent[0][0].name === 'errMessage') {
              this.$Message.error(errEvent[0][0].data)
            } else if (registerEvent !== undefined) {
              this.$Message.success('公司注册成功')
            }
          }).catch(err => {
            this.$Message.error('服务器出错,请稍后再试')
            console.log(err)
          })
        }
      })
    },
    toNext () {
      this.$router.push('/home')
    }
  }
}
</script>

<style scoped>
.top{
  padding: 10vh;
}
.container{
 background-color: white;
 border-radius: 15px;
 width: 50vmax;
 padding: 3%;
 text-align: center;
 margin: auto;
 box-shadow: 0 10px 10px gray;
}
.container h1{
  margin-bottom: 10%;
}
</style>
