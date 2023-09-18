<template>
  <div id = "teamPage">
    <van-search v-model="searchText" placeholder="搜索队伍" @search="onSearch" />
    <van-tabs v-model:active="active" @change="onTabChange">
      <van-tab title="公开" name="public"/>
      <van-tab title="加密" name="secret"/>
    </van-tabs>
    <van-button class="add-button" type="primary" icon="plus" @click="toCreateTeam" />
    <team-card-list :teamList="teamList" />
    <van-empty v-if="!teamList || teamList.length < 1" description="数据为空"/>
  </div>
</template>

<script setup>
import {useRouter} from "vue-router";
import TeamCardList from "../components/TeamCardList.vue";
import {onMounted, ref} from "vue";
import myAxios from "../plugins/myAxios";
import {showFailToast, showSuccessToast} from "vant";

const router = useRouter();

// 跳转到登录界面
const toCreateTeam = () => {
  router.push({
    path: "/team/add"
  })
}

const teamList = ref([]);
const originList = ref([]);
const searchText = ref([]);

/**
 * 搜索队伍
 * @param val
 * @returns {Promise<void>}
 */
const listTeam = async (val = '') => {
  const res = await myAxios.get("/team/list", {
    params: {
      searchText: val,
      pageNum: 1,
      pageSize: 10,
    },
  });
  if (res?.code === 0) {
    teamList.value = res.data;
    originList.value = res.data;
    showSuccessToast('搜索成功');
  } else {
    showFailToast('加载队伍失败，请刷新重试');
  }
}
const active =  ref('public');

const onTabChange = (name) => {
  if (name === 'public') {
      // 待补充 originList
      teamList.value = originList.value.filter(item => item.status === 0)
    } else {
      teamList.value = originList.value.filter(item => item.status === 2)
    }
}


onMounted(async () => {
  await listTeam()
  teamList.value = originList.value.filter(item => item.status === 0)
})

const onSearch = (val) => {
  listTeam(val)
}
</script>

<style scoped>

</style>
