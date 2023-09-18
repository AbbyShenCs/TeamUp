<template>
  <div id="teamCardList">
    <van-card
        v-for="team in props.teamList"
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
        <van-button size="small" type="primary"  plain
                    @click="preJoinTeam(team)">
          加入队伍
        </van-button>
        <van-button size="small" v-if="team.userId === currentUser?.id || currentUser.userRole === 1" plain @click="doQuitTeam(team.id)">
          退出队伍
        </van-button>
        <van-button  size="small" v-if="team.userId === currentUser?.id || currentUser.userRole === 1" type="primary" plain @click="doUpdateTeam(team.id)">
          更新队伍
        </van-button>
        <van-button size="small" v-if="team.userId === currentUser?.id || currentUser.userRole === 1  " type="danger" plain @click="doDeleteTeam(team.id)">
          解散队伍
        </van-button>
        <van-button plain size="small"
                    @click="doTeamDetail(team.id)">查看队伍
        </van-button>
      </template>
    </van-card>
    <van-dialog v-model:show="showPasswordDialog" title="请输入密码" show-cancel-button @confirm="doJoinTeam" @cancel="doJoinCancel">
      <van-field v-model="password" placeholder="请输入密码"/>
    </van-dialog>
  </div>

</template>

<script setup lang="ts">
import {TeamType} from "../models/team";
import {teamStatusEnum} from "../constants/team";
import myAxios from "../plugins/myAxios";

import {useRouter} from "vue-router";
import {showFailToast, showSuccessToast} from 'vant';
import {onMounted, ref} from "vue";
import {getCurrentUser} from "../services/user";


const currentUser = ref();

onMounted(() => {
  currentUser.value = getCurrentUser();
})
interface TeamCardListProps {
  teamList: TeamType[];
}

const props = withDefaults(defineProps<TeamCardListProps>(), {
  // @ts-ignore
  teamList: [] as TeamType[],
});

console.log(props.teamList)

const router = useRouter();

const showPasswordDialog = ref(false);
const password = ref('');
const joinTeamId = ref(0);


/**
 * 加入队伍
 */
const doJoinTeam = async () => {
  if (!joinTeamId.value){
    return;
  }
  const res = await myAxios.post('/team/join', {
    teamId: joinTeamId.value,
    password: password.value
  });
  if (res?.code === 0) {
    showSuccessToast('加入成功');
    doJoinCancel();
  } else {
    showFailToast('加入失败' + (res.description ? `，${res.description}` : ''));
  }
}

/**
 * 判断是不是加密房间，是的话显示密码框
 * @param team
 */
const preJoinTeam = (team: TeamType) => {
  joinTeamId.value = team.id;
  if (team.status === 0) {
    doJoinTeam()
  } else {
    showPasswordDialog.value = true;
  }
}

const doJoinCancel = () => {
  joinTeamId.value = 0;
  password.value = '';
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

</style>
