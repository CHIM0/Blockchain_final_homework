<template>
  <div id="app">
    <transition name="fade" mode="out-in" appear>
      <router-view/>
    </transition>
  </div>
</template>

<script>
export default {
  name: 'App',
  data () {
    return {
      transitionName: 'slide-left'
    }
  },
  watch: {

  }
}
</script>

<style>
#app {
  margin-top:0px;
  font-family: 'Avenir', Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  height: 100vh;
  background-image: url(https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1575990180361&di=04d58fd830a52ba5b3460ecf8528bc51&imgtype=0&src=http%3A%2F%2Fn.sinaimg.cn%2Fsinacn10110%2F213%2Fw2048h1365%2F20190110%2F7d52-hrkkweh6021839.jpg);
  background-repeat: no-repeat;
  background-size: cover;
  align-content: center;
}
.fade-enter-active, .fade-leave-active{
  transition: all 0.5s ease;
}
.fade-enter, .fade-leave-active{
  opacity: 0;
}
</style>
