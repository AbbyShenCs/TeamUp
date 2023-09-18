<template>
  <div id="teamPage">
    <div>
      <van-card
          v-for="team in teamList"
          :thumb=team.createUser.avatarUrl
          :desc="team.description"
          :title="`${team.name} `"
      >
        <template #tags>
          <van-tag plain type="danger" style="margin-right: 8px; margin-top: 8px">
            {{
              teamStatusEnum[team.status]
            }}
          </van-tag>
          <van-tag plain style="margin-right: 8px; margin-top: 8px" type="warning">
            队长：{{ team.createUser.planetCode }}-{{ team.createUser.username }}
          </van-tag>
        </template>
        <template #bottom>
          <div>
            {{ '队伍人数: ' + team.hasJoinNum + '/' + team.maxNum }}
          </div>
          <div v-if="team.expireTime" >
            {{ '过期时间: ' + new Date(team.expireTime).getFullYear() + '-' + (new Date(team.expireTime).getMonth() + 1) + '-' + (new Date(team.expireTime).getDate()) + ' 00:00:00' }}
          </div>
        </template>
        <template #footer>
          <van-button size="small" plain @click="doQuitTeam(team.id)">
            退出队伍
          </van-button>
          <van-button  size="small"  type="primary" plain @click="doUpdateTeam(team.id)">
            更新队伍
          </van-button>
          <van-button size="small" type="danger" plain @click="doDeleteTeam(team.id)">
            解散队伍
          </van-button>
          <van-button plain size="small"
                      @click="doTeamDetail(team.id)">查看队伍
          </van-button>
        </template>
      </van-card>
    </div>
    <van-empty v-if="teamList?.length < 1" description="数据为空"/>
  </div>
</template>

<script setup lang="ts">

import {useRouter} from "vue-router";
import {onMounted, ref} from "vue";
import myAxios from "../plugins/myAxios";
import {showFailToast, showSuccessToast} from "vant";

import {teamStatusEnum} from "../constants/team";


const router = useRouter();

const teamList = ref([]);

/**
 * 搜索队伍
 * @param val
 * @returns {Promise<void>}
 */
const listTeam = async (val = '') => {
  const res = await myAxios.get("/team/list/my/create", {
    params: {
      searchText: val,
      pageNum: 1,
    },
  });
  if (res?.code === 0) {
    teamList.value = res.data;
  } else {
    showFailToast('加载队伍失败，请刷新重试');
  }
}

/**
 * 跳转至更新队伍页
 * @param id
 */
const doUpdateTeam = (id: number) => {
  router.push({
    path: '/team/update',
    query: {
      id,
    }
  })
}

/**
 * 退出队伍
 * @param id
 */
const doQuitTeam = async (id: number) => {
  const res = await myAxios.post('/team/quit', {
    teamId: id
  });
  if (res?.code === 0) {
    showSuccessToast('操作成功');
    this.reload
  } else {
    showFailToast('操作失败' + (res.description ? `，${res.description}` : ''));
  }
}

/**
 * 解散队伍
 * @param id
 */
const doDeleteTeam = async (id: number) => {
  const res = await myAxios.post('/team/delete', {
    id,
  });
  if (res?.code === 0) {
    showSuccessToast('操作成功');
  } else {
    showFailToast('操作失败' + (res.description ? `，${res.description}` : ''));
  }
}

// 页面加载时只触发一次
onMounted( () => {
  listTeam();
})

/**
 * 跳转至队伍详情页
 * @param id
 */
const doTeamDetail = (id: number) => {
  router.push({
    path: '/team/detail',
    query: {
      id,
    }
  })
}
</script>

<style scoped>
#teamPage {

}
</style>
