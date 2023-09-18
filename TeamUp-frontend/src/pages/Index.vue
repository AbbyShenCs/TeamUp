<template>
  <van-cell center title="智能匹配">
    <template #icon>
      <van-icon color="#323233" name="friends" size="20px"/>
    </template>
    <template #right-icon>
      <van-switch v-model="isMatchMode" size="24" />
    </template>
  </van-cell>
  <user-card-list  :user-list="userList" :loading="loading" />
  <van-button v-if="!isMatchMode" round type="primary" block style="margin: 10px 0" @click="loadMore">加载更多</van-button>
  <van-button v-if="isMatchMode" round type="primary" block style="margin: 10px 0" @click="loadMoreMatch">加载更多</van-button>
  <van-empty v-if="!userList || userList.length < 1" description="数据为空"/>
</template>

<script setup lang="ts">

import {ref, watchEffect} from 'vue';
import myAxios from "../plugins/myAxios";
import UserCardList from "../components/UserCardList.vue";
import {showFailToast} from "vant";
import {UserType} from "../models/user";


let userList = ref([]);//存放用户列表
const isMatchMode = ref<boolean>(false);
let loading = ref(true);
const pageSize = ref(8);
const pageNum = ref(1);

const matchPageSize = ref(5)
const matchPageNum = ref(1)
/**
 * 加载数据
 */
const loadData = async () => {
  let userListData;
  loading.value = true;
  // 心动模式，根据标签匹配用户
  if (isMatchMode.value) {
    userListData = await myAxios.get('/user/match', {
      params: {
        pageSize: matchPageSize.value,
        pageNum: matchPageNum.value
      },
    })
        .then(function (response) {
          console.log('/user/match succeed', response);
          return response?.data?.records;
        })
        .catch(function (error) {
          console.error('/user/match error', error);
          showFailToast('请求失败');
        })
  } else {
    // 普通模式，直接分页查询用户
    userListData = await myAxios.get('/user/recommend', {
      params: {
        pageSize: pageSize.value,
        pageNum: pageNum.value
      },
    })
        .then(function (response) {
          console.log('/user/recommend succeed', response);
          return response?.data?.records;
        })
        .catch(function (error) {
          console.error('/user/recommend error', error);
          showFailToast('请求失败');
        })
  }
  if (userListData) {
    userListData.forEach((user: UserType) => {
      if (user.tags) {
        user.tags = JSON.parse(user.tags);
      }
    })
    userList.value = userListData;
  }
  loading.value = false;
}
const loadMore = async ()=>{
  pageSize.value += pageSize.value ;
}
const loadMoreMatch = async ()=>{
  matchPageSize.value += matchPageSize.value;
}

watchEffect(() => {
  loadData();
})
</script>

<style scoped>

</style>
