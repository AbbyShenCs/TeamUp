<template>
  <user-card-list :user-list="userList" :loading="loading" />
  <van-empty v-if="!userList || userList.length < 1" description="数据为空"/>
  <van-button v-if="userList && userList.length > 8" round type="primary" block style="margin: 10px 0" @click="loadMore">加载更多</van-button>
</template>
<script setup>

import {onMounted, ref} from 'vue';
import myAxios from "../plugins/myAxios.ts";
import {showFailToast, showSuccessToast} from "vant";
import {useRoute} from "vue-router";
import qs from "qs"
import UserCardList from "../components/UserCardList.vue";

const route = useRoute();
const {tags} = route.query;
const pageSize = ref(8);
const pageNum = ref(1);

let userList = ref([]);//存放用户列表
const loading =ref(true);

onMounted(async () => {     //异步调用
  loading.value = true;
  const userListData = await myAxios.get('/user/search/tags', {
    params: {
      pageSize: pageSize.value,
      pageNum: pageNum.value,
      tagNameList: tags
    },
    paramsSerializer: {
      serialize: function (params) {
        // return qs.stringify(params,{arrayFormat:'repeat'})
        return qs.stringify(params, {indices: false})
      }
    }
  }).then(function (response) {
    showSuccessToast("搜索成功")
    return response?.data?.records;  //返回数据  ?.可选链操作符，避免数据为null或undefined时报错
  }).catch(function (error) {
    console.error('/user/search/tags error', error);
    showFailToast('请求失败!');
  })
  if (userListData) {
    userListData.forEach(user => {
      if (user.tags) {
        user.tags = JSON.parse(user.tags)
      }
    })
    userList.value = userListData;
  }
  loading.value = false;
})
const loadMore = async ()=>{
  pageSize.value += pageSize.value ;
}

</script>

<style scoped>

</style>
